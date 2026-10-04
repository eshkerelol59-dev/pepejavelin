package tech.javelin.client.modules.impl.combat.aura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;

import java.util.concurrent.ThreadLocalRandom;

public class LegitRotation implements RotationAura {
    private float smoothedSpeed = 0.0f;

    public void reset() {
        this.smoothedSpeed = 0.0f;
    }

    private boolean isYawOnTarget(LivingEntity target, float currentYaw) {
        if (mc.player == null || target == null) return false;
        Vec3d eyePos = mc.player.getEyePos();
        Box box = target.getBoundingBox();
        double minDiff = Double.MAX_VALUE;
        double maxDiff = -Double.MAX_VALUE;
        double[] xs = new double[]{box.minX, box.maxX};
        double[] zs = new double[]{box.minZ, box.maxZ};
        for (double x : xs) {
            for (double z : zs) {
                double dx = x - eyePos.x;
                double dz = z - eyePos.z;
                float cornerYaw = (float) ((MathHelper.atan2(dz, dx) * 57.29577951308232) - 90.0);
                float diff = MathHelper.wrapDegrees(cornerYaw - currentYaw);
                if (diff < minDiff) minDiff = diff;
                if (diff > maxDiff) maxDiff = diff;
            }
        }
        return minDiff <= 0.0 && maxDiff >= 0.0;
    }

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) {
            reset();
            return;
        }

        float currentYaw = mc.player.getYaw();
        boolean isOnTarget = isYawOnTarget(target, currentYaw);
        Vec3d targetPoint = target.getBoundingBox().getCenter();

        Vec3d eyePos = mc.player.getEyePos();
        double deltaX = targetPoint.x - eyePos.x;
        double deltaY = targetPoint.y - eyePos.y;
        double deltaZ = targetPoint.z - eyePos.z;
        double distance = Math.hypot(deltaX, deltaZ);
        float targetYaw = (float) ((MathHelper.atan2(deltaZ, deltaX) * 57.29577951308232) - 90.0);
        float targetPitch = (float) ((-MathHelper.atan2(deltaY, distance)) * 57.29577951308232);

        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float absDelta = Math.abs(yawDelta);
        float targetSpeed = 0.0f;
        if (!isOnTarget) {
            float randomFactor = (float) ThreadLocalRandom.current().nextDouble(0.07, 0.12);
            float randomMaxSpeed = (float) ThreadLocalRandom.current().nextDouble(0.9, 1.5);
            targetSpeed = MathHelper.clamp(absDelta * randomFactor, 0.01f, randomMaxSpeed);
        }
        this.smoothedSpeed = MathHelper.lerp(0.12f, this.smoothedSpeed, targetSpeed);
        if (this.smoothedSpeed < 0.0005f) {
            this.smoothedSpeed = 0.0f;
        }

        aura.lastYaw = targetYaw;
        aura.lastPitch = targetPitch;

        RotationComponent.update(
                new Rotation(targetYaw, targetPitch),
                this.smoothedSpeed,
                0.0f,
                this.smoothedSpeed,
                0.0f,
                1,
                1,
                true
        );
    }
}
