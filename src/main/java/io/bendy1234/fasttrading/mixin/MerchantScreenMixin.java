package io.bendy1234.fasttrading.mixin;

import io.bendy1234.fasttrading.config.ModConfig;
import io.bendy1234.fasttrading.duck.MerchantScreenHooks;
import io.bendy1234.fasttrading.gui.SpeedTradeButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.bendy1234.fasttrading.util.PlayerInventoryUtil.canAcceptStack;
import static io.bendy1234.fasttrading.util.PlayerInventoryUtil.canMakeTrade;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> implements MerchantScreenHooks {
    @Shadow
    private int shopItem;
    @Unique
    private Inventory playerInventory;
    @Unique
    private SpeedTradeButton speedTradeButton;

    @SuppressWarnings("DataFlowIssue")
    public MerchantScreenMixin() {
        super(null, null, null);
        throw new RuntimeException("Mixin constructor called?!");
    }

    @Shadow
    protected abstract void postButtonClick();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void capturePlayerInventory(MerchantMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        this.playerInventory = inventory;
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addSpeedTradeButton(CallbackInfo ci) {
        addRenderableWidget(speedTradeButton = new SpeedTradeButton(leftPos + 247, topPos + 36, this));
    }

    @Override
    public MerchantScreenHooks.State fasttrading$computeState() {
        if (minecraft == null || minecraft.gui.screen() != this)
            return State.CLOSED;
        MerchantOffer offer = fasttrading$getCurrentTradeOffer();
        if (offer == null)
            return State.NO_SELECTION;
        if (offer.isOutOfStock())
            return State.OUT_OF_STOCK;
        if (!canAcceptStack(playerInventory, offer.getResult()))
            return State.NO_ROOM_FOR_SELL_ITEM;
        if (canMakeTrade(playerInventory, menu.getSlot(0).getItem(), menu.getSlot(1).getItem(), offer))
            return State.CAN_PERFORM;
        return State.NOT_ENOUGH_BUY_ITEMS;
    }

    @Override
    public MerchantOffer fasttrading$getCurrentTradeOffer() {
        MerchantOffers tradeOffers = menu.getOffers();
        if (shopItem < 0 || shopItem >= tradeOffers.size())
            return null;
        return tradeOffers.get(shopItem);
    }

    @Override
    public int fasttrading$getCurrentTradeOfferIndex() {
        return shopItem;
    }

    @Override
    public int fasttrading$getTradeOfferCount() {
        return menu.getOffers().size();
    }

    @Override
    public boolean fasttrading$isCurrentTradeOfferBlocked() {
        MerchantOffer offer = fasttrading$getCurrentTradeOffer();
        if (offer == null)
            return false;
        return ModConfig.tradeBlockBehavior.isBlocked(offer.getResult());
    }

    @Override
    public void fasttrading$autofillSellSlots() {
        switch (ModConfig.autofillBehavior) {
            case DEFAULT -> postButtonClick();
            case STRICT -> {
                fasttrading$clearSellSlots();
                MerchantOffer recipe = menu.getOffers().get(shopItem);

                fillSlot(0, recipe.getItemCostA().itemStack());
                if (recipe.getItemCostB().isPresent()) {
                    fillSlot(1, recipe.getItemCostB().get().itemStack());
                }
            }
        }
    }

    @Override
    public void fasttrading$performTrade() {
        Slot resultSlot = menu.getSlot(2);
        if (!resultSlot.getItem().isEmpty())
            slotClicked(resultSlot, -1, 0, ContainerInput.QUICK_MOVE);
    }

    @Override
    public void fasttrading$clearSellSlots() {
        slotClicked(null, 0, 0, ContainerInput.QUICK_MOVE);
        slotClicked(null, 1, 0, ContainerInput.QUICK_MOVE);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        speedTradeButton.tick();
    }

    @Unique
    private void fillSlot(int slot, ItemStack item) {
        int count = 0;
        for (int i = 3; i < 39; i++) {
            ItemStack invstack = menu.getSlot(i).getItem();
            if (!ItemStack.isSameItemSameComponents(item, invstack)) {
                continue;
            }

            count += invstack.getCount();

            this.slotClicked(null, i, 0, ContainerInput.PICKUP);
            this.slotClicked(null, slot, 0, ContainerInput.PICKUP);

            if (count > menu.getSlot(slot).getItem().getMaxStackSize()) { // items still on the cursor
                this.slotClicked(null, i, 0, ContainerInput.PICKUP);
                return;
            } else if (count == menu.getSlot(slot).getItem().getMaxStackSize()) {
                return;
            }
        }
    }
}
