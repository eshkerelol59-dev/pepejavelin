package tech.javelin.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "NoJumpDelay",
   category = Category.PLAYER,
   description = "Убирает задержку на прыжок"
)
public final class NoDelay extends Module {
   public static final NoDelay INSTANCE = new NoDelay();

   @EventTarget
   public void onUpdate(EventUpdate event) {
      if (mc.player != null && mc.world != null) {
         mc.player.jumpingCooldown = 0;
      }
   }
}
