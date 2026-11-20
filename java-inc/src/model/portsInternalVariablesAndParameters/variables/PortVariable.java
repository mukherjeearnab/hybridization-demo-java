package model.portsInternalVariablesAndParameters.variables;

import java.util.List;

import model.description.DataType;
import model.description.Unit;
import model.description.ValueDescription;

/**
 * A port variable: one value that flows INTO the model (input) or OUT of it
 * (output)
 * (UML: Variable, a kind of ValueDescription with the extra attribute "inOut").
 *
 * Think of a model as a black box with plugs on it. Every plug is a Variable:
 * the thermal model has the inputs "power" and "ambient" and the output
 * "temperature".
 *
 * Besides its direction, every variable says what ROLE it plays for the model.
 * The UML only defines
 * roles for ML models (ml.puml: Variable.role = inputFeature | target | label |
 * auxiliary), but the
 * idea is general: a simulation input can be something you control (heater
 * power) or something the
 * surroundings impose (ambient temperature). So one Role list is shared by all
 * kinds of models.
 */
public class PortVariable extends ValueDescription {

    /**
     * Direction of the value: IN = the model reads it, OUT = the model produces it.
     */
    public enum InOut {
        IN, OUT
    }

    public final InOut inOut;

    public PortVariable(String name, String description, InOut inOut, DataType dataType, Unit unit) {
        super(name, description, dataType, unit); // ValueDescription stores the common fields
        this.inOut = inOut;
    }

    /** Puts the direction in front, e.g. "IN power : Real(float64) [W] - ...". */
    @Override
    public String headline() {
        return String.format("%-4s", inOut) + super.headline();
    }
}
