package model.mic.hybridModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import model.portsInternalVariablesAndParameters.groups.Port;
import model.portsInternalVariablesAndParameters.variables.PortVariable;

/** Validates and stores the wiring for one hybrid model. */
final class ModelConnections {
    private final List<ModelLink> links = new ArrayList<>();

    List<ModelLink> getLinks() {
        return Collections.unmodifiableList(links);
    }

    ModelLink connect(List<PredictiveModel> models, PortVariable output, PortVariable input) {
        PredictiveModel source = variableOwner(models, output);
        PredictiveModel target = variableOwner(models, input);
        ModelLink link = checkedLink(source, output, target, input);
        links.add(link);
        return link;
    }

    List<ModelLink> connect(List<PredictiveModel> models, Port outputPort, Port inputPort) {
        PredictiveModel source = requireOwner(models, model -> allPorts(model).contains(outputPort), "output port");
        PredictiveModel target = requireOwner(models, model -> allPorts(model).contains(inputPort), "input port");

        Map<List<String>, PortVariable> outputs = variablesByPath(outputPort);
        Map<List<String>, PortVariable> inputs = variablesByPath(inputPort);
        if (outputs.isEmpty() || !outputs.keySet().equals(inputs.keySet())) {
            throw new IllegalArgumentException("Ports must have matching, nonempty variable paths");
        }

        // Stage every link first so a failure in a later pair leaves the wiring
        // unchanged.
        List<ModelLink> pending = new ArrayList<>();
        Set<PortVariable> destinations = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<List<String>, PortVariable> entry : outputs.entrySet()) {
            PortVariable output = entry.getValue();
            PortVariable input = inputs.get(entry.getKey());
            // Port ownership alone does not rule out a variable shared with another model.
            variableOwner(models, output);
            variableOwner(models, input);
            if (!destinations.add(input)) {
                throw new IllegalArgumentException("The same input appears at multiple port paths: " + input.name);
            }
            pending.add(checkedLink(source, output, target, input));
        }
        links.addAll(pending);
        return Collections.unmodifiableList(pending);
    }

    private ModelLink checkedLink(PredictiveModel source, PortVariable output,
            PredictiveModel target, PortVariable input) {
        if (source == target) {
            throw new IllegalArgumentException("A link must connect two distinct models");
        }
        String endpoints = source.name + "." + output.name + " -> " + target.name + "." + input.name;
        if (output.inOut != PortVariable.InOut.OUT || input.inOut != PortVariable.InOut.IN) {
            throw new IllegalArgumentException("Expected OUT -> IN: " + endpoints);
        }
        if (output.dataType == null || input.dataType == null
                || !Objects.equals(output.dataType.typeName, input.dataType.typeName)
                || !Objects.equals(output.dataType.precision, input.dataType.precision)
                || !Arrays.equals(output.dataType.shape, input.dataType.shape)) {
            throw new IllegalArgumentException("Incompatible data types: " + endpoints);
        }
        if (output.unit == null || input.unit == null
                || !Objects.equals(output.unit.ucumCode, input.unit.ucumCode)) {
            throw new IllegalArgumentException("Incompatible units: " + endpoints);
        }
        for (ModelLink link : links) {
            if (link.input == input) {
                throw new IllegalArgumentException("Input already connected: " + endpoints);
            }
        }
        return new ModelLink(output, input);
    }

    private static PredictiveModel variableOwner(List<PredictiveModel> models, PortVariable variable) {
        return requireOwner(models, model -> owns(model, variable),
                variable == null ? "null variable" : "variable " + variable.name);
    }

    private static PredictiveModel requireOwner(List<PredictiveModel> models, Predicate<PredictiveModel> owns,
            String endpoint) {
        PredictiveModel owner = null;
        for (PredictiveModel model : models) {
            if (model != null && model != owner && owns.test(model)) {
                if (owner != null) {
                    throw new IllegalArgumentException("Multiple models own " + endpoint);
                }
                owner = model;
            }
        }
        if (owner == null) {
            throw new IllegalArgumentException("No model in this hybrid owns " + endpoint);
        }
        return owner;
    }

    /**
     * Ownership means the exact variable object is declared on the model or in its
     * ports.
     */
    private static boolean owns(PredictiveModel model, PortVariable variable) {
        return variable != null && (model.ports.variables.stream().anyMatch(v -> v == variable)
                || allPorts(model).stream().anyMatch(port -> port.variables.stream().anyMatch(v -> v == variable)));
    }

    private static Set<Port> allPorts(PredictiveModel model) {
        Set<Port> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Port> remaining = new ArrayDeque<>(model.ports.ports);
        while (!remaining.isEmpty()) {
            Port port = remaining.removeFirst();
            if (visited.add(port)) {
                remaining.addAll(port.ports);
            }
        }
        return visited;
    }

    /**
     * Use path segments as keys so a name containing a separator cannot collide
     * with nesting.
     */
    private static Map<List<String>, PortVariable> variablesByPath(Port port) {
        Map<List<String>, PortVariable> variables = new LinkedHashMap<>();
        collectVariables(port, Collections.emptyList(), Collections.newSetFromMap(new IdentityHashMap<>()),
                variables);
        return variables;
    }

    private static void collectVariables(Port port, List<String> path, Set<Port> ancestors,
            Map<List<String>, PortVariable> variables) {
        if (!ancestors.add(port)) {
            throw new IllegalArgumentException("Cyclic port nesting at " + path);
        }
        for (PortVariable variable : port.variables) {
            List<String> variablePath = new ArrayList<>(path);
            variablePath.add(variable.name);
            if (variables.putIfAbsent(variablePath, variable) != null) {
                throw new IllegalArgumentException("Ambiguous variable path: " + variablePath);
            }
        }
        for (Port child : port.ports) {
            List<String> childPath = new ArrayList<>(path);
            childPath.add(child.name);
            collectVariables(child, childPath, ancestors, variables);
        }
        ancestors.remove(port);
    }
}
