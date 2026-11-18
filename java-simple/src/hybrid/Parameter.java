package hybrid;

import java.util.Arrays;

/**
 * A named constant that configures a model. Unlike a Variable (port), a parameter does not change
 * while the model runs.
 *
 * A parameter can be:
 *   - a single number (a "scalar"), e.g. the thermal resistance  R = 0.5 K/W
 *   - a table of numbers (a "tensor"), e.g. a 4 x 3 weight matrix of a neural network
 *
 * Tensors are stored in a flat array ("values") plus a "shape" telling how to read it.
 * Example: shape [2, 3] and values {1,2,3, 4,5,6} means the matrix
 *     | 1 2 3 |
 *     | 4 5 6 |
 * (row by row). A scalar simply has an empty shape and exactly one value.
 *
 * In the UML diagrams this is the class "Parameter".
 */
public class Parameter {
    public final String name;     // e.g. "R" or "net.0.weight"
    public final String unit;     // physical unit, "1" means "no unit"
    public final int[] shape;     // size of each dimension; empty for a single number
    public final double[] values; // the numbers themselves, row by row

    /** Creates a scalar parameter (a single number), e.g. new Parameter("R", "K/W", 0.5). */
    public Parameter(String name, String unit, double value) {
        // "this(...)" calls the other constructor below: empty shape, one value.
        this(name, unit, new int[0], new double[]{value});
    }

    /** Creates a tensor parameter (vector, matrix, ...). Values must be given row by row. */
    public Parameter(String name, String unit, int[] shape, double[] values) {
        this.name = name;
        this.unit = unit;
        this.shape = shape;
        this.values = values;
    }

    /** Convenience method for scalar parameters: returns the single number. */
    public double value() {
        return values[0];
    }

    /**
     * Used when printing. Scalars show their value, tensors only show their shape
     * (printing hundreds of weights would not be readable).
     * getClass().getSimpleName() prints the real class name, e.g. "LearnedParameter".
     */
    @Override
    public String toString() {
        String what = shape.length == 0 ? " = " + value() : " shape " + Arrays.toString(shape);
        return getClass().getSimpleName() + " " + name + what + " [" + unit + "]";
    }
}
