package model.portsInternalVariablesAndParameters.variables;

import model.description.DataType;
import model.description.Unit;
import model.description.ValueDescription;

/**
 * A value computed INSIDE a model that is not visible as an input or output
 * (UML: InternalVariable, a kind of ValueDescription).
 *
 * Examples: the rate of change dT/dt in the physics model, the hidden-layer
 * activations of
 * the neural network, or the intermediate signals temp_sim /
 * residual_correction of the hybrid model.
 * Describing them helps to understand and debug what happens inside the model.
 */
public class InternalVariable extends ValueDescription {
    public InternalVariable(String name, String description, DataType dataType, Unit unit) {
        super(name, description, dataType, unit);
    }
}
