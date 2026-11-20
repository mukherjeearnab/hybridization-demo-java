package model.portsInternalVariablesAndParameters.groups;

import java.util.ArrayList;
import java.util.List;
import model.portsInternalVariablesAndParameters.variables.PortVariable;
import model.portsInternalVariablesAndParameters.groups.GroupOfVariables;

/**
 * A named group of port variables that belong together (UML exec.puml: Port).
 *
 * Like a connector with several pins: e.g. the port "environment" of the
 * thermal model
 * could group everything that comes from the surroundings. Grouping makes it
 * easier to
 * see which values must be connected together when models are coupled.
 */
public class Port extends GroupOfVariables {
    public final List<PortVariable> variables = new ArrayList<>();
    public final List<Port> ports = new ArrayList<>();

    public Port(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a variable to this port and returns it, so you can write: PortVariable v
     * = port.add(new PortVariable(...));
     */
    public <T extends PortVariable> T add(T variable) {
        variables.add(variable);
        return variable;
    }

    /**
     * Adds a sub-port to this port and returns it, so you can write: Port p =
     * port.add(new Port(...));
     */
    public Port add(Port port) {
        ports.add(port);
        return port;
    }
}
