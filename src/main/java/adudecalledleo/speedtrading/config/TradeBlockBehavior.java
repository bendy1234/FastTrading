package adudecalledleo.speedtrading.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.Locale;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public enum TradeBlockBehavior {
    DAMAGEABLE, UNSTACKABLE, DISABLED;

    private static final TradeBlockBehavior[] VALUES = values();

    public static <T extends ConfigData> void registerConfigGuiProvider(Class<T> configClass) {
        final ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        AutoConfig.getGuiRegistry(configClass).registerTypeProvider((i13n, field, config, defaults, registry) ->
                Collections.singletonList(
                        entryBuilder.startSelector(
                                        Text.translatable(i13n),
                                        VALUES,
                                        getUnsafely(field, config, getUnsafely(field, defaults)))
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .setNameProvider(TradeBlockBehavior::toText)
                                .build()
                ), TradeBlockBehavior.class);
    }

    public boolean isBlocked(ItemStack stack) {
        return switch (this) {
            case DAMAGEABLE -> stack.isDamageable();
            case UNSTACKABLE -> !stack.isStackable();
            case DISABLED -> false;
        };
    }

    public Text toText() {
        MutableText text
                = Text.translatable("text.autoconfig.speedtrading.option.tradeBlockBehavior." + this.name().toLowerCase(Locale.ROOT));
        if (this == DISABLED)
            text = text.styled(style -> style.withColor(Formatting.RED));
        return text;
    }
}
