package io.bendy1234.fasttrading.util;

import io.bendy1234.fasttrading.config.ModConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public class PlayerInventoryUtil {
    public static boolean isValidPayment(ItemStack a, ItemCost b) {
        return switch (ModConfig.autofillBehavior) {
            case DEFAULT -> b.test(a);
            case STRICT -> ItemStack.isSameItemSameComponents(a, b.itemStack());
        };
    }

    public static boolean playerCanPay(Inventory inventory, ItemStack slot0, ItemStack slot1, ItemCost cost) {
        int count = 0;

        if (isValidPayment(slot0, cost)) count += slot0.getCount();
        if (isValidPayment(slot1, cost)) count += slot1.getCount();

        for (ItemStack itemStack : inventory.getNonEquipmentItems()) {
            if (isValidPayment(itemStack, cost)) {
                count += itemStack.getCount();
            }
        }

        return count >= cost.count();
    }

    public static boolean playerCanPay(Inventory inventory, ItemCost cost) {
        return playerCanPay(inventory, ItemStack.EMPTY, ItemStack.EMPTY, cost);
    }

    public static boolean playerCanPerformTrade(Inventory playerInventory, ItemStack slot0, ItemStack slot1, MerchantOffer offer) {
        return playerCanPay(playerInventory, slot0, slot1, offer.getItemCostA()) && offer.getItemCostB()
                    .map(cost -> playerCanPay(playerInventory, slot0, slot1, cost))
                    .orElse(true);
    }

    public static boolean playerCanPerformTrade(Inventory playerInventory, MerchantOffer offer) {
        return playerCanPerformTrade(playerInventory, ItemStack.EMPTY, ItemStack.EMPTY, offer);
    }

    public static boolean playerCanAcceptStack(Inventory playerInventory, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        if (stack.isStackable() && playerInventory.getSlotWithRemainingSpace(stack) >= 0)
            return true;

        return playerInventory.getFreeSlot() >= 0;
    }
}
