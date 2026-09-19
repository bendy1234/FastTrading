package io.bendy1234.fasttrading.util;

import io.bendy1234.fasttrading.config.ModConfig;
import io.bendy1234.fasttrading.config.AutofillBehavior;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;

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
        return playerCanAcceptStack(playerInventory.getNonEquipmentItems(), stack);
    }

    public static boolean playerCanAcceptStackAfterAutofill(List<ItemStack> inventory, ItemStack slot0, ItemStack slot1, MerchantOffer offer) {
        List<ItemStack> simulatedInventory = new ArrayList<>(inventory.size());
        inventory.forEach(stack -> simulatedInventory.add(stack.copy()));

        ItemStack paymentA = slot0.copy();
        ItemStack paymentB = slot1.copy();
        boolean backwards = ModConfig.autofillBehavior == AutofillBehavior.DEFAULT;
        if (!paymentA.isEmpty() && !moveToInventory(simulatedInventory, paymentA, backwards)
                || !paymentB.isEmpty() && !moveToInventory(simulatedInventory, paymentB, backwards)) {
            return false;
        }

        if (!paymentA.isEmpty() || !paymentB.isEmpty()) {
            return false;
        }

        fillPaymentSlot(simulatedInventory, paymentA, offer.getItemCostA());
        offer.getItemCostB().ifPresent(cost -> fillPaymentSlot(simulatedInventory, paymentB, cost));
        return playerCanAcceptStack(simulatedInventory, offer.getResult());
    }

    private static boolean playerCanAcceptStack(List<ItemStack> inventory, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        int remaining = stack.getCount();
        for (ItemStack inventoryStack : inventory) {
            if (inventoryStack.isEmpty()) {
                remaining -= stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(inventoryStack, stack)) {
                remaining -= inventoryStack.getMaxStackSize() - inventoryStack.getCount();
            }

            if (remaining <= 0)
                return true;
        }
        return false;
    }

    private static boolean moveToInventory(List<ItemStack> inventory, ItemStack stack, boolean backwards) {
        boolean moved = false;
        int start = backwards ? inventory.size() - 1 : 0;
        int end = backwards ? -1 : inventory.size();
        int step = backwards ? -1 : 1;

        if (stack.isStackable()) {
            for (int i = start; i != end && !stack.isEmpty(); i += step) {
                ItemStack inventoryStack = inventory.get(i);
                if (ItemStack.isSameItemSameComponents(stack, inventoryStack)) {
                    int count = Math.min(stack.getCount(), inventoryStack.getMaxStackSize() - inventoryStack.getCount());
                    if (count > 0) {
                        inventoryStack.grow(count);
                        stack.shrink(count);
                        moved = true;
                    }
                }
            }
        }

        if (!stack.isEmpty()) {
            for (int i = start; i != end; i += step) {
                if (inventory.get(i).isEmpty()) {
                    inventory.set(i, stack.copy());
                    stack.setCount(0);
                    return true;
                }
            }
        }
        return moved;
    }

    private static void fillPaymentSlot(List<ItemStack> inventory, ItemStack payment, ItemCost cost) {
        for (ItemStack inventoryStack : inventory) {
            if (!inventoryStack.isEmpty()
                    && isValidPayment(inventoryStack, cost)
                    && (payment.isEmpty() || ItemStack.isSameItemSameComponents(inventoryStack, payment))) {
                int count = Math.min(inventoryStack.getCount(), inventoryStack.getMaxStackSize() - payment.getCount());
                if (payment.isEmpty()) {
                    payment = inventoryStack.copyWithCount(count);
                } else {
                    payment.grow(count);
                }
                inventoryStack.shrink(count);
                if (payment.getCount() >= payment.getMaxStackSize())
                    return;
            }
        }
    }
}
