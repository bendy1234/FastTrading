package io.bendy1234.fasttrading.config;

import net.minecraft.item.ItemStack;

public enum TradeBlockBehavior {
    DAMAGEABLE, UNSTACKABLE, DISABLED;

    public boolean isBlocked(ItemStack stack) {
        return switch (this) {
            case DAMAGEABLE -> stack.isDamageable();
            case UNSTACKABLE -> !stack.isStackable();
            case DISABLED -> false;
        };
    }
}
