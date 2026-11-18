package hybrid.variables;

import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;

/**
 * A port variable of an ML model (UML ml.puml: Variable with the extra attribute "role").
 *
 * The role tells what the value means for the learning task:
 *   - inputFeature: a value the network receives as input
 *   - target:       the value the network was trained to predict
 *   - label / auxiliary: other kinds (not used in this example)
 */
public class MlVariable extends Variable {

    public enum Role { inputFeature, target, label, auxiliary }

    public final Role role;

    public MlVariable(String name, String description, InOut inOut, Role role, DataType dataType, Unit unit) {
        super(name, description, inOut, dataType, unit);
        this.role = role;
    }

    @Override
    protected List<String> details() {
        List<String> d = super.details(); // keep all the common details ...
        d.add(0, "role: " + role);        // ... and put the role first
        return d;
    }
}
