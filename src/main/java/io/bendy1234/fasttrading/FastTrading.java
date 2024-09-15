package io.bendy1234.fasttrading;

import eu.midnightdust.lib.config.MidnightConfig;
import io.bendy1234.fasttrading.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FastTrading implements ClientModInitializer {
    public static final String MOD_ID = "fasttrading";
    public static final String MOD_NAME = "Fast Trading";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        MidnightConfig.init(MOD_ID, ModConfig.class);
        ModKeyBindings.register();
        ClientTickEvents.END_WORLD_TICK.register(SpeedTradeTimer::onClientWorldTick);
        LOGGER.info("Waste your hard-earned emeralds with ease!");
    }
}
