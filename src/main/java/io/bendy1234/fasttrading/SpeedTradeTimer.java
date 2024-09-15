package io.bendy1234.fasttrading;

import io.bendy1234.fasttrading.config.ModConfig;
import net.minecraft.client.world.ClientWorld;

public class SpeedTradeTimer {
    private static int counter;

    public static void reset() {
        counter = 0;
    }

    public static int getCounterMax() {
        return ModConfig.ticksBetweenActions;
    }

    public static boolean doAction() {
        return counter == getCounterMax();
    }

    public static void onClientWorldTick(ClientWorld world) {
        counter++;
        if (counter > getCounterMax())
            reset();
    }
}
