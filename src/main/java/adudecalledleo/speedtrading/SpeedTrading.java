package adudecalledleo.speedtrading;

import adudecalledleo.speedtrading.config.ModConfig;
import adudecalledleo.speedtrading.config.TradeBlockBehavior;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeedTrading implements ClientModInitializer {
    public static final String MOD_ID = "fasttrading";
    public static final String MOD_NAME = "Fast Trading";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.of("speedtrading", path);
    }

    @Override
    public void onInitializeClient() {
        TradeBlockBehavior.registerConfigGuiProvider(ModConfig.class);
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        ModKeyBindings.register();
        ClientTickEvents.END_WORLD_TICK.register(SpeedTradeTimer::onClientWorldTick);
        LOGGER.info("Waste your hard-earned emeralds with ease!");
    }
}
