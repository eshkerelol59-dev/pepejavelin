package tech.javelin.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.javelin.base.events.impl.input.EventKey;

@Mixin({Keyboard.class})
public class KeyboardMixin {
   @Inject(
           method = {"onKey"},
           at = {@At("HEAD")}
   )
   public void triggerKeyEvent(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (key != -1) {
         EventManager.call(new EventKey(action, key));
      }
   }
}