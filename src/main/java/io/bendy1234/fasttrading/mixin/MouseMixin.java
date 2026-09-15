package io.bendy1234.fasttrading.mixin;

import io.bendy1234.fasttrading.ModKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(method = "onButton", at = @At("HEAD"))
    public void updateModKeys(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        // this forces our key bindings to be updated in screens
        if (minecraft.gui.screen() != null && minecraft.getWindow().handle() == window) {
            if (minecraft.gui.screen() instanceof HandledScreenAccessor handledScreen) {
                Slot focusedSlot = handledScreen.callGetHoveredSlot(xpos, ypos);
                if (focusedSlot != null) {
                    // mouse is over a slot, don't update keys!
                    return;
                }
            } else {
                GuiEventListener hoveredElement = minecraft.gui.screen().getChildAt(xpos, ypos).orElse(null);
                if (hoveredElement instanceof AbstractWidget) {
                    // mouse is over something clickable, don't update keys!
                    return;
                }
            }

            KeyMapping targetBinding = null;
            final MouseButtonEvent click = new MouseButtonEvent(0, 0, input);
            for (KeyMapping keyBinding : ModKeyBindings.all) {
                if (keyBinding.matchesMouse(click)) {
                    targetBinding = keyBinding;
                    break;
                }
            }
            if (targetBinding == null)
                return;
            if (action == 0) // mc code also uses 0 for key up
                targetBinding.setDown(false);
            else {
                targetBinding.setDown(true);
                ((KeyBindingAccessor) targetBinding).setClickCount(((KeyBindingAccessor) targetBinding).getClickCount() + 1);
            }
        }
    }
}
