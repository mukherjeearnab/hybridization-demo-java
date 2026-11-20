package model.portsInternalVariablesAndParameters;

import java.util.ArrayList;
import java.util.List;

import model.portsInternalVariablesAndParameters.groups.Port;
import model.portsInternalVariablesAndParameters.variables.PortVariable;

/**
 * The top-level collection of a model's connection points (UML exec.puml: Ports).
 *
 * A variable can be added here directly, or grouped with other variables in a
 * named {@link Port}. A hybrid can connect variables from either location.
 */
public class Ports {
    public final List<PortVariable> variables = new ArrayList<>();
    public final List<Port> ports = new ArrayList<>();

    public Ports() {
    }

    /**
     * Adds a variable directly to this ports and returns it, so you can write:
     * Variable v = ports.add(new Variable(...));
     */
    public <T extends PortVariable> T add(T variable) {
        variables.add(variable);
        return variable;
    }

    /**
     * Adds a sub-port (group of variables / sub-ports) to this ports and returns
     * it, so you can write: Port p = ports.add(new Port(...));
     */
    public Port add(Port port) {
        ports.add(port);
        return port;
    }
}
