package io.bendy1234.fasttrading.duck;

import net.minecraft.village.TradeOffer;

public interface MerchantScreenHooks {
    State fasttrading$computeState();

    TradeOffer fasttrading$getCurrentTradeOffer();

    boolean fasttrading$isCurrentTradeOfferBlocked();

    void fasttrading$autofillSellSlots();

    void fasttrading$performTrade();

    void fasttrading$clearSellSlots();

    enum State {
        CAN_PERFORM, CLOSED, NO_SELECTION, OUT_OF_STOCK, NOT_ENOUGH_BUY_ITEMS, NO_ROOM_FOR_SELL_ITEM
    }
}
