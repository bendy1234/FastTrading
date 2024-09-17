package io.bendy1234.fasttrading;

import io.bendy1234.fasttrading.config.ModConfig;
import net.minecraft.client.world.ClientWorld;

public class SpeedTradeTimer {
    private static boolean active;
    public static double counter;

    public static void start() {
        active = true;
        counter = 0;
    }

    public static void stop() {
        active = false;
    }

    public static boolean shouldDoAction() {
        return counter > 1;
    }

    public static void onDoAction() {
        counter--;
    }

    public static void onClientWorldTick(ClientWorld world) {
        if (active)
            counter += 1 / ModConfig.ticksBetweenActions;
    }
}
