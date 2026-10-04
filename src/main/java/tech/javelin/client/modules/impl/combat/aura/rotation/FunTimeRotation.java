package tech.javelin.client.modules.impl.combat.aura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.FreeLookComponent;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;

import java.util.concurrent.ThreadLocalRandom;

public class FunTimeRotation implements RotationAura {

    private float smoothedYaw = 0;
    private float smoothedPitch = 0;
    private boolean initialized = false;

    private float microNoiseYaw = 0;
    private float microNoisePitch = 0;
    private long lastNoiseUpdate = 0;

    private float recoilYaw = 0;
    private float recoilPitch = 0;

    @Override
    public void onAttack() {
        if (mc.player != null) {
            recoilYaw = (float) ThreadLocalRandom.current().nextDouble(-0.35, 0.35);
            recoilPitch = (float) ThreadLocalRandom.current().nextDouble(-0.25, 0.15);
        }
    }

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null || mc.world == null) {
            initialized = false;
            return;
        }

        FreeLookComponent.setActive(true);

        Vec3d eyePos = mc.player.getEyePos();
        Box box = target.getBoundingBox();
        double heightPct = (mc.player.isTouchingWater() || target.isTouchingWater()) ? 0.55 : 0.68;
        Vec3d aimPoint = new Vec3d(
                (box.minX + box.maxX) / 2.0,
                box.minY + (box.maxY - box.minY) * heightPct,
                (box.minZ + box.maxZ) / 2.0
        );

        Rotation targetRot = RotationUtil.fromVec3d(aimPoint.subtract(eyePos));

        long now = System.currentTimeMillis();
        if (now - lastNoiseUpdate > 40) {
            microNoiseYaw = (float) ThreadLocalRandom.current().nextDouble(-0.25, 0.25);
            microNoisePitch = (float) ThreadLocalRandom.current().nextDouble(-0.2, 0.2);
            lastNoiseUpdate = now;
        }

        float desiredYaw = targetRot.getYaw() + microNoiseYaw + recoilYaw;
        float desiredPitch = MathHelper.clamp(targetRot.getPitch() + microNoisePitch + recoilPitch, -89.5f, 89.5f);

        recoilYaw = MathHelper.lerp(0.3f, recoilYaw, 0f);
        recoilPitch = MathHelper.lerp(0.3f, recoilPitch, 0f);

        if (!initialized) {
            smoothedYaw = aura.lastYaw != 0 ? aura.lastYaw : mc.player.getYaw();
            smoothedPitch = aura.lastPitch != 0 ? aura.lastPitch : mc.player.getPitch();
            initialized = true;
        }

        float yawDelta = MathHelper.wrapDegrees(desiredYaw - smoothedYaw);
        float pitchDelta = desiredPitch - smoothedPitch;
        double dist = Math.hypot(yawDelta, pitchDelta);

        float yawFactor = (dist > 30) ? 0.72f : (dist > 15) ? 0.62f : 0.52f;
        float pitchFactor = (dist > 30) ? 0.68f : (dist > 15) ? 0.58f : 0.48f;

        smoothedYaw = smoothedYaw + yawDelta * yawFactor;
        smoothedPitch = MathHelper.clamp(smoothedPitch + pitchDelta * pitchFactor, -89.5f, 89.5f);

        float gcd = Rotation.gcd();
        smoothedYaw -= (smoothedYaw - aura.lastYaw) % gcd;
        smoothedPitch -= (smoothedPitch - aura.lastPitch) % gcd;

        RotationComponent.update(new Rotation(smoothedYaw, smoothedPitch), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
        aura.lastYaw = smoothedYaw;
        aura.lastPitch = smoothedPitch;
    }
}
