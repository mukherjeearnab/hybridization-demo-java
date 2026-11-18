package hybrid.parameters;

import java.util.List;

import hybrid.description.DataType;
import hybrid.description.Unit;

/**
 * A setting chosen BY A HUMAN (or a search algorithm) before training (UML ml.puml: Hyperparameter).
 * Examples: the number of hidden neurons, the activation function, the learning rate.
 *
 * The value is stored as text, because hyperparameters can be numbers ("4", "0.001") or words ("tanh").
 */
public class Hyperparameter extends Parameter {

    public enum Category { architecture, optimizer, regularization, training, dataProcessing, inference }

    public final Category category;
    public final String setting;
    public boolean isTuned;        // true = found by automatic search (e.g. grid search), false = chosen by hand
    public String tuningMethod;    // e.g. "manual", "gridSearch", "randomSearch"

    public Hyperparameter(String name, String description, Category category, String setting) {
        super(name, description, DataType.STRING, Unit.ONE, null);
        this.category = category;
        this.setting = setting;
    }

    @Override
    public String headline() {
        return name + " = " + setting + " - " + description;
    }

    @Override
    protected List<String> details() {
        List<String> d = super.details();
        d.add(0, "category: " + category);
        d.add("tuned: " + isTuned + (tuningMethod == null ? "" : " (" + tuningMethod + ")"));
        return d;
    }
}
