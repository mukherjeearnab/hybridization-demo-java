package model.description;

import java.util.Arrays;

/**
 * The data type of a value (UML valueDescription.puml: DataType, RealType,
 * TensorType ...).
 *
 * - typeName: kind of value, e.g. "Real" (a floating-point number) or "String"
 * - precision: how many bits are used: "float64" (Java double, used by
 * simulators)
 * or "float32" (used by most neural networks)
 * - shape: empty for a single number ("scalar"); otherwise the size of each
 * dimension of a
 * "tensor", e.g. [4, 3] for a matrix with 4 rows and 3 columns.
 */
public class DataType {
    public final String typeName;
    public final String precision;
    public final int[] shape;

    public DataType(String typeName, String precision, int... shape) {
        this.typeName = typeName;
        this.precision = precision;
        this.shape = shape; // "int..." lets you pass any number of ints, e.g. new DataType("Real",
                            // "float32", 4, 3)
    }

    public static final DataType REAL64 = new DataType("Real", "float64");
    public static final DataType REAL32 = new DataType("Real", "float32");
    public static final DataType STRING = new DataType("String", "utf-8");

    /** Creates a float32 tensor type with the given shape, e.g. tensor(4, 3). */
    public static DataType tensor(int... shape) {
        return new DataType("Real", "float32", shape);
    }

    /** Total number of elements, e.g. 4 x 3 = 12 (1 for a scalar). */
    public int count() {
        int n = 1;
        for (int s : shape) {
            n *= s;
        }
        return n;
    }

    /** E.g. "Real(float64)" or "Real(float32)[4, 3]". */
    @Override
    public String toString() {
        return typeName + "(" + precision + ")" + (shape.length == 0 ? "" : Arrays.toString(shape));
    }
}
