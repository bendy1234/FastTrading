package io.bendy1234.fasttrading.duck;

import net.minecraft.world.item.trading.MerchantOffer;

public interface MerchantScreenHooks {
    State fasttrading$computeState();

    MerchantOffer fasttrading$getCurrentTradeOffer();

    int fasttrading$getCurrentTradeOfferIndex();

    int fasttrading$getTradeOfferCount();

    boolean fasttrading$isCurrentTradeOfferBlocked();

    boolean fasttrading$canAutofillMakeRoom();

    void fasttrading$autofillSellSlots();

    void fasttrading$performTrade();

    void fasttrading$clearSellSlots();

    enum State {
        CAN_PERFORM, CLOSED, NO_SELECTION, OUT_OF_STOCK, NOT_ENOUGH_BUY_ITEMS, NO_ROOM_FOR_SELL_ITEM
    }
}
