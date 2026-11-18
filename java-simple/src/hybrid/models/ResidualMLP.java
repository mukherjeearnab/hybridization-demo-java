package hybrid.models;

import hybrid.description.DataType;
import hybrid.description.RangeDomain;
import hybrid.description.Uncertainty;
import hybrid.description.Unit;
import hybrid.mic.MlMIC;
import hybrid.parameters.Hyperparameter;
import hybrid.parameters.LearnedParameter;
import hybrid.parameters.NonTrainableState;
import hybrid.variables.InternalVariable;
import hybrid.variables.MlVariable;
import hybrid.variables.Port;
import hybrid.variables.Variable;

/**
 * The MACHINE-LEARNING part of the hybrid model: a Java version of torch/model.py (ResidualMLP).
 *
 * Why is it needed? A physics model is never perfect (simplified equations, uncertain parameters).
 * The difference between reality and the simulation is called the "residual":
 *     residual = measured temperature - simulated temperature
 * This small neural network (an MLP, "multi-layer perceptron") has been trained to PREDICT that
 * residual from the simulated temperature and the model inputs:
 *     residual = net(normalize([temp_sim, power, ambient]))
 *
 * Note: the real network is 3 -> 32 -> 32 -> 1 neurons. To keep this example readable it is
 * shrunk to 3 -> 4 -> 1, and the weights are made-up numbers. In a real project they would be
 * loaded from the trained PyTorch checkpoint.
 */
public class ResidualMLP extends MlMIC {

    private static final String CHECKPOINT = "torch/checkpoints/residual_mlp.pt";

    public ResidualMLP() {
        // 1. Fill in the identity card (MlMIC): name, description, framework.
        super("ResidualMLP", "Predicts the error of the physics model", "PyTorch");

        // 2. Declare the ports. The order of the input features is the order the network expects.
        //    The validity domain of each feature is its "training envelope": the range seen in the training data.
        Port features = port("features", "Input features of the network");
        MlVariable tSim = features.add(new MlVariable("temperature_sim", "Temperature simulated by the physics model",
                Variable.InOut.IN, MlVariable.Role.inputFeature, DataType.REAL32, Unit.DEG_C));
        tSim.quantityKind = "Temperature";
        tSim.validityDomain = new RangeDomain(0.0, 1100.0, "training envelope", "min/max of training data");

        MlVariable power = features.add(new MlVariable("power", "Applied heater power",
                Variable.InOut.IN, MlVariable.Role.inputFeature, DataType.REAL32, Unit.WATT));
        power.quantityKind = "Power";
        power.validityDomain = new RangeDomain(0.0, 250.0, "training envelope", "min/max of training data");

        MlVariable ambient = features.add(new MlVariable("ambient", "Ambient temperature",
                Variable.InOut.IN, MlVariable.Role.inputFeature, DataType.REAL32, Unit.DEG_C));
        ambient.quantityKind = "Temperature";
        ambient.validityDomain = new RangeDomain(10.0, 35.0, "training envelope", "min/max of training data");

        Port prediction = port("prediction", "Output of the network");
        MlVariable residual = prediction.add(new MlVariable("predicted_residual",
                "Predicted error: measured minus simulated temperature",
                Variable.InOut.OUT, MlVariable.Role.target, DataType.REAL32, Unit.KELVIN));
        residual.quantityKind = "TemperatureDifference";
        residual.uncertainty = new Uncertainty("normal", 0.3, "RMSE on the test dataset");

        // 3. Declare the architecture, named like PyTorch's nn.Sequential: net.0, net.1, net.2
        layer("net.0", "Linear"); // 3 inputs -> 4 hidden neurons
        layer("net.1", "Tanh");   // activation function
        layer("net.2", "Linear"); // 4 hidden neurons -> 1 output

        // 4. Internal variable: the hidden layer's output. tanh keeps it between -1 and 1.
        InternalVariable hidden = internal(new InternalVariable("net.1.out", "Hidden-layer activations",
                DataType.tensor(4), Unit.ONE));
        hidden.validityDomain = new RangeDomain(-1.0, 1.0, "numerical", "range of tanh");

        // 5. Hyperparameters: choices made before training.
        parameter(new Hyperparameter("hidden_size", "Neurons in the hidden layer",
                Hyperparameter.Category.architecture, "4")).tuningMethod = "manual";
        parameter(new Hyperparameter("activation", "Activation function",
                Hyperparameter.Category.architecture, "tanh")).tuningMethod = "manual";
        Hyperparameter lr = parameter(new Hyperparameter("learning_rate", "Step size of the Adam optimiser",
                Hyperparameter.Category.optimizer, "0.001"));
        lr.isTuned = true;
        lr.tuningMethod = "randomSearch";

        // 6. Learned weights and biases. Their names "<layer>.weight" / "<layer>.bias" link them to the
        //    layers above. Weight shape is [outputs, inputs]: here 4 rows (neurons) x 3 columns (features).
        learnedParameter(new LearnedParameter("net.0.weight", "Weights of the hidden layer",
                LearnedParameter.Kind.weight, new double[]{
                0.8, -0.3, 0.2,     // neuron 1
                -0.5, 0.6, 0.1,     // neuron 2
                0.3, 0.4, -0.7,     // neuron 3
                -0.2, -0.1, 0.5},   // neuron 4
                4, 3));
        learnedParameter(new LearnedParameter("net.0.bias", "Biases of the hidden layer",
                LearnedParameter.Kind.bias, new double[]{0.1, -0.2, 0.05, 0.0}, 4));
        learnedParameter(new LearnedParameter("net.2.weight", "Weights of the output layer",
                LearnedParameter.Kind.weight, new double[]{0.6, -0.4, 0.3, 0.5}, 1, 4));
        learnedParameter(new LearnedParameter("net.2.bias", "Bias of the output layer",
                LearnedParameter.Kind.bias, new double[]{-0.1}, 1));

        // 7. Normalisation statistics (one value per input feature).
        //    Neural networks work best with inputs around 0, so each feature is rescaled:
        //        x_norm = (x - mean) / std
        parameter(new NonTrainableState("feature_mean", "Mean of each feature in the training data",
                NonTrainableState.Kind.buffer, new double[]{150.0, 100.0, 20.0})).storageRef = "normalization.json#mean";
        parameter(new NonTrainableState("feature_std", "Standard deviation of each feature in the training data",
                NonTrainableState.Kind.buffer, new double[]{250.0, 60.0, 5.0})).storageRef = "normalization.json#std";
    }

    /** Adds a learned parameter together with the information shared by all of them. */
    private void learnedParameter(LearnedParameter p) {
        p.initializer = "PyTorch default (uniform)";
        p.storageRef = CHECKPOINT + "#" + p.name;
        parameter(p);
    }

    /**
     * Predicts the residual (correction) for one point in time.
     *
     * @return the predicted residual in K, to be ADDED to the simulated temperature
     */
    public double predict(double temperatureSim, double power, double ambient) {
        // Read the normalisation statistics from the MIC parameters.
        double[] mean = ((NonTrainableState) parameter("feature_mean")).values;
        double[] std = ((NonTrainableState) parameter("feature_std")).values;

        // Build the feature vector (same order as the input ports) and normalise it.
        double[] x = {temperatureSim, power, ambient};
        for (int i = 0; i < x.length; i++) {
            x[i] = (x[i] - mean[i]) / std[i];
        }

        // Run the network (MlMIC.forward). It has a single output, so take element 0.
        return forward(x)[0];
    }
}
