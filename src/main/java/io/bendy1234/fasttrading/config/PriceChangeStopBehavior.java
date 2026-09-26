package io.bendy1234.fasttrading.config;

public enum PriceChangeStopBehavior {
    ON_INCREASE,
    ON_CHANGE,
    DISABLED;

    public boolean shouldStop(int old_price, int current) {
        return switch (this) {
            case ON_INCREASE -> current > old_price;
            case ON_CHANGE -> old_price != current;
            case DISABLED -> false;
        };
    }
}
