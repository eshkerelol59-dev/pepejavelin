package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.utility.predict.PredictUtils;

@ModuleAnnotation(
   name = "Speed",
   category = Category.MOVEMENT,
   description = "Ускоряет вас возле цели ауры"
)
public class Speed extends Module {
   public static final Speed INSTANCE = new Speed();

   @EventTarget
   private void onUpdate(EventUpdate ignored) {
      if (mc.player != null && mc.world != null) {
         this.collisionSpeed();
      }
   }

   @Native
   private void collisionSpeed() {
      Aura aura = Aura.INSTANCE;
      LivingEntity target = aura.getTarget();
      if (target != null && target != mc.player) {
         Box aABB = mc.player.getBoundingBox().expand(1.2000000476837158D);
         if ((mc.player.isGliding() || target.getBoundingBox().intersects(aABB)) && (!mc.player.isGliding() || !(mc.player.getEyePos().distanceTo(PredictUtils.predict(target, target.getPos(), Aura.INSTANCE.predict.getCurrent() - 0.3F)) > 2.5D) && !(mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter()) > 2.5D))) {
            Vec3d newVelocity = getVec3d(target);
            mc.player.setVelocity(newVelocity);
         }
      }
   }

   @NotNull
   private static Vec3d getVec3d(LivingEntity target) {
      double deltaX = target.getX() - mc.player.getX();
      double deltaZ = target.getZ() - mc.player.getZ();
      if (mc.player.isGliding() && target.isGliding()) {
         deltaX = PredictUtils.predict(target, target.getPos(), Aura.INSTANCE.predict.getCurrent()).x - mc.player.getX();
         deltaZ = PredictUtils.predict(target, target.getPos(), Aura.INSTANCE.predict.getCurrent()).z - mc.player.getZ();
      }

      float targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0D);
      double radYaw = Math.toRadians((double)targetYaw);
      double force = 0.07200000107288361D;
      Vec3d velocity = mc.player.getVelocity();
      return new Vec3d(velocity.x + -Math.sin(radYaw) * 0.07200000107288361D, velocity.y, velocity.z + Math.cos(radYaw) * 0.07200000107288361D);
   }
}
