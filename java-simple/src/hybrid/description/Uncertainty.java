package hybrid.description;

/**
 * How uncertain a value is (UML valueDescription.puml: Uncertainty).
 *
 * Example: a temperature sensor with an accuracy of +/- 0.5 degC is described as a "normal"
 * distribution with standard deviation 0.5. Knowing this helps to judge how much to trust
 * the model's results.
 */
public class Uncertainty {
    public final String distribution; // e.g. "normal", "uniform"
    public final double stdDev;       // standard deviation, in the unit of the value
    public final String source;       // where it comes from, e.g. "sensor accuracy"

    public Uncertainty(String distribution, double stdDev, String source) {
        this.distribution = distribution;
        this.stdDev = stdDev;
        this.source = source;
    }

    @Override
    public String toString() {
        return distribution + "(stdDev " + stdDev + ", " + source + ")";
    }
}
