package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.math.Vec3d;
import tech.javelin.base.events.impl.player.EventMotion;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.utility.math.Timer;

@ModuleAnnotation(
   name = "GrimGlide",
   category = Category.MOVEMENT,
   description = "Ускорение на элитре без фееров"
)
public class ElytraAccelerate extends Module {
   public static final ElytraAccelerate INSTANCE = new ElytraAccelerate();
   private Timer ticks = new Timer();
   int ticksTwo = 0;

   @EventTarget
   public void onEvent(EventMotion event) {
      if (mc.player != null && mc.world != null && mc.player.isGliding()) {
         ++this.ticksTwo;
         Vec3d pos = mc.player.getPos();
         float yaw = mc.player.getYaw();
         double forward = 0.087D;
         double motion = Math.hypot(mc.player.prevX - mc.player.getX(), mc.player.prevZ - mc.player.getZ()) * 20.0D;
         float valuePidor = 48.0F;
         if (motion >= (double)valuePidor) {
            forward = 0.0D;
            motion = 0.0D;
         }

         double dx = -Math.sin(Math.toRadians((double)yaw)) * forward;
         double dz = Math.cos(Math.toRadians((double)yaw)) * forward;
         mc.player.setVelocity(dx * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F), mc.player.getVelocity().y - 0.019999999552965164D, dz * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F));
         if (this.ticks.finished(50L)) {
            mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            this.ticks.reset();
         }

         mc.player.setVelocity(dx * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F), mc.player.getVelocity().y + 0.01600000075995922D, dz * (double)ThreadLocalRandom.current().nextFloat(1.1F, 1.21F));
      }
   }
}
