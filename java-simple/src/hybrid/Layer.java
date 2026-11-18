package hybrid;

/**
 * One step ("layer") of a neural network. A network is just a list of layers applied one after another.
 *
 * Supported types in this demo:
 *   - "Linear": y = W * x + b  (uses the parameters "<name>.weight" (W) and "<name>.bias" (b))
 *   - "Tanh":   y = tanh(x)    (squashes every value into the range -1 .. 1; has no parameters)
 *
 * Layer names follow PyTorch's nn.Sequential naming: "net.0", "net.1", ...
 *
 * In the UML diagram ml.puml this is the class "Layer".
 */
public class Layer {
    public final String name; // e.g. "net.0"
    public final String type; // e.g. "Linear" or "Tanh"

    public Layer(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /** Used when printing, e.g. "net.0: Linear". */
    @Override
    public String toString() {
        return name + ": " + type;
    }
}
