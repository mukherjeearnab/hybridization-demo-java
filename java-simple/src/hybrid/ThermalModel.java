package hybrid;

/**
 * The PHYSICS part of the hybrid model: a Java version of modelica/ThermalModel.mo.
 *
 * Physical idea: a single object (e.g. a heated block) with temperature T.
 *   - A heater adds "power" (watts) to it.
 *   - It loses heat to the surroundings ("ambient" temperature) through a thermal resistance R.
 *   - Its heat capacity C says how slowly its temperature changes.
 *
 * Equation (energy balance):   C * dT/dt = power - (T - ambient) / R
 *
 * "dT/dt" is the rate of change of T. We solve the equation with the simplest method,
 * explicit Euler: compute the current rate of change, then move a small time step dt forward:
 *     T_new = T_old + dt * dT/dt
 *
 * Steady state (when T stops changing): T = ambient + power * R.
 */
public class ThermalModel extends SimMIC {

    // The model's STATE: the only value it remembers from one step to the next.
    private double temperature;

    public ThermalModel() {
        // 1. Fill in the identity card (SimMIC): name, description, language, equation, solver.
        super("ThermalPlant", "First-order lumped thermal model",
                "Modelica", "C * der(T) = power - (T - ambient) / R", "Euler");

        // 2. Declare the ports: what goes in and what comes out.
        port("power", "W", Variable.InOut.IN);
        port("ambient", "degC", Variable.InOut.IN);
        port("temperature", "degC", Variable.InOut.OUT);

        // 3. Declare the parameters (same values as in the .mo file).
        parameter(new Parameter("R", "K/W", 0.5));         // thermal resistance
        parameter(new Parameter("C", "J/K", 100.0));       // heat capacity
        parameter(new Parameter("T_start", "degC", 1000.0)); // temperature at time 0

        // 4. Initialise the state from the parameter T_start.
        temperature = parameter("T_start").value();
    }

    /**
     * Advances the simulation by dt seconds and returns the new temperature.
     *
     * @param power   heater power during this step [W]
     * @param ambient surrounding temperature during this step [degC]
     * @param dt      length of the time step [s]
     */
    public double step(double power, double ambient, double dt) {
        double r = parameter("R").value();
        double c = parameter("C").value();

        // Rate of change of the temperature, from the model equation.
        double dTdt = (power - (temperature - ambient) / r) / c;

        // Explicit Euler step: move forward in time by dt.
        temperature = temperature + dt * dTdt;
        return temperature;
    }

    /** Current temperature, without advancing time. */
    public double temperature() {
        return temperature;
    }
}
