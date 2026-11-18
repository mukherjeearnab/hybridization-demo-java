package hybrid;

/**
 * A "port" of a model: one value that flows INTO the model (an input) or OUT of it (an output).
 *
 * Think of a model as a black box with plugs on it. Every plug is a Variable:
 *   - the thermal model has the inputs "power" and "ambient" and the output "temperature".
 *
 * In the UML diagrams this is the class "Variable" (with the attribute "inOut").
 */
public class Variable {

    /** Direction of the value: IN = the model reads it, OUT = the model produces it. */
    public enum InOut { IN, OUT }

    public final String name;   // e.g. "power"
    public final String unit;   // physical unit, e.g. "W" (watt) or "degC" (degrees Celsius)
    public final InOut inOut;   // input or output?

    public Variable(String name, String unit, InOut inOut) {
        this.name = name;
        this.unit = unit;
        this.inOut = inOut;
    }

    /** Used when printing, e.g. "IN power [W]". */
    @Override
    public String toString() {
        return inOut + " " + name + " [" + unit + "]";
    }
}
