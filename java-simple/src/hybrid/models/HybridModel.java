package hybrid.models;

import hybrid.description.DataType;
import hybrid.description.Uncertainty;
import hybrid.description.Unit;
import hybrid.mic.ExecMIC;
import hybrid.variables.InternalVariable;
import hybrid.variables.Port;
import hybrid.variables.Variable;

/**
 * The HYBRID model: combines the physics model and the ML model.
 *
 *   {power, ambient_temp} --> ThermalModel ------------------------> temp_sim --+
 *            |                                                                  |
 *            +-------------> ResidualMLP(temp_sim, power, ambient) --> residual --+--> temp_pred = temp_sim + residual
 *
 * Idea: the physics model gives a good first guess based on physical laws; the ML model, trained
 * on measured data, learns the remaining error and corrects it. Together they are more accurate
 * than physics alone, and more trustworthy than ML alone.
 *
 * The hybrid model is itself an executable model with its own ports, so it also extends ExecMIC.
 * It CONTAINS the two sub-models (this is called "composition").
 */
public class HybridModel extends ExecMIC {

    // The two sub-models. Each one is created once and keeps its own state.
    public final ThermalModel physics = new ThermalModel();
    public final ResidualMLP ml = new ResidualMLP();

    public HybridModel() {
        super("HybridThermalModel", "Modelica thermal model corrected by a PyTorch MLP");

        // The hybrid model's own ports, as seen from outside (the two sub-models are internal details).
        Port inputs = port("inputs", "Operating conditions");
        inputs.add(new Variable("power", "Applied heater power",
                Variable.InOut.IN, DataType.REAL64, Unit.WATT)).quantityKind = "Power";
        inputs.add(new Variable("ambient_temp", "Ambient temperature",
                Variable.InOut.IN, DataType.REAL64, Unit.DEG_C)).quantityKind = "Temperature";

        Port outputs = port("outputs", "Prediction");
        Variable tempPred = outputs.add(new Variable("temp_pred", "Hybrid temperature prediction",
                Variable.InOut.OUT, DataType.REAL64, Unit.DEG_C));
        tempPred.quantityKind = "Temperature";
        tempPred.uncertainty = new Uncertainty("normal", 0.3, "dominated by the ML residual error");

        // The arrows between the sub-models in the diagram above are the hybrid's internal variables.
        internal(new InternalVariable("temp_sim", "ThermalModel.temperature -> ResidualMLP.temperature_sim",
                DataType.REAL64, Unit.DEG_C)).quantityKind = "Temperature";
        internal(new InternalVariable("residual_correction", "ResidualMLP.predicted_residual -> sum",
                DataType.REAL64, Unit.KELVIN)).quantityKind = "TemperatureDifference";
    }

    /**
     * The result of one hybrid step. Besides the final prediction, it also keeps the two
     * intermediate values so we can see how much the ML model corrected the physics.
     * ("static" nested class = a small helper class that belongs to HybridModel.)
     */
    public static class Result {
        public final double tempSim;            // output of the physics model
        public final double residualCorrection; // output of the ML model
        public final double tempPred;           // final hybrid prediction

        Result(double tempSim, double residualCorrection) {
            this.tempSim = tempSim;
            this.residualCorrection = residualCorrection;
            this.tempPred = tempSim + residualCorrection; // the hybridisation formula
        }
    }

    /**
     * Evaluates the model at the current time WITHOUT advancing the simulation.
     * Used for the very first point (time 0), where the temperature is simply T_start.
     */
    public Result evaluate(double power, double ambientTemp) {
        return combine(physics.temperature(), power, ambientTemp);
    }

    /** Advances the model by dt seconds with the given inputs and returns the hybrid prediction. */
    public Result step(double power, double ambientTemp, double dt) {
        double tempSim = physics.step(power, ambientTemp, dt); // 1. run the physics model
        return combine(tempSim, power, ambientTemp);           // 2. + 3. correct it with ML
    }

    /** Asks the ML model for a correction and adds it to the simulated temperature. */
    private Result combine(double tempSim, double power, double ambientTemp) {
        double residual = ml.predict(tempSim, power, ambientTemp); // 2. ML predicts the error
        return new Result(tempSim, residual);                      // 3. temp_pred = temp_sim + residual
    }
}
