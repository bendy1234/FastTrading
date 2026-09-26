package io.bendy1234.fasttrading.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {
    @Entry(min = 0.025)
    public static double ticksBetweenActions = 1;
    @Entry
    public static AutofillBehavior autofillBehavior = AutofillBehavior.DEFAULT;
    @Entry
    public static TradeBlockBehavior tradeBlockBehavior = TradeBlockBehavior.DAMAGEABLE;
    @Entry
    public static PriceChangeStopBehavior priceChangeStopBehavior = PriceChangeStopBehavior.ON_INCREASE;
    @Entry
    public static boolean stopOnNewOffers = false;
}
