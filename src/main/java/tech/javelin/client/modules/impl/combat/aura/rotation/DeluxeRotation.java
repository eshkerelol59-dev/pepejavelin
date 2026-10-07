package tech.javelin.client.modules.impl.combat.aura.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.RaytracingUtil;
import tech.javelin.utility.game.player.rotation.Rotation;

/**
 * Порт ротации SpookyTime из DELUXE: velocity-based наводка с рандомизацией
 * параметров каждые 25-50 мс, "импульсом" перед ударом (заморозка прицела на
 * 200-250 мс), связкой yaw/pitch скоростей и deadzone у цели.
 */
public class DeluxeRotation implements RotationAura {
   private static final long RANDOMIZE_MIN_MS = 25L;
   private static final long RANDOMIZE_MAX_MS = 50L;

   private final ThreadLocalRandom random = ThreadLocalRandom.current();

   private float attackImpulse;
   private float impulseHoldYaw;
   private float impulseHoldPitch;
   private long impulseTime;
   private long holdDurationMillis;
   private float yawVelocity;
   private float pitchVelocity;
   private float maxSpeed = 120.0F;
   private float easingAngle = 44.0F;
   private float acceleration = 10.0F;
   private float yawPitchCoupling;
   private float deadzone = 0.6F;
   private long lastRandomizeTime;
   private long randomizeInterval;
   private long startTime;
   private long alignedSince;
   private float alignedFactor;
   private float targetHeightFraction;
   private float targetOffsetX;
   private float targetOffsetZ;

   @Override
   public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
      if (mc.player == null || mc.world == null || target == null) {
         return;
      }

      long now = System.currentTimeMillis();
      boolean airborneMaceMode = aura.maceCrits.isEnabled()
            && !mc.player.isOnGround()
            && mc.player.getMainHandStack().isOf(Items.MACE);
      if (this.startTime == 0L) {
         this.startTime = now;
      }
      if (this.targetHeightFraction == 0.0F) {
         this.targetHeightFraction = this.randomFloat(0.24F, 0.72F);
      }
      if (now - this.lastRandomizeTime > this.randomizeInterval) {
         this.lastRandomizeTime = now;
         this.randomizeInterval = (long) this.randomFloat(RANDOMIZE_MIN_MS, RANDOMIZE_MAX_MS);
         this.maxSpeed = this.randomFloat(142.0F, 200.0F);
         this.easingAngle = this.randomFloat(34.0F, 54.0F);
         this.acceleration = this.randomFloat(7.0F, 14.0F);
         this.yawPitchCoupling = this.randomFloat(0.04F, 0.12F) * (this.random.nextBoolean() ? 1.0F : -1.0F);
         this.deadzone = this.randomFloat(0.4F, 0.9F);
      }

      Vec3d eyePos = mc.player.getEyePos();
      Vec3d aimPoint = this.targetPoint(target, airborneMaceMode ? 1.0 : this.targetHeightFraction,
            this.targetOffsetX, this.targetOffsetZ);
      Vec3d aimDirection = aimPoint.subtract(eyePos).normalize();
      float heldYaw = (float) Math.toDegrees(Math.atan2(-aimDirection.x, aimDirection.z));
      float heldPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(aimDirection.y,
            Math.hypot(aimDirection.x, aimDirection.z))), -90.0, 90.0);

      boolean impulseTriggered = false;
      double distanceToTarget = eyePos.distanceTo(
            tech.javelin.utility.math.MultipointUtils.getNearestPoint(target, (double) ranges[0]));
      if (canAttack && distanceToTarget < (double) ranges[0]) {
         this.attackImpulse = this.randomFloat(1.9F, 2.3F);
         this.impulseTime = now;
         this.holdDurationMillis = (long) this.random.nextDouble(200.0, 250.0);
         this.targetHeightFraction = this.randomFloat(0.54F, 0.72F);
         this.targetOffsetX = this.randomFloat(-0.12F, 0.12F);
         this.targetOffsetZ = this.randomFloat(-0.12F, 0.12F);
      }

      if (this.attackImpulse > this.randomFloat(0.43F, 0.75F)) {
         impulseTriggered = true;
         this.attackImpulse = this.randomFloat(0.13F, 0.25F);
      }

      if (impulseTriggered) {
         this.impulseHoldYaw = heldYaw;
         this.impulseHoldPitch = heldPitch;
      } else if (now - this.impulseTime < this.holdDurationMillis) {
         heldYaw = this.impulseHoldYaw;
         heldPitch = this.impulseHoldPitch;
      }

      // прицел уже на цели — плавно гасим скорость, чтобы не дергаться по хитбоксу
      if (RaytracingUtil.rayTrace(mc.player.getRotationVector(), (double) ranges[0] + 1.0, target.getBoundingBox())) {
         if (this.alignedSince == 0L) {
            this.alignedSince = now;
         }
      } else {
         this.alignedSince = 0L;
      }

      float alignedTarget = MathHelper.clamp(((float) (this.alignedSince == 0L ? 0L : now - this.alignedSince) - 250.0F) / 300.0F, 0.0F, 1.0F);
      if (alignedTarget >= this.alignedFactor) {
         this.alignedFactor = alignedTarget;
      } else {
         this.alignedFactor = this.alignedFactor + (alignedTarget - this.alignedFactor) * 0.2F;
      }

      float lastYaw = aura.lastYaw != 0.0F ? aura.lastYaw : mc.player.getYaw();
      float lastPitch = aura.lastPitch != 0.0F ? aura.lastPitch : mc.player.getPitch();
      float yawError = MathHelper.wrapDegrees(heldYaw - lastYaw);
      float pitchError = heldPitch - lastPitch;
      float angularDistance = (float) Math.hypot(yawError, pitchError);
      boolean overlapsPlayer = target.getBoundingBox().intersects(mc.player.getBoundingBox());
      float rampUp = MathHelper.clamp((float) (now - this.startTime) / 220.0F, 0.0F, 1.0F);
      float effectiveSpeed = this.maxSpeed
            * MathHelper.clamp(angularDistance / this.easingAngle, 0.0F, 1.0F)
            * MathHelper.lerp(rampUp, 0.78F, 1.0F)
            * (1.0F - this.alignedFactor * (airborneMaceMode ? 0.35F : 1.0F))
            * (impulseTriggered ? 1.25F : 1.0F)
            * (airborneMaceMode ? 3.2F : 1.0F)
            * (mc.player.handSwingTicks > 0 && !airborneMaceMode ? 0.5F : 1.0F);
      float effectiveAcceleration = this.acceleration * (impulseTriggered ? 4.0F : 1.0F) * (airborneMaceMode ? 2.6F : 1.0F);
      float yawAccelLimit = effectiveAcceleration * (airborneMaceMode ? 0.6F : 1.2F);
      float pitchAccelLimit = effectiveAcceleration * 0.68F;

      this.yawVelocity += MathHelper.clamp(
            effectiveSpeed * (angularDistance < 0.001F ? 0.0F : yawError / angularDistance) * (overlapsPlayer ? 0.5F : 1.0F) - this.yawVelocity,
            -yawAccelLimit, yawAccelLimit);
      this.pitchVelocity += MathHelper.clamp(
            effectiveSpeed * (angularDistance < 0.001F ? 0.0F : pitchError / angularDistance) + this.yawVelocity * this.yawPitchCoupling - this.pitchVelocity,
            -pitchAccelLimit, pitchAccelLimit);
      this.yawVelocity = MathHelper.clamp(this.yawVelocity, -Math.abs(yawError), Math.abs(yawError));
      this.pitchVelocity = MathHelper.clamp(this.pitchVelocity,
            -Math.abs(pitchError) - Math.abs(this.yawVelocity * this.yawPitchCoupling),
            Math.abs(pitchError) + Math.abs(this.yawVelocity * this.yawPitchCoupling));
      if (angularDistance < this.deadzone) {
         this.yawVelocity = 0.0F;
         this.pitchVelocity = 0.0F;
      }

      float gcd = Rotation.gcd();
      float outYaw = lastYaw + (float) Math.round(this.yawVelocity / gcd) * gcd;
      float outPitch = lastPitch + (float) Math.round(this.pitchVelocity / gcd) * gcd;
      outPitch = MathHelper.clamp(outPitch, -90.0F, 90.0F);
      if (Float.isNaN(outYaw) || Float.isNaN(outPitch)) {
         outYaw = lastYaw;
         outPitch = lastPitch;
      }

      Rotation rot = new Rotation(outYaw, outPitch);
      RotationComponent.update(rot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
      aura.lastYaw = rot.getYaw();
      aura.lastPitch = rot.getPitch();
   }

   private Vec3d targetPoint(LivingEntity target, double heightFraction, double offsetX, double offsetZ) {
      double height = target.getHeight();
      double y = MathHelper.clamp(height * heightFraction, 0.1, Math.max(0.1, height - 0.1));
      return target.getPos().add(offsetX, y, offsetZ);
   }

   private float randomFloat(float min, float max) {
      return min + this.random.nextFloat() * (max - min);
   }

   @Override
   public void onAttack() {
   }

   public void hardReset() {
      this.attackImpulse = 0.0F;
      this.impulseHoldYaw = 0.0F;
      this.impulseHoldPitch = 0.0F;
      this.impulseTime = 0L;
      this.holdDurationMillis = 0L;
      this.yawVelocity = 0.0F;
      this.pitchVelocity = 0.0F;
      this.lastRandomizeTime = 0L;
      this.randomizeInterval = 0L;
      this.startTime = 0L;
      this.alignedSince = 0L;
      this.alignedFactor = 0.0F;
      this.targetHeightFraction = 0.0F;
      this.targetOffsetX = 0.0F;
      this.targetOffsetZ = 0.0F;
   }
}
