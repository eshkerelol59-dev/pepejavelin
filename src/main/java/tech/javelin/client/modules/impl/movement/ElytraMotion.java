package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.impl.combat.Aura;

@ModuleAnnotation(
   name = "ElytraMotion",
   category = Category.MOVEMENT,
   description = "Остановка перед целью на элитрах"
)
public class ElytraMotion extends Module {
   public static final ElytraMotion INSTANCE = new ElytraMotion();
   private boolean waitTarget;

   @EventTarget
   @Native
   private void onPlayerTick(EventUpdate e) {
      if (mc.player != null) {
         LivingEntity target = Aura.INSTANCE.getTarget();
         if (target == null) {
            if (!this.waitTarget) {
               mc.player.setNoGravity(false);
               this.waitTarget = true;
            }

         } else {
            this.waitTarget = false;
            if (mc.player.isGliding() && mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter().add(0.0D, (target.getY() - target.prevY) * 2.0D, 0.0D)) < 2.4000000953674316D && !target.isGliding()) {
               mc.player.setVelocity(0.0D, 0.0D, 0.0D);
               mc.player.setNoGravity(true);
            } else {
               mc.player.setNoGravity(false);
            }

         }
      }
   }

   public void onDisable() {
      super.onDisable();
      if (mc.player != null) {
         this.waitTarget = false;
         mc.player.setNoGravity(false);
      }
   }
}
