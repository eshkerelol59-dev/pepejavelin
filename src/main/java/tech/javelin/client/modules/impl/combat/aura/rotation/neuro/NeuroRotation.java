package tech.javelin.client.modules.impl.combat.aura.rotation.neuro;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.FreeLookComponent;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;

import java.util.concurrent.ThreadLocalRandom;

public class NeuroRotation implements RotationAura {

    private float smoothedYaw = 0;
    private float smoothedPitch = 0;
    private boolean initialized = false;

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) {
            initialized = false;
            return;
        }

        FreeLookComponent.setActive(true);

        Box box = target.getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPoint = box.getCenter();

        double deltaX = targetPoint.x - eyePos.x;
        double deltaY = targetPoint.y - eyePos.y;
        double deltaZ = targetPoint.z - eyePos.z;
        double dist = Math.hypot(deltaX, deltaZ);

        float targetYaw = (float) (MathHelper.atan2(deltaZ, deltaX) * 57.29577951308232 - 90.0);
        float targetPitch = (float) (-MathHelper.atan2(deltaY, dist) * 57.29577951308232);

        if (!initialized) {
            smoothedYaw = aura.lastYaw != 0 ? aura.lastYaw : mc.player.getYaw();
            smoothedPitch = aura.lastPitch != 0 ? aura.lastPitch : mc.player.getPitch();
            initialized = true;
        }

        float noiseYaw = (float) ThreadLocalRandom.current().nextDouble(-0.15, 0.15);
        float noisePitch = (float) ThreadLocalRandom.current().nextDouble(-0.1, 0.1);

        float yawDelta = MathHelper.wrapDegrees(targetYaw - smoothedYaw);
        float pitchDelta = targetPitch - smoothedPitch;

        smoothedYaw = smoothedYaw + (yawDelta + noiseYaw) * 0.55f;
        smoothedPitch = MathHelper.clamp(smoothedPitch + (pitchDelta + noisePitch) * 0.55f, -89.5f, 89.5f);

        float gcd = Rotation.gcd();
        smoothedYaw -= (smoothedYaw - aura.lastYaw) % gcd;
        smoothedPitch -= (smoothedPitch - aura.lastPitch) % gcd;

        RotationComponent.update(new Rotation(smoothedYaw, smoothedPitch), 360F, 360F, 360F, 360F, 0, 1, false);
        aura.lastYaw = smoothedYaw;
        aura.lastPitch = smoothedPitch;
    }
}
