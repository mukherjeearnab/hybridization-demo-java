package model.portsInternalVariablesAndParameters;

import java.util.ArrayList;
import java.util.List;

import model.portsInternalVariablesAndParameters.groups.GroupOfInternalVariables;
import model.portsInternalVariablesAndParameters.variables.InternalVariable;

/**
 * The top-level collection of values computed inside a model.
 *
 * An internal variable can be declared here directly or in a named group.
 * Unlike port variables, these values are not connection endpoints.
 */
public class InternalVariables {
    public final List<InternalVariable> internalVariables = new ArrayList<>();
    public final List<GroupOfInternalVariables> groups = new ArrayList<>();

    public InternalVariables() {
    }

    /**
     * Adds a variable directly to this group and returns it, so you can write:
     * Variable v = group.add(new Variable(...));
     */
    public <T extends InternalVariable> T add(T variable) {
        internalVariables.add(variable);
        return variable;
    }

    /**
     * Adds a sub-group (group of variables / sub-ports) to this internal variables
     * and returns
     * it, so you can write: GroupOfInternalVariables g = group.add(new
     * GroupOfInternalVariables(...));
     */
    public GroupOfInternalVariables add(GroupOfInternalVariables group) {
        groups.add(group);
        return group;
    }
}
