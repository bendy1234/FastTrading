package io.bendy1234.fasttrading.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import eu.midnightdust.lib.config.MidnightConfig;
import io.bendy1234.fasttrading.FastTrading;

public class ModMenuSupport implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, FastTrading.MOD_ID);
    }
}
