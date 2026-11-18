package hybrid;

/**
 * A fixed value an ML model needs, but which is NOT learned by gradient descent.
 * Typical example: the mean and standard deviation used to normalise the input features.
 * They are computed once from the training data and then kept constant.
 *
 * In the UML diagram ml.puml this is the class "NonTrainableState".
 */
public class NonTrainableState extends Parameter {
    public NonTrainableState(String name, int[] shape, double[] values) {
        super(name, "1", shape, values);
    }
}
