package hybrid;

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

    public ResidualMLP() {
        // 1. Fill in the identity card (MlMIC): name, description, framework.
        super("ResidualMLP", "Predicts the error of the physics model", "PyTorch");

        // 2. Declare the ports. The order of the inputs is the order of the features the network expects.
        port("temperature_sim", "degC", Variable.InOut.IN);
        port("power", "W", Variable.InOut.IN);
        port("ambient", "degC", Variable.InOut.IN);
        port("predicted_residual", "K", Variable.InOut.OUT);

        // 3. Declare the architecture, named like PyTorch's nn.Sequential: net.0, net.1, net.2
        layer("net.0", "Linear"); // 3 inputs -> 4 hidden neurons
        layer("net.1", "Tanh");   // activation function
        layer("net.2", "Linear"); // 4 hidden neurons -> 1 output

        // 4. Declare the learned weights and biases as MIC parameters.
        //    Their names "<layer>.weight" / "<layer>.bias" link them to the layers above.
        //    Weight shape is [outputs, inputs]: here 4 rows (neurons) x 3 columns (features).
        parameter(new LearnedParameter("net.0.weight", new int[]{4, 3}, new double[]{
                0.8, -0.3, 0.2,    // neuron 1
                -0.5, 0.6, 0.1,    // neuron 2
                0.3, 0.4, -0.7,    // neuron 3
                -0.2, -0.1, 0.5})); // neuron 4
        parameter(new LearnedParameter("net.0.bias", new int[]{4}, new double[]{0.1, -0.2, 0.05, 0.0}));
        parameter(new LearnedParameter("net.2.weight", new int[]{1, 4}, new double[]{0.6, -0.4, 0.3, 0.5}));
        parameter(new LearnedParameter("net.2.bias", new int[]{1}, new double[]{-0.1}));

        // 5. Declare the normalisation statistics (one value per input feature).
        //    Neural networks work best with inputs around 0, so each feature is rescaled:
        //        x_norm = (x - mean) / std
        parameter(new NonTrainableState("feature_mean", new int[]{3}, new double[]{150.0, 100.0, 20.0}));
        parameter(new NonTrainableState("feature_std", new int[]{3}, new double[]{250.0, 60.0, 5.0}));
    }

    /**
     * Predicts the residual (correction) for one point in time.
     *
     * @return the predicted residual in K, to be ADDED to the simulated temperature
     */
    public double predict(double temperatureSim, double power, double ambient) {
        // Read the normalisation statistics from the MIC parameters.
        double[] mean = parameter("feature_mean").values;
        double[] std = parameter("feature_std").values;

        // Build the feature vector (same order as the input ports) and normalise it.
        double[] x = {temperatureSim, power, ambient};
        for (int i = 0; i < x.length; i++) {
            x[i] = (x[i] - mean[i]) / std[i];
        }

        // Run the network (MlMIC.forward). It has a single output, so take element 0.
        return forward(x)[0];
    }
}
