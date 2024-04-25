package adudecalledleo.speedtrading.util;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;

public class PlayerInventoryUtil {
    public static boolean areItemsEqual(ItemStack a, ItemStack b) {
        return ItemStack.areItemsAndComponentsEqual(a, b);
    }

    public static boolean listContainsStack(DefaultedList<ItemStack> list, ItemStack stack) {
        if (stack.isEmpty())
            return true;
        int count = 0;
        for (ItemStack itemStack : list) {
            if (areItemsEqual(itemStack, stack))
                count += itemStack.getCount();
        }
        return count >= stack.getCount();
    }

    public static boolean playerHasStack(PlayerInventory playerInventory, ItemStack stack) {
        return listContainsStack(playerInventory.main, stack);
    }

    public static boolean playerCanPerformTrade(PlayerInventory playerInventory, TradeOffer offer) {
        return playerHasStack(playerInventory, offer.getDisplayedFirstBuyItem()) && playerHasStack(playerInventory, offer.getDisplayedSecondBuyItem());
    }

    public static boolean playerCanAcceptStack(PlayerInventory playerInventory, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.isStackable())
            if (playerInventory.getOccupiedSlotWithRoomForStack(stack) >= 0)
                return true;
        return playerInventory.getEmptySlot() >= 0;
    }
}
