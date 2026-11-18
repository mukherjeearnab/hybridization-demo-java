package hybrid;

import java.util.ArrayList;
import java.util.List;

/**
 * ExecMIC = "Executable Model Identity Card" (UML diagram exec.puml).
 *
 * A Model Identity Card is like an ID card for a model: it describes WHAT the model is,
 * WHICH values go in and out (ports) and WHICH constants configure it (parameters).
 * This is the information every executable model has, no matter if it is a physics
 * simulation or a neural network.
 *
 * The class is "abstract": you never create an ExecMIC directly. Instead, specific kinds of
 * models extend it:
 *
 *   ExecMIC
 *   ├── SimMIC  (simulation models, e.g. Modelica) ── ThermalModel
 *   ├── MlMIC   (machine-learning models, e.g. PyTorch) ── ResidualMLP
 *   └── HybridModel (combines the two)
 */
public abstract class ExecMIC {
    public final String name;          // short identifier, e.g. "ThermalPlant"
    public final String description;   // one sentence explaining the model

    // Every model gets its own (initially empty) lists; subclasses fill them in their constructor.
    public final List<Variable> ports = new ArrayList<>();
    public final List<Parameter> parameters = new ArrayList<>();

    /** "protected" = only subclasses may call this constructor. */
    protected ExecMIC(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Declares a port (input or output) of the model. Called from a subclass constructor. */
    protected void port(String name, String unit, Variable.InOut inOut) {
        ports.add(new Variable(name, unit, inOut));
    }

    /** Declares a parameter of the model. Called from a subclass constructor. */
    protected void parameter(Parameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Looks up a parameter by its name, e.g. parameter("R").
     * (Two methods may share the name "parameter" because their arguments differ: this is
     * called "overloading" in Java.)
     */
    protected Parameter parameter(String parameterName) {
        for (Parameter p : parameters) {
            if (p.name.equals(parameterName)) {
                return p;
            }
        }
        // Reaching this line means the name was wrong: fail loudly instead of silently continuing.
        throw new IllegalArgumentException("No parameter " + parameterName + " in " + name);
    }

    /**
     * Prints the identity card. Subclasses may add more lines by overriding this method
     * and calling super.print() first.
     */
    public void print() {
        // getClass().getSuperclass() tells which kind of MIC this is, e.g. "SimMIC".
        System.out.println(name + " [" + getClass().getSuperclass().getSimpleName() + "]: " + description);
        for (Variable v : ports) {
            System.out.println("  port  " + v);
        }
        for (Parameter p : parameters) {
            System.out.println("  param " + p);
        }
    }
}
