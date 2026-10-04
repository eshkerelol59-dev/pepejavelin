package tech.javelin.client.modules.impl.combat.aura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;

import java.util.concurrent.ThreadLocalRandom;

public class LonyGriefRotation implements RotationAura {

    private float yawSpeedMultiplier = 0.0f;
    private float pitchSpeedMultiplier = 0.0f;

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null || mc.world == null) {
            reset();
            return;
        }

        boolean inWater = mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || target.isTouchingWater() || target.isSubmergedInWater();
        float heightMultiplier = inWater ? 0.65f : 0.6f;
        Vec3d targetVec = new Vec3d(target.getX(), target.getY() + target.getHeight() * heightMultiplier, target.getZ());
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = targetVec.subtract(eyePos);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.hypot(diff.x, diff.z)));

        if (inWater) {
            double torsoMiddleY = (target.getY() + target.getHeight() * 0.5) - mc.player.getEyeY();
            float maxDownPitch = (float) -Math.toDegrees(Math.atan2(torsoMiddleY, Math.hypot(diff.x, diff.z)));
            targetPitch = Math.min(targetPitch, maxDownPitch);
        }

        targetPitch = MathHelper.clamp(targetPitch, -89.0f, 89.0f);

        Rotation currentRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
        Rotation targetRotation = new Rotation(targetYaw, targetPitch);

        float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
        float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();

        yawSpeedMultiplier = MathHelper.lerp(0.12f, yawSpeedMultiplier, 1.0f);
        pitchSpeedMultiplier = MathHelper.lerp(0.12f, pitchSpeedMultiplier, 1.0f);

        float baseYawSpeed = (float) (58.0f + ThreadLocalRandom.current().nextDouble(14));
        float basePitchSpeed = (float) (22.0f + ThreadLocalRandom.current().nextDouble(10));

        float clampedYawDelta = MathHelper.clamp(yawDelta, -baseYawSpeed, baseYawSpeed);
        float clampedPitchDelta = MathHelper.clamp(pitchDelta, -basePitchSpeed, basePitchSpeed);

        float finalYaw = currentRotation.getYaw() + clampedYawDelta;
        float finalPitch = MathHelper.clamp(currentRotation.getPitch() + clampedPitchDelta, -89.0f, 89.0f);

        float gcd = Rotation.gcd();
        finalYaw -= (finalYaw - mc.player.getYaw()) % gcd;
        finalPitch -= (finalPitch - mc.player.getPitch()) % gcd;

        RotationComponent.update(new Rotation(finalYaw, finalPitch), 360f, 360f, 360f, 360f, 1, 1, false);
        aura.lastYaw = finalYaw;
        aura.lastPitch = finalPitch;
    }

    public void reset() {
        yawSpeedMultiplier = 0.0f;
        pitchSpeedMultiplier = 0.0f;
    }
}
