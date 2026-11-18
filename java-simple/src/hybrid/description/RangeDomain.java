package hybrid.description;

/**
 * The range of values for which a model is valid (UML valueDescription.puml: ValidityDomain / RangeDomain).
 *
 * Every model is only trustworthy inside certain limits:
 *   - a physical model: e.g. a resistance must be positive, a heater has a maximum power
 *   - an ML model: it only knows the range covered by its training data (the "training envelope");
 *     outside it, its predictions are guesses.
 *
 * min or max may be null, meaning "no limit on that side".
 */
public class RangeDomain {
    public final Double min;        // "Double" (with capital D) can be null, unlike "double"
    public final Double max;
    public final String kind;       // e.g. "physical", "operational", "training envelope"
    public final String description;

    public RangeDomain(Double min, Double max, String kind, String description) {
        this.min = min;
        this.max = max;
        this.kind = kind;
        this.description = description;
    }

    /** True if x lies inside the range. */
    public boolean contains(double x) {
        return (min == null || x >= min) && (max == null || x <= max);
    }

    /** E.g. "[0.0, 500.0] (physical: heater rating)"; an open side is shown as -inf / +inf. */
    @Override
    public String toString() {
        return "[" + (min == null ? "-inf" : min) + ", " + (max == null ? "+inf" : max) + "] ("
                + kind + ": " + description + ")";
    }
}
