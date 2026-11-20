package model.portsInternalVariablesAndParameters.groups;

import java.util.ArrayList;
import java.util.List;
import model.portsInternalVariablesAndParameters.variables.Parameter;
import model.portsInternalVariablesAndParameters.groups.GroupOfVariables;

/**
 * A named group of parameters that belong together (UML exec.puml:
 * GroupOfParameters).
 *
 * Like a connector with several pins: e.g. the port "environment" of the
 * thermal model
 * could group everything that comes from the surroundings. Grouping makes it
 * easier to
 * see which values must be connected together when models are coupled.
 */
public class GroupOfParameters extends GroupOfVariables {
    public final List<Parameter> parameters = new ArrayList<>();
    public final List<GroupOfParameters> groups = new ArrayList<>();

    public GroupOfParameters(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a variable to this group and returns it, so you can write:
     * Parameter v =
     * group.add(new Parameter(...));
     */
    public <T extends Parameter> T add(T variable) {
        parameters.add(variable);
        return variable;
    }

    /**
     * Adds a sub-group (group of variables / sub-ports) to this group and returns
     * it, so you can write: GroupOfParameters g = group.add(new
     * GroupOfParameters(...));
     */
    public GroupOfParameters add(GroupOfParameters group) {
        groups.add(group);
        return group;
    }
}
