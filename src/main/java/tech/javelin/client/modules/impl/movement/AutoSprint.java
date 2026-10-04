package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import tech.javelin.base.events.impl.other.EventTick;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "AutoSprint",
   category = Category.MOVEMENT,
   description = "Автоматически включает спринт"
)
public final class AutoSprint extends Module {
   public static final AutoSprint INSTANCE = new AutoSprint();

   private AutoSprint() {
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      mc.options.sprintKey.setPressed(false);
      mc.player.setSprinting(mc.player.isWalking() && mc.player.canSprint() && !mc.player.isUsingItem() && !mc.player.isBlind() && (!mc.player.hasVehicle() || mc.player.getVehicle().canSprintAsVehicle() && mc.player.getVehicle().isLogicalSideForUpdatingMovement()) && !mc.player.isGliding() && (!mc.player.shouldSlowDown() || mc.player.isSubmergedInWater()) && (!mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) && !mc.player.horizontalCollision && !mc.player.collidedSoftly);
   }
}
