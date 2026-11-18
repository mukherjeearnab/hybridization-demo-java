package hybrid;

/**
 * Identity card of a SIMULATION model (UML diagram sim.puml).
 *
 * A simulation model is built from physical laws (equations). On top of what every ExecMIC has
 * (name, ports, parameters), it also records:
 *   - the modelling language the model was written in (here: Modelica),
 *   - the equation(s) it solves,
 *   - the numerical solver used to compute the solution over time.
 *
 * It is still "abstract": a concrete model such as ThermalModel extends it.
 */
public abstract class SimMIC extends ExecMIC {
    public final String language; // e.g. "Modelica"
    public final String equation; // human-readable form of the model equation
    public final String solver;   // e.g. "Euler"

    protected SimMIC(String name, String description, String language, String equation, String solver) {
        super(name, description); // let ExecMIC store name and description
        this.language = language;
        this.equation = equation;
        this.solver = solver;
    }

    /** Prints everything ExecMIC prints, plus the simulation-specific information. */
    @Override
    public void print() {
        super.print();
        System.out.println("  " + language + " equation: " + equation + "  (solver: " + solver + ")");
    }
}
