package jar.elem.elempoints.plugin.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Number formatting utilities.
 */
public final class NumberFormat {

    private static final NavigableMap<Double, String> SUFFIXES = new TreeMap<>();

    static {
        SUFFIXES.put(1_000D, "k");
        SUFFIXES.put(1_000_000D, "M");
        SUFFIXES.put(1_000_000_000D, "B");
        SUFFIXES.put(1_000_000_000_000D, "T");
        SUFFIXES.put(1_000_000_000_000_000D, "Q");
    }

    private final DecimalFormat fmtSeparated;
    private final DecimalFormat fmtPlain;
    private final DecimalFormat fmtShortDecimal;

    public NumberFormat(Locale locale) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        this.fmtSeparated = new DecimalFormat("#,##0.##", symbols);
        this.fmtPlain = new DecimalFormat("#0.##", new DecimalFormatSymbols(Locale.US));
        this.fmtShortDecimal = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.US));
    }

    /** 1337.5 → "1337.5" */
    public String plain(double value) {
        return fmtPlain.format(value);
    }

    /** 1337.5 → "1,337.5" (locale-dependent separators) */
    public String formatted(double value) {
        return fmtSeparated.format(value);
    }

    /** 1337.5 → "1.3k" */
    public String shorthand(double value) {
        if (value < 0) return "-" + shorthand(-value);
        if (value < 1000) return fmtPlain.format(value);

        Map.Entry<Double, String> entry = SUFFIXES.floorEntry(value);
        if (entry == null) return fmtPlain.format(value);

        double divided = value / entry.getKey();
        divided = Math.floor(divided * 10) / 10.0;
        return fmtShortDecimal.format(divided) + entry.getValue();
    }
}