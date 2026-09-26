package io.bendy1234.fasttrading.config;

public enum PriceChangeStopBehavior {
    ON_INCREASE,
    ON_CHANGE,
    DISABLED;

    public boolean shouldStop(int oldPrice, int current) {
        return switch (this) {
            case ON_INCREASE -> current > oldPrice;
            case ON_CHANGE -> oldPrice != current;
            case DISABLED -> false;
        };
    }
}
