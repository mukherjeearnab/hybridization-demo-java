package hybrid.variables;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of port variables that belong together (UML exec.puml: Port).
 *
 * Like a connector with several pins: e.g. the port "environment" of the thermal model
 * could group everything that comes from the surroundings. Grouping makes it easier to
 * see which values must be connected together when models are coupled.
 */
public class Port {
    public final String name;
    public final String description;
    public final List<Variable> variables = new ArrayList<>();

    public Port(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Adds a variable to this port and returns it, so you can write: Variable v = port.add(new Variable(...)); */
    public <T extends Variable> T add(T variable) {
        variables.add(variable);
        return variable;
    }
}
