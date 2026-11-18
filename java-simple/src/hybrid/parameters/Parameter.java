package hybrid.parameters;

import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;
import hybrid.description.ValueDescription;

/**
 * A named constant that configures a model (UML: Parameter, a kind of ValueDescription).
 * Unlike a Variable (port), a parameter does not change while the model runs.
 *
 * For a simple numeric parameter such as the thermal resistance R = 0.5 K/W, the number is
 * stored in the inherited field "defaultValue". ML models use the more specialised subclasses
 * LearnedParameter, NonTrainableState and Hyperparameter.
 */
public class Parameter extends ValueDescription {

    /** Numeric scalar parameter, e.g. new Parameter("R", "Thermal resistance", DataType.REAL64, Unit.KELVIN_PER_WATT, 0.5). */
    public Parameter(String name, String description, DataType dataType, Unit unit, Double value) {
        super(name, description, dataType, unit);
        this.defaultValue = value;
    }

    /** The parameter's number. */
    public double value() {
        return defaultValue;
    }

    /** Puts the value right into the headline, e.g. "R = 0.5 : Real(float64) [K/W] - ...". */
    @Override
    public String headline() {
        return defaultValue == null ? super.headline() : name + " = " + defaultValue
                + super.headline().substring(name.length());
    }

    /** The value is already in the headline, so remove "default: ..." from the details. */
    @Override
    protected List<String> details() {
        List<String> d = super.details();
        d.removeIf(s -> s.startsWith("default:"));
        return d;
    }
}
