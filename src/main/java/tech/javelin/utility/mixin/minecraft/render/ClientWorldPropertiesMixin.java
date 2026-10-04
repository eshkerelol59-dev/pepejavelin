package tech.javelin.utility.mixin.minecraft.render;

import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.javelin.client.modules.impl.render.WorldTime;

@Mixin({Properties.class})
public class ClientWorldPropertiesMixin {
   @Shadow
   private long field_24439;

   @Inject(
      method = {"setTimeOfDay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setTimeOfDayHook(long timeOfDay, CallbackInfo ci) {
      WorldTime tweaks = WorldTime.INSTANCE;
      if (tweaks.isEnabled()) {
         this.field_24439 = (long)(tweaks.timeSetting.getCurrent() * 1000.0F);
         ci.cancel();
      }

   }
}
