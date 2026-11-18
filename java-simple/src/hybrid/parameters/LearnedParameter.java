package hybrid.parameters;

import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;

/**
 * A parameter whose values were LEARNED during training: the weights and biases of a neural
 * network (UML ml.puml: LearnedParameter). In PyTorch they are stored in the "state_dict" checkpoint.
 *
 * Many numbers are stored in one flat array ("values"); the shape in the data type says how to read it.
 * Example: shape [2, 3] and values {1,2,3, 4,5,6} means the matrix
 *     | 1 2 3 |
 *     | 4 5 6 |
 * (read row by row).
 */
public class LearnedParameter extends Parameter {

    public enum Kind { weight, bias, embedding, filter, scale, shift }

    public final Kind kind;
    public final double[] values;
    public String initializer;   // how the values were initialised before training
    public boolean frozen;       // true = not updated during training (e.g. when fine-tuning)
    public String storageRef;    // where the values are stored, e.g. "checkpoint.pt#net.0.weight"

    public LearnedParameter(String name, String description, Kind kind, double[] values, int... shape) {
        // A weight has no physical unit and no single "value", hence Unit.ONE and null.
        super(name, description, DataType.tensor(shape), Unit.ONE, null);
        if (values.length != dataType.count()) {
            throw new IllegalArgumentException(name + ": expected " + dataType.count() + " values");
        }
        this.kind = kind;
        this.values = values;
    }

    // Chained setters for the optional fields (call them BEFORE the inherited with-methods,
    // because those return a general type).
    public LearnedParameter withInitializer(String initializer) {
        this.initializer = initializer;
        return this;
    }

    public LearnedParameter withFrozen(boolean frozen) {
        this.frozen = frozen;
        return this;
    }

    public LearnedParameter withStorageRef(String storageRef) {
        this.storageRef = storageRef;
        return this;
    }

    @Override
    protected List<String> details() {
        List<String> d = super.details();
        d.add(0, "kind: " + kind);
        d.add(1, "count: " + dataType.count());
        if (initializer != null) d.add("initializer: " + initializer);
        d.add("frozen: " + frozen);
        if (storageRef != null) d.add("stored in: " + storageRef);
        return d;
    }
}
