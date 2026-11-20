package model.mic.hybridModel;

import model.portsInternalVariablesAndParameters.variables.PortVariable;

/**
 * One directed connection, from {@code output} to {@code input}.
 * A whole-port connection is stored as one ModelLink per matched variable pair.
 * The link keeps the variable objects, not their positions in the port lists.
 */
public final class ModelLink {
    public final PortVariable output;
    public final PortVariable input;

    ModelLink(PortVariable output, PortVariable input) {
        this.output = output;
        this.input = input;
    }
}
