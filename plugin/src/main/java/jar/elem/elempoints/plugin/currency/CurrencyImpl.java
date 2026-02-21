package jar.elem.elempoints.plugin.currency;

import jar.elem.elempoints.api.currency.Currency;
import jar.elem.elempoints.plugin.config.CurrencyConfig;

/**
 * Implementation of the Currency interface backed by CurrencyConfig.
 */
public final class CurrencyImpl implements Currency {

    private final CurrencyConfig config;
    private boolean enabled;
    private final String id;

    public CurrencyImpl(CurrencyConfig config, String id) {
        this.config = config;
        this.enabled = true;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
    @Override public String getDisplayName() { return config.getDisplayName(); }
    @Override public String getSingular() { return config.getSingular(); }
    @Override public String getPlural() { return config.getPlural(); }
    @Override public String getSymbol() { return config.getSymbol(); }
    @Override public double getDefaultBalance() { return config.getDefaultBalance(); }
    @Override public boolean isPrimary() { return config.isPrimary(); }
    @Override public double getMaxBalance() { return config.getMaxBalance(); }
    @Override public boolean isTransferEnabled() { return config.isTransferEnabled(); }
    @Override public double getTransferFee() { return config.getTransferFeePercent(); }
    @Override public double getMinTransfer() { return config.getMinTransfer(); }
    @Override public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public CurrencyConfig getConfig() { return config; }
}