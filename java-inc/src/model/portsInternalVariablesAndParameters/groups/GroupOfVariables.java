package model.portsInternalVariablesAndParameters.groups;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of variables that belong together (UML exec.puml:
 * GroupOfVariables).
 */
public abstract class GroupOfVariables {
    public final String name;
    public final String description;

    public GroupOfVariables(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
