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
 * Only name, description, dataType and unit are required. The other fields are optional
 * (null = "not specified") and can simply be assigned after creating the object, e.g.:
 *     power.quantityKind = "Power";
 */
public abstract class ValueDescription {
    // --- required information (set in the constructor) ---
    public final String name;          // identifier, e.g. "power"
    public final String description;   // human explanation
    public final DataType dataType;    // e.g. Real(float64)
    public final Unit unit;            // e.g. W

    // --- optional information (may stay null) ---
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
