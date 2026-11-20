package model.mic;

import java.util.ArrayList;
import java.util.List;

import model.mic.hybridModel.ModelConnections;
import model.mic.hybridModel.ModelLink;
import model.portsInternalVariablesAndParameters.groups.Port;
import model.portsInternalVariablesAndParameters.variables.PortVariable;

/**
 * An identity card for a hybrid model: it lists the predictive models it
 * combines and describes how their outputs feed each other's inputs.
 *
 * Think of a port as a connector with several pins. You can connect individual
 * pins (port variables), or connect two whole ports if their pins match:
 *
 * <pre>{@code
 * hybrid.connect(thermal.ports.ports.get(0).variables.get(0),
 *         ml.ports.ports.get(0).variables.get(0));
 * // Or, for two ports with matching variable paths:
 * hybrid.connect(thermal.ports.ports.get(0), ml.ports.ports.get(0));
 * }</pre>
 *
 * These connections describe wiring only. They do not run the models, move
 * values, or set the execution order.
 */
public class HybridModel extends ModelMIC {
    public List<PredictiveModel> predictiveModels = new ArrayList<>();
    private final ModelConnections connections = new ModelConnections();

    public HybridModel(String name, String description) {
        super(name, description);
    }

    /**
     * Adds a predictive model directly to this hybrid model and returns it, so you
     * can write:
     * PredictiveModel pm = hybridModel.add(new PredictiveModel(...));
     */
    public <T extends PredictiveModel> T add(T model) {
        predictiveModels.add(model);
        return model;
    }

    /**
     * Returns a read-only view of the wiring. The view reflects later calls to
     * {@code connect}; use those methods rather than editing the list.
     */
    public List<ModelLink> getLinks() {
        return connections.getLinks();
    }

    /**
     * Connects an output to an input on a different predictive model. The hybrid
     * finds each variable's owner among its predictive models. The variables can
     * be declared directly on their models or inside nested ports, but each must
     * belong to exactly one model in the hybrid. Their names need not match.
     *
     * The variables must have the same data type, precision, shape, and UCUM unit
     * code. No conversion is performed. One output can feed several inputs, but
     * each input can have only one incoming link.
     *
     * @param output output variable belonging to the source
     * @param input  input variable belonging to the target
     * @return the new link, also available through {@link #getLinks()}
     * @throws IllegalArgumentException if the endpoints are invalid, incompatible,
     *                                  or the input already has a link
     */
    public ModelLink connect(PortVariable output, PortVariable input) {
        return connections.connect(predictiveModels, output, input);
    }

    /**
     * Connects whole ports by matching variable paths, not list positions.
     * For example, {@code outputs/detail/power} matches
     * {@code inputs/detail/power}: the path inside each port is
     * {@code detail/power}.
     * The selected ports' own names are not part of the match.
     *
     * Each port must belong to exactly one model in this hybrid, and the two
     * owners must differ. The ports must have the same nonempty set of variable
     * paths. Every pair must pass the same checks as an individual connection. If
     * any pair
     * is invalid, no links are added. To connect a subset or variables with
     * different names, use the variable overload.
     *
     * @param outputPort port containing the output variables
     * @param inputPort  port containing the input variables
     * @return a read-only list of the links created by this call
     * @throws IllegalArgumentException if the ports do not match unambiguously,
     *                                  the endpoints are invalid, or any variable
     *                                  pair cannot be connected
     */
    public List<ModelLink> connect(Port outputPort, Port inputPort) {
        return connections.connect(predictiveModels, outputPort, inputPort);
    }
}
