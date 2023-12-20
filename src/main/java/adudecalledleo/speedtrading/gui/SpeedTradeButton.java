package adudecalledleo.speedtrading.gui;

import adudecalledleo.speedtrading.ModKeyBindings;
import adudecalledleo.speedtrading.SpeedTradeTimer;
import adudecalledleo.speedtrading.SpeedTrading;
import adudecalledleo.speedtrading.duck.MerchantScreenHooks;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;

import java.util.ArrayList;
import java.util.Locale;

import static adudecalledleo.speedtrading.ModKeyBindings.keyOverrideBlock;

public class SpeedTradeButton extends PressableWidget {
    private static final int PHASE_NONE = 0;
    private static final int PHASE_AUTOFILL = 1;
    private static final int PHASE_PERFORM = 2;

    private final MerchantScreenHooks hooks;
    private int phase;

    public SpeedTradeButton(int x, int y, MerchantScreenHooks hooks) {
        super(x, y, 18, 20, Text.empty());
        this.hooks = hooks;
        phase = PHASE_NONE;
    }

    private boolean checkPrimed() {
        active = phase == PHASE_NONE
                && hooks.speedtrading$computeState() == MerchantScreenHooks.State.CAN_PERFORM
                && (ModKeyBindings.isDown(keyOverrideBlock) || !hooks.speedtrading$isCurrentTradeOfferBlocked());
        return active;
    }

    @Override
    public void onPress() {
        if (checkPrimed()) {
            phase++;
            SpeedTradeTimer.reset();
        }
    }

    private boolean checkState() {
        if (hooks.speedtrading$computeState() != MerchantScreenHooks.State.CAN_PERFORM) {
            phase = PHASE_NONE;
            hooks.speedtrading$clearSellSlots();
            return false;
        }
        return true;
    }

    public void tick() {
        if (phase > PHASE_NONE) {
            active = false;
            if (SpeedTradeTimer.doAction()) {
                if (!checkState())
                    return;
                switch (phase) {
                case PHASE_AUTOFILL:
                    hooks.speedtrading$autofillSellSlots();
                    phase++;
                    break;
                case PHASE_PERFORM:
                    hooks.speedtrading$performTrade();
                default:
                    phase = PHASE_AUTOFILL;
                    break;
                }
                checkState();
            }
        } else {
            checkPrimed();
        }
    }

    private static final Identifier BUTTON_LOCATION = SpeedTrading.id("textures/gui/speedtrade.png");

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // I have no clue if GameRenderer::getPositionTexShader is the same, but it works
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, BUTTON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int v = 36;
        if (checkPrimed()) {
            v = isHovered() ? 18 : 0;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        context.drawTexture(BUTTON_LOCATION, getX(), getY(), 0, v, 20, 18, 20, 54);
        applyTooltip();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    protected void applyTooltip() {
        if (!isHovered())
            return;

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) {
            return;
        }

        ArrayList<OrderedText> textList = new ArrayList<>();
        if (phase > PHASE_NONE) {
            textList.add(Text.translatable("speedtrading.tooltip.in_progress").styled(
                    style -> style.withFormatting(Formatting.BOLD, Formatting.ITALIC, Formatting.DARK_GREEN)
            ).asOrderedText());
        }
        else {
            MerchantScreenHooks.State state = hooks.speedtrading$computeState();
            if (state == MerchantScreenHooks.State.CAN_PERFORM) {
                boolean isBlocked = hooks.speedtrading$isCurrentTradeOfferBlocked();
                boolean isOverriden = ModKeyBindings.isDown(keyOverrideBlock);
                if (isBlocked && !isOverriden) {
                    textList.add(Text.translatable("speedtrading.tooltip.cannot_perform").styled(
                            style -> style.withFormatting(Formatting.BOLD, Formatting.RED)
                    ).asOrderedText());
                    textList.add(Text.translatable("speedtrading.tooltip.blocked").styled(
                            style -> style.withFormatting(Formatting.ITALIC, Formatting.GRAY)
                    ).asOrderedText());
                    if (keyOverrideBlock.isUnbound()) {
                        textList.add(Text.translatable("speedtrading.tooltip.unblock_hint.unbound[0]",
                                Texts.bracketed(Text.translatable(keyOverrideBlock.getTranslationKey())
                                        .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                                .styled(style -> style.withColor(Formatting.GRAY)).asOrderedText());
                        textList.add(Text.translatable("speedtrading.tooltip.unblock_hint.unbound[1]")
                                .styled(style -> style.withColor(Formatting.GRAY)).asOrderedText());
                    } else {
                        textList.add(Text.translatable("speedtrading.tooltip.unblock_hint",
                                Texts.bracketed(Text.translatable(keyOverrideBlock.getBoundKeyTranslationKey())
                                        .styled(style -> style.withBold(true).withColor(Formatting.WHITE))))
                                .styled(style -> style.withColor(Formatting.GRAY)).asOrderedText());
                    }
                } else {
                    textList.add(Text.translatable("speedtrading.tooltip.can_perform").styled(
                            style -> style.withFormatting(Formatting.BOLD, Formatting.GREEN)
                    ).asOrderedText());
                    if (isBlocked) {
                        textList.add(Text.translatable("speedtrading.tooltip.can_perform.unblock_hint")
                                .styled(style -> style.withItalic(true).withColor(Formatting.GRAY)).asOrderedText());
                    }
                }
            } else {
                textList.add(Text.translatable("speedtrading.tooltip.cannot_perform").styled(
                        style -> style.withFormatting(Formatting.BOLD, Formatting.RED)
                ).asOrderedText());
                textList.add(
                        Text.translatable("speedtrading.tooltip." + state.name().toLowerCase(Locale.ROOT)).styled(
                                style -> style.withFormatting(Formatting.ITALIC, Formatting.GRAY)
                        ).asOrderedText());
            }
            textList.add(Text.empty().asOrderedText());
            appendTradeDescription(hooks.speedtrading$getCurrentTradeOffer(), textList);
        }

        screen.setTooltip(textList);
    }

    private static final Style STYLE_GRAY = Style.EMPTY.withColor(Formatting.GRAY);

    private void appendTradeDescription(TradeOffer offer, ArrayList<OrderedText> destList) {
        if (offer == null)
            return;
        ItemStack originalFirstBuyItem = offer.getOriginalFirstBuyItem();
        ItemStack adjustedFirstBuyItem = offer.getAdjustedFirstBuyItem();
        ItemStack secondBuyItem = offer.getSecondBuyItem();
        ItemStack sellItem = offer.getSellItem();
        destList.add(Text.translatable("speedtrading.tooltip.current_trade.is")
                .styled(style -> style.withColor(Formatting.GRAY)).asOrderedText());
        destList.add(createItemStackDescription(originalFirstBuyItem, adjustedFirstBuyItem)
                .fillStyle(STYLE_GRAY).asOrderedText());
        if (!secondBuyItem.isEmpty())
            destList.add(Text.translatable("speedtrading.tooltip.current_trade.and",
                    createItemStackDescription(secondBuyItem))
                    .fillStyle(STYLE_GRAY).asOrderedText());
        destList.add(Text.translatable("speedtrading.tooltip.current_trade.for",
                createItemStackDescription(sellItem))
                .fillStyle(STYLE_GRAY).asOrderedText());
    }

    private MutableText createItemStackDescription(ItemStack stack, ItemStack adjustedStack) {
        if (stack.getCount() == adjustedStack.getCount())
            return createItemStackDescription(stack);
        else {
            return getItemStackName(stack)
                    .append(Text.literal(" "))
                    .append(Text.literal("x" + stack.getCount())
                            .styled(style -> style.withFormatting(Formatting.STRIKETHROUGH, Formatting.RED)))
                    .append(Text.literal(" x" + adjustedStack.getCount())
                            .styled(style -> style.withFormatting(Formatting.BOLD, Formatting.GREEN)));
        }
    }

    private MutableText createItemStackDescription(ItemStack stack) {
        return getItemStackName(stack)
                .append(Text.literal(" x" + stack.getCount()));
    }

    private MutableText getItemStackName(ItemStack stack) {
        return Texts.bracketed(Text.literal("").append(stack.getName()).styled(style -> style.withFormatting(stack.getRarity().formatting)));
    }

}
