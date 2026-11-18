package hybrid;

import java.util.ArrayList;
import java.util.List;

/**
 * Identity card of a MACHINE-LEARNING model (UML diagram ml.puml).
 *
 * An ML model is learned from data instead of being written from physical laws. On top of what
 * every ExecMIC has (name, ports, parameters), it also records:
 *   - the framework it was built with (here: PyTorch),
 *   - its architecture: the ordered list of layers.
 *
 * Important idea: the trained weights are just PARAMETERS of the MIC (LearnedParameter).
 * Because the MIC knows both the layers and their weights, it can compute the network's
 * output itself (see forward()). No PyTorch is needed to run it.
 */
public abstract class MlMIC extends ExecMIC {
    public final String framework;                        // e.g. "PyTorch"
    public final List<Layer> architecture = new ArrayList<>(); // layers, in execution order

    protected MlMIC(String name, String description, String framework) {
        super(name, description);
        this.framework = framework;
    }

    /** Adds the next layer of the network. Called from a subclass constructor. */
    protected void layer(String name, String type) {
        architecture.add(new Layer(name, type));
    }

    /**
     * The "forward pass": sends the input vector x through all layers, one after another,
     * and returns the final output vector.
     */
    protected double[] forward(double[] x) {
        for (Layer layer : architecture) {
            switch (layer.type) {
                case "Linear":
                    // Look up this layer's weights and biases among the MIC parameters by name.
                    x = linear(x, parameter(layer.name + ".weight"), parameter(layer.name + ".bias"));
                    break;
                case "Tanh":
                    // Apply tanh to every element (an "activation function").
                    for (int i = 0; i < x.length; i++) {
                        x[i] = Math.tanh(x[i]);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown layer type " + layer.type);
            }
        }
        return x;
    }

    /**
     * A linear (fully connected) layer: y = W * x + b
     *
     * W has shape [out, in]: one row per output neuron, one column per input value.
     * Each output y[o] is: bias b[o] + sum over i of W[o][i] * x[i].
     */
    private static double[] linear(double[] x, Parameter weight, Parameter bias) {
        int out = weight.shape[0]; // number of output neurons
        int in = weight.shape[1];  // number of inputs
        double[] y = new double[out];
        for (int o = 0; o < out; o++) {
            y[o] = bias.values[o];
            for (int i = 0; i < in; i++) {
                // The matrix is stored row by row, so element [o][i] is at position o * in + i.
                y[o] += weight.values[o * in + i] * x[i];
            }
        }
        return y;
    }

    /** Prints everything ExecMIC prints, plus the ML-specific information. */
    @Override
    public void print() {
        super.print();
        System.out.println("  " + framework + " architecture: " + architecture);
    }
}
