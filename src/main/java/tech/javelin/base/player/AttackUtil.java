package tech.javelin.base.player;

import lombok.Generated;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.misc.FreeCam;
import tech.javelin.utility.game.player.PlayerIntersectionUtil;
import tech.javelin.utility.game.player.SimulatedPlayer;
import tech.javelin.utility.interfaces.IClient;
import tech.javelin.utility.math.Timer;

public final class AttackUtil implements IClient {
   private static final Timer attackTimer = new Timer();
   private static int count = 0;

   public static void attackEntity(Entity entity) {
      mc.interactionManager.attackEntity(mc.player, entity);
      mc.player.swingHand(Hand.MAIN_HAND);
      attackTimer.reset();
      ++count;
   }

   public static boolean canAttack() {
      double effectiveJumpHeight = (double)mc.player.getStepHeight();
      Vec3d jumpVec = new Vec3d(0.0D, effectiveJumpHeight, 0.0D);
      Vec3d allowedMovement = mc.player.adjustMovementForCollisions(jumpVec);
      boolean notCrit = mc.player.isInLava() || mc.player.isClimbing() || mc.player.isTouchingWater() && mc.player.isSubmergedInWater() || mc.player.hasStatusEffect(StatusEffects.LEVITATION) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || PlayerIntersectionUtil.isPlayerInBlock(Blocks.COBWEB) || mc.player.isGliding() || mc.player.hasVehicle() || mc.player.getAbilities().flying || allowedMovement.y < (double)mc.player.getStepHeight() - 0.5D && mc.player.isOnGround() || mc.player.getVelocity().y == -0.005D && mc.player.isTouchingWater() || FreeCam.INSTANCE.isEnabled() || mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && Aura.INSTANCE.critsOnlyWithSpace.isEnabled();
      return notCrit || mc.player.fallDistance > 0.0F && mc.player.prevY - mc.player.getY() != 0.12160004615783748D && mc.player.prevY - mc.player.getY() != 0.37663049823865435D && mc.player.prevY - mc.player.getY() != 0.30431682745754074D && mc.player.prevY - mc.player.getY() != 0.3739040364667261D;
   }

   public static boolean hasPreMovementRestrictions(SimulatedPlayer simulatedPlayer) {
      return simulatedPlayer.hasStatusEffect(StatusEffects.BLINDNESS) || simulatedPlayer.hasStatusEffect(StatusEffects.LEVITATION) || PlayerIntersectionUtil.isBoxInBlock(simulatedPlayer.boundingBox, Blocks.COBWEB) || simulatedPlayer.isSubmergedInWater() || simulatedPlayer.isInLava() || simulatedPlayer.isClimbing() || !PlayerIntersectionUtil.canChangeIntoPose(EntityPose.STANDING) && mc.player.isInSneakingPose() || mc.player.getAbilities().flying;
   }

   public static boolean isPlayerInCriticalState() {
      boolean crit = mc.player.fallDistance > 0.0F && ((double)mc.player.fallDistance < 0.08D || !SimulatedPlayer.simulateLocalPlayer(1).onGround);
      return !mc.player.isOnGround() && crit;
   }

   public static boolean isPrePlayerInCriticalState(SimulatedPlayer simulatedPlayer) {
      boolean crit = simulatedPlayer.fallDistance > 0.0F && ((double)simulatedPlayer.fallDistance < 0.08D || !SimulatedPlayer.simulateLocalPlayer(2).onGround);
      return !simulatedPlayer.onGround && crit;
   }

   @Generated
   private AttackUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
