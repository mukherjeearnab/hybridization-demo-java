package hybrid.models;

import hybrid.description.DataType;
import hybrid.description.RangeDomain;
import hybrid.description.Uncertainty;
import hybrid.description.Unit;
import hybrid.mic.SimMIC;
import hybrid.parameters.Parameter;
import hybrid.variables.InternalVariable;
import hybrid.variables.Port;
import hybrid.variables.Variable;

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

        // 2. Declare the ports (what goes in and what comes out), grouped by where they connect.
        Port heater = port("heater", "Electrical heater feeding the plant");
        Variable power = heater.add(new Variable("power", "Applied heater power",
                Variable.InOut.IN, DataType.REAL64, Unit.WATT));
        power.quantityKind = "Power";
        power.semanticIRI = "http://qudt.org/vocab/quantitykind/Power";
        power.defaultValue = 0.0;
        power.validityDomain = new RangeDomain(0.0, 500.0, "physical", "heater rating");

        Port environment = port("environment", "Surroundings the plant loses heat to");
        Variable ambient = environment.add(new Variable("ambient", "Ambient temperature",
                Variable.InOut.IN, DataType.REAL64, Unit.DEG_C));
        ambient.quantityKind = "Temperature";
        ambient.defaultValue = 20.0;
        ambient.validityDomain = new RangeDomain(-40.0, 60.0, "operational", "indoor/outdoor conditions");
        ambient.uncertainty = new Uncertainty("normal", 0.5, "ambient sensor accuracy");

        Port thermalOutput = port("thermalOutput", "Temperature of the plant");
        Variable temperatureOut = thermalOutput.add(new Variable("temperature", "Simulated plant temperature",
                Variable.InOut.OUT, DataType.REAL64, Unit.DEG_C));
        temperatureOut.quantityKind = "Temperature";
        temperatureOut.validityDomain = new RangeDomain(-273.15, null, "physical", "above absolute zero");

        // 3. Declare internal variables: values computed inside the model.
        InternalVariable rate = internal(new InternalVariable("der(temperature)", "Rate of change of the temperature",
                DataType.REAL64, Unit.KELVIN_PER_SECOND));
        rate.quantityKind = "TemperatureRate";

        // 4. Declare the parameters (same values as in the .mo file).
        Parameter r = parameter(new Parameter("R", "Thermal resistance to ambient",
                DataType.REAL64, Unit.KELVIN_PER_WATT, 0.5));
        r.quantityKind = "ThermalResistance";
        r.validityDomain = new RangeDomain(0.0, null, "physical", "must be positive");
        r.uncertainty = new Uncertainty("normal", 0.05, "calibration from measurements");

        Parameter c = parameter(new Parameter("C", "Heat capacity of the plant",
                DataType.REAL64, Unit.JOULE_PER_KELVIN, 100.0));
        c.quantityKind = "HeatCapacity";
        c.validityDomain = new RangeDomain(0.0, null, "physical", "must be positive");

        Parameter tStart = parameter(new Parameter("T_start", "Temperature at time 0",
                DataType.REAL64, Unit.DEG_C, 1000.0));
        tStart.quantityKind = "Temperature";

        // 5. Initialise the state from the parameter T_start.
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

        // Rate of change of the temperature, from the model equation (the internal variable der(temperature)).
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
