package model.mic;

import java.util.List;

import model.description.DataType;
import model.description.Unit;
import model.portsInternalVariablesAndParameters.groups.Port;
import model.portsInternalVariablesAndParameters.variables.PortVariable;
import model.portsInternalVariablesAndParameters.variables.PortVariable.InOut;

/** Standalone checks for individual and whole-port wiring; run the main method. */
public class HybridModelTest {
    public static void main(String[] args) {
        connectsIndividualVariables();
        connectsWholePortsAtomically();
        rejectsAmbiguousOwners();
    }

    private static void connectsIndividualVariables() {
        HybridModel hybrid = new HybridModel("hybrid", "");
        PredictiveModel thermal = hybrid.add(new PredictiveModel("thermal", ""));
        PredictiveModel ml = hybrid.add(new PredictiveModel("ml", ""));
        PortVariable temperature = thermal.ports.add(variable("temperature", InOut.OUT, Unit.DEG_C));
        Port features = ml.ports.add(new Port("features", ""));
        PortVariable tempSim = features.add(variable("temperatureSim", InOut.IN, Unit.DEG_C));
        PortVariable otherInput = features.add(variable("otherInput", InOut.IN, Unit.DEG_C));
        PortVariable tensor = features.add(new PortVariable("tensor", "", InOut.IN,
                new DataType("Real", "float64", 1), Unit.DEG_C));

        rejects(() -> hybrid.connect(temperature, tensor));
        rejects(() -> hybrid.connect(tempSim, temperature));
        PortVariable unowned = variable("unowned", InOut.IN, Unit.DEG_C);
        rejects(() -> hybrid.connect(temperature, unowned));
        PortVariable sameModelInput = thermal.ports.add(variable("feedback", InOut.IN, Unit.DEG_C));
        rejects(() -> hybrid.connect(temperature, sameModelInput));
        ModelLink link = hybrid.connect(thermal.ports.variables.get(0),
                ml.ports.ports.get(0).variables.get(0));
        if (link.output != temperature || link.input != tempSim) {
            throw new AssertionError("Unexpected link endpoints");
        }
        PortVariable secondOutput = thermal.ports.add(variable("secondOutput", InOut.OUT, Unit.DEG_C));
        rejects(() -> hybrid.connect(secondOutput, tempSim));
        hybrid.connect(temperature, otherInput);
        if (hybrid.getLinks().size() != 2) {
            throw new AssertionError("Expected one output to feed two inputs");
        }
    }

    private static void connectsWholePortsAtomically() {
        HybridModel hybrid = new HybridModel("hybrid", "");
        PredictiveModel thermal = hybrid.add(new PredictiveModel("thermal", ""));
        PredictiveModel ml = hybrid.add(new PredictiveModel("ml", ""));
        Port outputs = thermal.ports.add(new Port("outputs", ""));
        Port inputs = ml.ports.add(new Port("inputs", ""));
        PortVariable temperature = outputs.add(variable("temperature", InOut.OUT, Unit.DEG_C));
        PortVariable tempSim = inputs.add(variable("temperature", InOut.IN, Unit.DEG_C));
        Port outputDetail = outputs.add(new Port("detail", ""));
        Port inputDetail = inputs.add(new Port("detail", ""));
        PortVariable power = outputDetail.add(variable("power", InOut.OUT, Unit.WATT));
        rejects(() -> hybrid.connect(outputs, inputs));

        // The first pair matches, but the nested pair has the wrong unit.
        inputDetail.add(variable("power", InOut.IN, Unit.DEG_C));
        rejects(() -> hybrid.connect(outputs, inputs));
        if (!hybrid.getLinks().isEmpty()) {
            throw new AssertionError("A failed whole-port connection must not add partial links");
        }

        PortVariable powerFeature = variable("power", InOut.IN, new Unit("watt", "W"));
        inputDetail.variables.set(0, powerFeature);
        List<ModelLink> links = hybrid.connect(thermal.ports.ports.get(0), ml.ports.ports.get(0));
        if (links.size() != 2 || hybrid.getLinks().size() != 2
                || links.get(0).output != temperature || links.get(0).input != tempSim
                || links.get(1).output != power || links.get(1).input != powerFeature) {
            throw new AssertionError("Expected links to both matching relative paths");
        }
    }

    private static void rejectsAmbiguousOwners() {
        HybridModel hybrid = new HybridModel("hybrid", "");
        PredictiveModel thermal = hybrid.add(new PredictiveModel("thermal", ""));
        PredictiveModel ml = hybrid.add(new PredictiveModel("ml", ""));
        PredictiveModel other = hybrid.add(new PredictiveModel("other", ""));
        Port outputs = thermal.ports.add(new Port("outputs", ""));
        Port inputs = ml.ports.add(new Port("inputs", ""));
        PortVariable temperature = outputs.add(variable("temperature", InOut.OUT, Unit.DEG_C));
        PortVariable tempSim = inputs.add(variable("temperature", InOut.IN, Unit.DEG_C));

        other.ports.add(inputs);
        rejects(() -> hybrid.connect(outputs, inputs));
        other.ports.ports.clear();

        other.ports.add(tempSim);
        rejects(() -> hybrid.connect(temperature, tempSim));
        rejects(() -> hybrid.connect(outputs, inputs));
        if (!hybrid.getLinks().isEmpty()) {
            throw new AssertionError("Ambiguous endpoint ownership must not create links");
        }
    }

    private static PortVariable variable(String name, InOut direction, Unit unit) {
        return new PortVariable(name, "", direction, DataType.REAL64, unit);
    }

    private static void rejects(Runnable connect) {
        try {
            connect.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected invalid connection to be rejected");
    }
}
