package hybrid.mic;

import java.util.ArrayList;
import java.util.List;

import hybrid.parameters.Parameter;
import hybrid.variables.InternalVariable;
import hybrid.variables.Port;
import hybrid.variables.Variable;

/**
 * ExecMIC = "Executable Model Identity Card" (UML diagram exec.puml).
 *
 * A Model Identity Card is like an ID card for a model. It describes:
 *   - WHAT the model is                     (name, description)
 *   - WHICH values go in and out            (ports, each grouping several Variables)
 *   - WHICH values exist inside the model   (internal variables)
 *   - WHICH constants configure it          (parameters)
 * Each of those values is described in detail (type, unit, valid range, ...) by ValueDescription.
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
    public final List<Port> ports = new ArrayList<>();
    public final List<InternalVariable> internalVariables = new ArrayList<>();
    public final List<Parameter> parameters = new ArrayList<>();

    /** "protected" = only subclasses may call this constructor. */
    protected ExecMIC(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Creates a new (empty) port; add variables to it with port.add(...). Called from a subclass constructor. */
    protected Port port(String name, String description) {
        Port p = new Port(name, description);
        ports.add(p);
        return p;
    }

    /** Declares an internal variable. Called from a subclass constructor. */
    protected InternalVariable internal(InternalVariable variable) {
        internalVariables.add(variable);
        return variable;
    }

    /**
     * Declares a parameter. Called from a subclass constructor.
     * "<T extends Parameter> T" means: whatever kind of Parameter you pass in (e.g. a LearnedParameter),
     * you get the same kind back, so you can keep configuring it.
     */
    protected <T extends Parameter> T parameter(T parameter) {
        parameters.add(parameter);
        return parameter;
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
     * Prints the identity card. Subclasses add their own section by overriding printSpecifics().
     */
    public void print() {
        // getClass().getSuperclass() tells which kind of MIC this is, e.g. "SimMIC".
        System.out.println(name + " [" + getClass().getSuperclass().getSimpleName() + "]: " + description);
        printSpecifics();
        System.out.println("  Ports:");
        for (Port p : ports) {
            System.out.println("    <" + p.name + "> " + p.description);
            for (Variable v : p.variables) {
                v.print("      ");
            }
        }
        if (!internalVariables.isEmpty()) {
            System.out.println("  Internal variables:");
            for (InternalVariable v : internalVariables) {
                v.print("    ");
            }
        }
        if (!parameters.isEmpty()) {
            System.out.println("  Parameters:");
            for (Parameter p : parameters) {
                p.print("    ");
            }
        }
        System.out.println();
    }

    /** Hook for subclasses to print their extra information. Does nothing by default. */
    protected void printSpecifics() {
    }
}
