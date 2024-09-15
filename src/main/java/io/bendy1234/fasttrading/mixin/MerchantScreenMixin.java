package io.bendy1234.fasttrading.mixin;

import io.bendy1234.fasttrading.config.ModConfig;
import io.bendy1234.fasttrading.duck.MerchantScreenHooks;
import io.bendy1234.fasttrading.gui.SpeedTradeButton;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.bendy1234.fasttrading.util.PlayerInventoryUtil.playerCanAcceptStack;
import static io.bendy1234.fasttrading.util.PlayerInventoryUtil.playerCanPerformTrade;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> implements MerchantScreenHooks {
    @Shadow
    private int selectedIndex;
    @Unique
    private PlayerInventory playerInventory;
    @Unique
    private SpeedTradeButton speedTradeButton;

    @SuppressWarnings("DataFlowIssue")
    public MerchantScreenMixin() {
        super(null, null, null);
        throw new RuntimeException("Mixin constructor called?!");
    }

    @Shadow
    protected abstract void syncRecipeIndex();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void capturePlayerInventory(MerchantScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.playerInventory = inventory;
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void addSpeedTradeButton(CallbackInfo ci) {
        addDrawableChild(speedTradeButton = new SpeedTradeButton(x + 247, y + 36, this));
    }

    @Override
    public MerchantScreenHooks.State fasttrading$computeState() {
        if (client == null || client.currentScreen != this)
            return State.CLOSED;
        TradeOffer offer = fasttrading$getCurrentTradeOffer();
        if (offer == null)
            return State.NO_SELECTION;
        if (offer.isDisabled())
            return State.OUT_OF_STOCK;
        ItemStack sellItem = offer.getSellItem();
        if (!playerCanAcceptStack(playerInventory, sellItem))
            return State.NO_ROOM_FOR_SELL_ITEM;
        if (handler.getSlot(2).hasStack() || playerCanPerformTrade(playerInventory, offer))
            return State.CAN_PERFORM;
        return State.NOT_ENOUGH_BUY_ITEMS;
    }

    @Override
    public TradeOffer fasttrading$getCurrentTradeOffer() {
        TradeOfferList tradeOffers = handler.getRecipes();
        if (selectedIndex < 0 || selectedIndex >= tradeOffers.size())
            return null;
        return tradeOffers.get(selectedIndex);
    }

    @Override
    public boolean fasttrading$isCurrentTradeOfferBlocked() {
        TradeOffer offer = fasttrading$getCurrentTradeOffer();
        if (offer == null)
            return false;
        return ModConfig.tradeBlockBehavior.isBlocked(offer.getSellItem());
    }

    @Override
    public void fasttrading$autofillSellSlots() {
        switch (ModConfig.autofillBehavior) {
            case DEFAULT -> syncRecipeIndex();
            case STRICT -> {
                fasttrading$clearSellSlots();
                TradeOffer recipe = handler.getRecipes().get(selectedIndex);

                fillSlot(0, recipe.getFirstBuyItem().itemStack());
                if (recipe.getSecondBuyItem().isPresent()) {
                    fillSlot(1, recipe.getSecondBuyItem().get().itemStack());
                }
            }
        }
    }

    @Override
    public void fasttrading$performTrade() {
        Slot resultSlot = handler.getSlot(2);
        if (!resultSlot.getStack().isEmpty())
            onMouseClick(resultSlot, -1, 0, SlotActionType.QUICK_MOVE);
    }

    @Override
    public void fasttrading$clearSellSlots() {
        onMouseClick(null, 0, 0, SlotActionType.QUICK_MOVE);
        onMouseClick(null, 1, 0, SlotActionType.QUICK_MOVE);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        speedTradeButton.tick();
    }

    @Unique
    private void fillSlot(int slot, ItemStack item) {
        int count = 0;
        for (int i = 3; i < 39; i++) {
            ItemStack invstack = handler.getSlot(i).getStack();
            if (!ItemStack.areItemsAndComponentsEqual(item, invstack)) {
                continue;
            }

            count += invstack.getCount();

            this.onMouseClick(null, i, 0, SlotActionType.PICKUP);
            this.onMouseClick(null, slot, 0, SlotActionType.PICKUP);

            if (count > handler.getSlot(slot).getStack().getMaxCount()) { // items still on the cursor
                this.onMouseClick(null, i, 0, SlotActionType.PICKUP);
                return;
            } else if (count == handler.getSlot(slot).getStack().getMaxCount()) {
                return;
            }
        }
    }
}
