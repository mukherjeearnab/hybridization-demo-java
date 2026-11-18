package hybrid.parameters;

import java.util.Arrays;
import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;

/**
 * A fixed set of values an ML model needs, but which is NOT learned by gradient descent
 * (UML ml.puml: NonTrainableState).
 * Typical example: the mean and standard deviation used to normalise the input features.
 * They are computed once from the training data and then kept constant.
 */
public class NonTrainableState extends Parameter {

    public enum Kind { runningMean, runningVariance, positionalEncoding, buffer }

    public final Kind kind;
    public final double[] values;
    public String storageRef;

    public NonTrainableState(String name, String description, Kind kind, double[] values) {
        super(name, description, DataType.tensor(values.length), Unit.ONE, null);
        this.kind = kind;
        this.values = values;
    }

    /** Chained setter: where the values are stored (optional). */
    public NonTrainableState withStorageRef(String storageRef) {
        this.storageRef = storageRef;
        return this;
    }

    @Override
    protected List<String> details() {
        List<String> d = super.details();
        d.add(0, "kind: " + kind);
        d.add(1, "values: " + Arrays.toString(values)); // few numbers, so we can print them
        if (storageRef != null) d.add("stored in: " + storageRef);
        return d;
    }
}
