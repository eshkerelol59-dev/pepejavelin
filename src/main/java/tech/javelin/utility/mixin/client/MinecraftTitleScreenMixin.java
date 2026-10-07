package tech.javelin.utility.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tech.javelin.client.screens.menu.DeluxeMainMenuScreen;

@Mixin(MinecraftClient.class)
public class MinecraftTitleScreenMixin {

    @ModifyVariable(method = "setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), argsOnly = true)
    private Screen javelin$replaceTitleScreen(Screen screen) {
        return screen instanceof TitleScreen && !(screen instanceof DeluxeMainMenuScreen) ? new DeluxeMainMenuScreen() : screen;
    }
}
