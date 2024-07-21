package adudecalledleo.speedtrading.duck;

import net.minecraft.village.TradeOffer;

public interface MerchantScreenHooks {
    State speedtrading$computeState();

    TradeOffer speedtrading$getCurrentTradeOffer();

    boolean speedtrading$isCurrentTradeOfferBlocked();

    void speedtrading$autofillSellSlots();

    void speedtrading$performTrade();

    void speedtrading$clearSellSlots();

    enum State {
        CAN_PERFORM, CLOSED, NO_SELECTION, OUT_OF_STOCK, NOT_ENOUGH_BUY_ITEMS, NO_ROOM_FOR_SELL_ITEM
    }
}
