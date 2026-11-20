package model.portsInternalVariablesAndParameters;

import model.portsInternalVariablesAndParameters.groups.GroupOfParameters;
import model.portsInternalVariablesAndParameters.variables.Parameter;

// import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * A named group of ports that belong together (UML exec.puml: Ports).
 *
 */
public class Parameters {
    public final List<Parameter> parameters = new ArrayList<>();
    public final List<GroupOfParameters> groups = new ArrayList<>();

    public Parameters() {
    }

    /**
     * Adds a parameter directly to this parameters and returns it, so you can
     * write:
     * Parameter p = parameters.add(new Parameter(...));
     */
    public <T extends Parameter> T add(T variable) {
        parameters.add(variable);
        return variable;
    }

    /**
     * Adds a sub-group of parameters to this parameters and
     * returns
     * it, so you can write: GroupOfParameters g = parameters.add(new
     * GroupOfParameters(...));
     */
    public GroupOfParameters add(GroupOfParameters group) {
        groups.add(group);
        return group;
    }
}
