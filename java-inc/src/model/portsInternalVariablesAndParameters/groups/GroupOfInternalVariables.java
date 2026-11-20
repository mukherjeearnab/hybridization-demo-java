package model.portsInternalVariablesAndParameters.groups;

import java.util.ArrayList;
import java.util.List;
import model.portsInternalVariablesAndParameters.variables.InternalVariable;
import model.portsInternalVariablesAndParameters.groups.GroupOfVariables;

/**
 * A named group of internal variables that belong together (UML exec.puml:
 * GroupOfInternalVariables).
 *
 * Like a connector with several pins: e.g. the port "environment" of the
 * thermal model
 * could group everything that comes from the surroundings. Grouping makes it
 * easier to
 * see which values must be connected together when models are coupled.
 */
public class GroupOfInternalVariables extends GroupOfVariables {
    public final List<InternalVariable> internalVariables = new ArrayList<>();
    public final List<GroupOfInternalVariables> groups = new ArrayList<>();

    public GroupOfInternalVariables(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a variable to this group and returns it, so you can write:
     * InternalVariable v =
     * group.add(new InternalVariable(...));
     */
    public <T extends InternalVariable> T add(T variable) {
        internalVariables.add(variable);
        return variable;
    }

    /**
     * Adds a sub-group (group of variables / sub-ports) to this group and returns
     * it, so you can write: GroupOfInternalVariables g = group.add(new
     * GroupOfInternalVariables(...));
     */
    public GroupOfInternalVariables add(GroupOfInternalVariables group) {
        groups.add(group);
        return group;
    }
}
