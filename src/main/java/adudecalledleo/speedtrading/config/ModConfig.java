package adudecalledleo.speedtrading.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

// use old name so I don't need to rename translation keys
@Config(name = "speedtrading")
public class ModConfig implements ConfigData {
    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    @ConfigEntry.BoundedDiscrete(max = 100, min = 1)
    @ConfigEntry.Gui.Tooltip(count = 2)
    public int ticksBetweenActions = 1;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @ConfigEntry.Gui.Tooltip
    public TradeBlockBehavior tradeBlockBehavior = TradeBlockBehavior.DAMAGEABLE;
}
