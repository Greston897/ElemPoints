package jar.elem.elempoints.plugin.currency;

import jar.elem.elempoints.api.currency.Currency;

import java.util.*;

/**
 * Thread-safe registry of all loaded currencies.
 */
public final class CurrencyRegistry {

    private final Map<String, CurrencyImpl> currencies = new LinkedHashMap<>();
    private CurrencyImpl primary;

    public void register(CurrencyImpl currency) {
        currencies.put(currency.getId(), currency);
        if (currency.isPrimary() && primary == null) {
            primary = currency;
        }
    }

    public void clear() {
        currencies.clear();
        primary = null;
    }

    public CurrencyImpl get(String id) {
        return currencies.get(id);
    }

    public Optional<Currency> find(String id) {
        return Optional.ofNullable(currencies.get(id));
    }

    public boolean has(String id) {
        return currencies.containsKey(id);
    }

    public CurrencyImpl getPrimary() {
        return primary;
    }

    public void ensurePrimary() {
        if (primary == null && !currencies.isEmpty()) {
            primary = currencies.values().iterator().next();
        }
    }

    public Collection<CurrencyImpl> all() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    public int size() {
        return currencies.size();
    }
}