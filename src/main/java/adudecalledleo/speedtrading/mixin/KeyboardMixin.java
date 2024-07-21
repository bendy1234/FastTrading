package adudecalledleo.speedtrading.mixin;

import adudecalledleo.speedtrading.ModKeyBindings;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "HEAD"))
    public void updateModKeys(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        // this forces our key bindings to be updated in screens
        // this allows scancodes to work properly, since you can't poll them via GLFW
        if (client.currentScreen != null && client.getWindow().getHandle() == window) {
            if (client.currentScreen.getFocused() instanceof TextFieldWidget textFieldWidget) {
                if (textFieldWidget.isActive()) {
                    // a text field widget is active, don't update keys!
                    return;
                }
            }

            KeyBinding targetBinding = null;
            for (KeyBinding keyBinding : ModKeyBindings.all) {
                if (keyBinding.matchesKey(key, scancode)) {
                    targetBinding = keyBinding;
                    break;
                }
            }
            if (targetBinding == null)
                return;
            if (action == GLFW_RELEASE)
                targetBinding.setPressed(false);
            else {
                targetBinding.setPressed(true);
                ((KeyBindingAccessor) targetBinding).setTimesPressed(((KeyBindingAccessor) targetBinding).getTimesPressed() + 1);
            }
        }
    }
}
