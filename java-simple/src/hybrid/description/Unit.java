package hybrid.description;

/**
 * A physical unit (UML valueDescription.puml: Unit).
 *
 * Writing the unit next to every value avoids classic mistakes such as adding watts to degrees.
 *   - symbol:   how humans write it, e.g. "degC"
 *   - ucumCode: the standard machine-readable code (UCUM = Unified Code for Units of Measure), e.g. "Cel"
 */
public class Unit {
    public final String symbol;
    public final String ucumCode;

    public Unit(String symbol, String ucumCode) {
        this.symbol = symbol;
        this.ucumCode = ucumCode;
    }

    // Ready-made units used in this example ("static final" = one shared constant for the whole program).
    public static final Unit WATT = new Unit("W", "W");
    public static final Unit DEG_C = new Unit("degC", "Cel");
    public static final Unit KELVIN = new Unit("K", "K");                 // used for temperature DIFFERENCES
    public static final Unit KELVIN_PER_WATT = new Unit("K/W", "K/W");
    public static final Unit JOULE_PER_KELVIN = new Unit("J/K", "J/K");
    public static final Unit KELVIN_PER_SECOND = new Unit("K/s", "K/s");
    public static final Unit ONE = new Unit("1", "1");                    // "no unit" (dimensionless)

    @Override
    public String toString() {
        return symbol;
    }
}
