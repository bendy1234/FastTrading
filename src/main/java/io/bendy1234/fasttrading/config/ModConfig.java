package io.bendy1234.fasttrading.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModConfig extends MidnightConfig {
    @Entry(isSlider = true, min = 1, max = 100)
    public static int ticksBetweenActions = 1;
    @Entry
    public static AutofillBehavior autofillBehavior = AutofillBehavior.DEFAULT;
    @Entry
    public static TradeBlockBehavior tradeBlockBehavior = TradeBlockBehavior.DAMAGEABLE;
}
