package hybrid;

/**
 * A parameter whose value was LEARNED during training of an ML model: the weights and biases
 * of a neural network. In PyTorch these are stored in the model's "state_dict" checkpoint.
 *
 * It is a special kind of Parameter ("extends Parameter"), so it can be stored in the same
 * parameter list of a model as R, C, etc.
 *
 * In the UML diagram ml.puml this is the class "LearnedParameter".
 */
public class LearnedParameter extends Parameter {
    public LearnedParameter(String name, int[] shape, double[] values) {
        // "super(...)" calls the constructor of the parent class Parameter.
        // Weights have no physical unit, hence "1".
        super(name, "1", shape, values);
    }
}
