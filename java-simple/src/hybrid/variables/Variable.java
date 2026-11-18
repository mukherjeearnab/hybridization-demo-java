package hybrid.variables;

import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;
import hybrid.description.ValueDescription;

/**
 * A port variable: one value that flows INTO the model (input) or OUT of it (output)
 * (UML: Variable, a kind of ValueDescription with the extra attribute "inOut").
 *
 * Think of a model as a black box with plugs on it. Every plug is a Variable:
 * the thermal model has the inputs "power" and "ambient" and the output "temperature".
 *
 * Besides its direction, every variable says what ROLE it plays for the model. The UML only defines
 * roles for ML models (ml.puml: Variable.role = inputFeature | target | label | auxiliary), but the
 * idea is general: a simulation input can be something you control (heater power) or something the
 * surroundings impose (ambient temperature). So one Role list is shared by all kinds of models.
 */
public class Variable extends ValueDescription {

    /** Direction of the value: IN = the model reads it, OUT = the model produces it. */
    public enum InOut { IN, OUT }

    /** What a value is used for in this model, independent of whether it flows in or out. */
    public enum Role {
        /** A setting the caller can change to influence the model, such as heater power. */
        controlInput,
        /** A condition imposed by the environment, such as ambient temperature. */
        boundaryCondition,
        /** A value the simulation carries from step to step, such as plant temperature. */
        state,
        /** An estimate produced by a model, such as the corrected temperature. */
        prediction,
        /** A value fed into an ML model, such as the simulated temperature or heater power. */
        inputFeature,
        /** The quantity the ML model learns to predict, such as the temperature residual. */
        target,
        /** A known annotation for a data example, such as its class in a classifier. */
        label,
        /** A supporting value, such as a diagnostic, rather than a primary feature or result. */
        auxiliary
    }

    public final InOut inOut;
    public final Role role;

    public Variable(String name, String description, InOut inOut, Role role, DataType dataType, Unit unit) {
        super(name, description, dataType, unit); // ValueDescription stores the common fields
        this.inOut = inOut;
        this.role = role;
    }

    /** Puts the direction in front, e.g. "IN  power : Real(float64) [W] - ...". */
    @Override
    public String headline() {
        return String.format("%-4s", inOut) + super.headline();
    }

    /** Adds the role in front of the common details. */
    @Override
    protected List<String> details() {
        List<String> d = super.details();
        d.add(0, "role: " + role);
        return d;
    }
}
