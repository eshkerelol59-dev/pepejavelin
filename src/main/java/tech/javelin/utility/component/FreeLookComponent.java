package tech.javelin.utility.component;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import tech.javelin.base.events.impl.player.EventLook;
import tech.javelin.base.events.impl.player.EventRotation;
import tech.javelin.utility.interfaces.IClient;

public class FreeLookComponent implements IClient {
   private static boolean active;
   private static float freeYaw;
   private static float freePitch;

   public FreeLookComponent() {
      EventManager.register(this);
   }

   @EventTarget
   public void onLook(EventLook event) {
      if (active) {
         this.rotateTowards(event.getYaw(), event.getPitch());
         event.cancel();
      }

   }

   @EventTarget
   public void onRotation(EventRotation event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         freePitch = event.getPitch();
      }

   }

   private void rotateTowards(double targetYaw, double targetPitch) {
      freePitch = MathHelper.clamp((float)((double)freePitch + targetPitch * 0.15D), -90.0F, 90.0F);
      freeYaw = (float)((double)freeYaw + targetYaw * 0.15D);
   }

   @Generated
   public static void setActive(boolean active) {
      FreeLookComponent.active = active;
   }

   @Generated
   public static float getFreeYaw() {
      return freeYaw;
   }

   @Generated
   public static float getFreePitch() {
      return freePitch;
   }

   @Generated
   public static void setFreeYaw(float freeYaw) {
      FreeLookComponent.freeYaw = freeYaw;
   }

   @Generated
   public static void setFreePitch(float freePitch) {
      FreeLookComponent.freePitch = freePitch;
   }
}
