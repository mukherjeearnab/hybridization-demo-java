package hybrid.description;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything we know about ONE value of a model (UML valueDescription.puml: ValueDescription).
 *
 * It is the common parent of the three kinds of values a model has:
 *   - Variable          a port value that flows in or out
 *   - InternalVariable  a value computed inside the model, invisible from outside
 *   - Parameter         a constant that configures the model
 *
 * Only name, description, dataType and unit are required (constructor). The other fields are
 * optional (null = "not specified") and are set with chained "with..." methods, e.g.:
 *     new Variable("power", ...).withQuantityKind("Power").withDefault(0.0)
 * Each with-method stores the value and returns the same object, so the next call can follow.
 */
public abstract class ValueDescription {
    // --- required information (set in the constructor) ---
    public final String name;          // identifier, e.g. "power"
    public final String description;   // human explanation
    public final DataType dataType;    // e.g. Real(float64)
    public final Unit unit;            // e.g. W

    // --- optional information (may stay null, set with the with-methods below) ---
    public String quantityKind;        // the physical quantity, e.g. "Power" or "Temperature"
    public String semanticIRI;         // link to a shared vocabulary so tools/people agree on the meaning
    public Double defaultValue;        // start / default value
    public RangeDomain validityDomain; // where the value is valid
    public Uncertainty uncertainty;    // how precise the value is

    protected ValueDescription(String name, String description, DataType dataType, Unit unit) {
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.unit = unit;
    }

    // ---------------------------------------------------------------------------------------
    // Chained setters for the optional information.
    // The return type "<T extends ValueDescription> T" means: "give back this same object, typed
    // as whatever the caller expects". So new Variable(...).withDefault(0.0) is still a Variable,
    // and new Parameter(...).withDefault(0.0) is still a Parameter.
    // ---------------------------------------------------------------------------------------

    public <T extends ValueDescription> T withQuantityKind(String quantityKind) {
        this.quantityKind = quantityKind;
        return self();
    }

    public <T extends ValueDescription> T withSemanticIRI(String semanticIRI) {
        this.semanticIRI = semanticIRI;
        return self();
    }

    public <T extends ValueDescription> T withDefault(double defaultValue) {
        this.defaultValue = defaultValue;
        return self();
    }

    public <T extends ValueDescription> T withValidity(RangeDomain validityDomain) {
        this.validityDomain = validityDomain;
        return self();
    }

    public <T extends ValueDescription> T withUncertainty(Uncertainty uncertainty) {
        this.uncertainty = uncertainty;
        return self();
    }

    /** Returns "this" with the type the caller expects (safe, because it really is that object). */
    @SuppressWarnings("unchecked")
    private <T extends ValueDescription> T self() {
        return (T) this;
    }

    /**
     * One-line summary shown first when printing, e.g. "power : Real(float64) [W] - Applied heater power".
     * Subclasses can override it (Variable adds IN/OUT in front).
     */
    public String headline() {
        return name + " : " + dataType + " [" + unit + "] - " + description;
    }

    /**
     * The optional details as "key: value" texts. Subclasses add their own extra details by
     * overriding this method and calling super.details() first.
     */
    protected List<String> details() {
        List<String> d = new ArrayList<>();
        if (quantityKind != null) d.add("quantity: " + quantityKind);
        if (semanticIRI != null) d.add("IRI: " + semanticIRI);
        if (defaultValue != null) d.add("default: " + defaultValue);
        if (validityDomain != null) d.add("valid: " + validityDomain);
        if (uncertainty != null) d.add("uncertainty: " + uncertainty);
        return d;
    }

    /** Prints the headline and, on the next line, all details (indented by "indent"). */
    public void print(String indent) {
        System.out.println(indent + headline());
        List<String> d = details();
        if (!d.isEmpty()) {
            System.out.println(indent + "    " + String.join(", ", d));
        }
    }
}
