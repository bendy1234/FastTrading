package io.bendy1234.fasttrading.mixin;

import io.bendy1234.fasttrading.ModKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At(value = "HEAD"))
    public void updateModKeys(long window, int action, KeyEvent input, CallbackInfo ci) {
        // this forces our key bindings to be updated in screens
        // this allows scancodes to work properly, since you can't poll them via GLFW
        if (minecraft.gui.screen() != null && minecraft.getWindow().handle() == window) {
            if (minecraft.gui.screen().getFocused() instanceof EditBox textFieldWidget) {
                if (textFieldWidget.canConsumeInput()) {
                    // a text field widget is active, don't update keys!
                    return;
                }
            }

            KeyMapping targetBinding = null;
            for (KeyMapping keyBinding : ModKeyBindings.all) {
                if (keyBinding.matches(input)) {
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
