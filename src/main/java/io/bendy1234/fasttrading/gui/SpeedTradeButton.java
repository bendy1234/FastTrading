package io.bendy1234.fasttrading.gui;

import io.bendy1234.fasttrading.FastTrading;
import io.bendy1234.fasttrading.ModKeyBindings;
import io.bendy1234.fasttrading.SpeedTradeTimer;
import io.bendy1234.fasttrading.config.AutofillBehavior;
import io.bendy1234.fasttrading.config.ModConfig;
import io.bendy1234.fasttrading.duck.MerchantScreenHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.Locale;

import static io.bendy1234.fasttrading.ModKeyBindings.keyOverrideBlock;

public class SpeedTradeButton extends AbstractButton {

    private static final Identifier BUTTON_LOCATION = FastTrading.id("textures/gui/fasttrading.png");
    private static final Style STYLE_GRAY = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private final MerchantScreenHooks hooks;
    private Phase phase;
    private int tradeOfferIndexAtStart;
    private int tradeOfferCountAtStart;
    private int tradeCostACountAtStart;

    public SpeedTradeButton(int x, int y, MerchantScreenHooks hooks) {
        super(x, y, 18, 20, Component.empty());
        this.hooks = hooks;
        phase = Phase.INACTIVE;
    }

    private boolean checkPrimed() {
        active = phase == Phase.INACTIVE
                && hooks.fasttrading$computeState() == MerchantScreenHooks.State.CAN_PERFORM
                && (ModKeyBindings.isDown(keyOverrideBlock) || !hooks.fasttrading$isCurrentTradeOfferBlocked());
        return active;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (checkPrimed()) {
            phase = Phase.AUTOFILL;
            tradeOfferIndexAtStart = hooks.fasttrading$getCurrentTradeOfferIndex();
            if (ModConfig.stopOnPriceChange)
                tradeCostACountAtStart = hooks.fasttrading$getCurrentTradeOffer().getCostA().getCount();
            if (ModConfig.stopOnNewOffers)
                tradeOfferCountAtStart = hooks.fasttrading$getTradeOfferCount();
            SpeedTradeTimer.start();
        }
    }

    //checks if the player still has items to trade and if he didn't change trade
    private boolean checkState() {
        MerchantScreenHooks.State state = hooks.fasttrading$computeState();
        boolean canContinue = state == MerchantScreenHooks.State.CAN_PERFORM;
        MerchantOffer offer = hooks.fasttrading$getCurrentTradeOffer();
        if (!canContinue
                || tradeOfferIndexAtStart != hooks.fasttrading$getCurrentTradeOfferIndex()
                || ModConfig.stopOnPriceChange && tradeCostACountAtStart != offer.getCostA().getCount()
                || ModConfig.stopOnNewOffers && tradeOfferCountAtStart != hooks.fasttrading$getTradeOfferCount()) {
            phase = Phase.INACTIVE;
            hooks.fasttrading$clearSellSlots();
            SpeedTradeTimer.stop();
            return false;
        }
        return true;
    }

    public void tick() {
        if (phase == Phase.INACTIVE) {
            checkPrimed();
            return;
        }
        active = false;

        while (SpeedTradeTimer.shouldDoAction()) {
            if (!checkState())
                return;

            SpeedTradeTimer.onDoAction();

            if (phase == Phase.AUTOFILL) {
                hooks.fasttrading$autofillSellSlots();
                phase = Phase.TRADE;
            } else {
                hooks.fasttrading$performTrade();
                phase = Phase.AUTOFILL;
            }
        }
        checkState();
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int v = 36;
        if (checkPrimed()) {
            v = isHovered() ? 18 : 0;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_LOCATION, getX(), getY(), 0, v, 20, 18, 20, 54);
        applyTooltip();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    protected void applyTooltip() {
        if (!isHovered())
            return;

        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null) {
            return;
        }

        ArrayList<FormattedCharSequence> textList = new ArrayList<>();
        if (phase != Phase.INACTIVE) {
            textList.add(Component.translatable("fasttrading.tooltip.in_progress").withStyle(
                    style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.DARK_GREEN)
            ).getVisualOrderText());
        } else {
            MerchantScreenHooks.State state = hooks.fasttrading$computeState();
            if (state == MerchantScreenHooks.State.CAN_PERFORM) {
                boolean isBlocked = hooks.fasttrading$isCurrentTradeOfferBlocked();
                boolean isOverriden = ModKeyBindings.isDown(keyOverrideBlock);
                if (isBlocked && !isOverriden) {
                    textList.add(Component.translatable("fasttrading.tooltip.cannot_perform").withStyle(
                            style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.RED)
                    ).getVisualOrderText());
                    textList.add(Component.translatable("fasttrading.tooltip.blocked").withStyle(
                            style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)
                    ).getVisualOrderText());
                    if (keyOverrideBlock.isUnbound()) {
                        textList.add(Component.translatable("fasttrading.tooltip.unblock_hint.unbound[0]",
                                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyOverrideBlock.saveString())
                                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                        textList.add(Component.translatable("fasttrading.tooltip.unblock_hint.unbound[1]")
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    } else {
                        textList.add(Component.translatable("fasttrading.tooltip.unblock_hint",
                                        ComponentUtils.wrapInSquareBrackets(Component.translatable(keyOverrideBlock.saveString())
                                                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.WHITE))))
                                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    }
                } else {
                    textList.add(Component.translatable("fasttrading.tooltip.can_perform").withStyle(
                            style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.GREEN)
                    ).getVisualOrderText());
                    if (isBlocked) {
                        textList.add(Component.translatable("fasttrading.tooltip.can_perform.unblock_hint")
                                .withStyle(style -> style.withItalic(true).withColor(ChatFormatting.GRAY)).getVisualOrderText());
                    }
                }
            } else {
                textList.add(Component.translatable("fasttrading.tooltip.cannot_perform").withStyle(
                        style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.RED)
                ).getVisualOrderText());
                textList.add(
                        Component.translatable("fasttrading.tooltip." + state.name().toLowerCase(Locale.ROOT)).withStyle(
                                style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)
                        ).getVisualOrderText());
                if (state == MerchantScreenHooks.State.NOT_ENOUGH_BUY_ITEMS && ModConfig.autofillBehavior == AutofillBehavior.STRICT) {
                    textList.add(Component.translatable("fasttrading.tooltip.strict_autofill_config_hint").withStyle(
                            style -> style.applyFormats(ChatFormatting.ITALIC, ChatFormatting.GRAY)).getVisualOrderText());
                }
            }
            textList.add(Component.empty().getVisualOrderText());
            appendTradeDescription(hooks.fasttrading$getCurrentTradeOffer(), textList);
        }
        var tt = Tooltip.create(null);
        tt.cachedTooltip = textList;
        tt.splitWithLanguage = Language.getInstance();
        this.setTooltip(tt);
    }

    private void appendTradeDescription(MerchantOffer offer, ArrayList<FormattedCharSequence> destList) {
        if (offer == null)
            return;
        ItemStack originalFirstBuyItem = offer.getBaseCostA();
        ItemStack adjustedFirstBuyItem = offer.getCostA();
        ItemStack secondBuyItem = offer.getCostB();
        ItemStack sellItem = offer.getResult();
        destList.add(Component.translatable("fasttrading.tooltip.current_trade.is")
                .withStyle(style -> style.withColor(ChatFormatting.GRAY)).getVisualOrderText());
        destList.add(createItemStackDescription(originalFirstBuyItem, adjustedFirstBuyItem)
                .withStyle(STYLE_GRAY).getVisualOrderText());
        if (!secondBuyItem.isEmpty())
            destList.add(Component.translatable("fasttrading.tooltip.current_trade.and",
                            createItemStackDescription(secondBuyItem))
                    .withStyle(STYLE_GRAY).getVisualOrderText());
        destList.add(Component.translatable("fasttrading.tooltip.current_trade.for",
                        createItemStackDescription(sellItem))
                .withStyle(STYLE_GRAY).getVisualOrderText());
    }

    private MutableComponent createItemStackDescription(ItemStack stack, ItemStack adjustedStack) {
        if (stack.getCount() == adjustedStack.getCount())
            return createItemStackDescription(stack);
        else {
            return getItemStackName(stack)
                    .append(Component.literal(" "))
                    .append(Component.literal("x" + stack.getCount())
                            .withStyle(style -> style.applyFormats(ChatFormatting.STRIKETHROUGH, ChatFormatting.RED)))
                    .append(Component.literal(" x" + adjustedStack.getCount())
                            .withStyle(style -> style.applyFormats(ChatFormatting.BOLD, ChatFormatting.GREEN)));
        }
    }

    private MutableComponent createItemStackDescription(ItemStack stack) {
        return getItemStackName(stack)
                .append(Component.literal(" x" + stack.getCount()));
    }

    private MutableComponent getItemStackName(ItemStack stack) {
        return ComponentUtils.wrapInSquareBrackets(Component.literal("").append(stack.getHoverName()).withStyle(style -> style.applyFormat(stack.getRarity().color())));
    }

    public enum Phase {
        INACTIVE,
        AUTOFILL,
        TRADE
    }
}
