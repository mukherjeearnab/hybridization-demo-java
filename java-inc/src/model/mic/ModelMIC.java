package model.mic;

import model.portsInternalVariablesAndParameters.InternalVariables;
import model.portsInternalVariablesAndParameters.Parameters;
import model.portsInternalVariablesAndParameters.Ports;

/**
 * ModelMIC = "Abstract Model Identity Card" (UML diagram exec.puml).
 *
 * A Model Identity Card is like an ID card for a model. It describes:
 * - WHAT the model is (name, description)
 * - WHICH values go in and out (ports, each grouping several Variables)
 * - WHICH values exist inside the model (internal variables)
 * - WHICH constants configure it (parameters)
 * Each of those values is described in detail (type, unit, valid range, ...) by
 * ValueDescription.
 * This is the information every executable model has, no matter if it is a
 * physics
 * simulation or a neural network.
 *
 * The class is "abstract": you never create a ModelMIC directly. Instead,
 * specific kinds of
 * models extend it:
 *
 * ModelMIC
 * ├── SimModel (simulation models, e.g. Modelica) ── ThermalModel
 * ├── MlModel (machine-learning models, e.g. PyTorch) ── ResidualMLP
 * └── HybridModel (combines the two)
 */
public abstract class ModelMIC {
    public final String name; // short identifier, e.g. "ThermalPlant"
    public final String description; // one sentence explaining the model

    // Every model gets its own (initially empty) lists; subclasses fill them in
    // their constructor.
    /** Connection endpoints are declared here, either directly or inside named ports. */
    public final Ports ports = new Ports();
    public final InternalVariables internalVariables = new InternalVariables();
    public final Parameters parameters = new Parameters();

    /** "protected" = only subclasses may call this constructor. */
    protected ModelMIC(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
