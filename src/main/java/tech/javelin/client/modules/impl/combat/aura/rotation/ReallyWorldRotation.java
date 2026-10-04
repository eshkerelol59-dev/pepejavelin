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

import java.util.concurrent.ThreadLocalRandom;

public class ReallyWorldRotation implements RotationAura {

    private int snapTicks = 0;
    private float smoothedYaw = 0;
    private float smoothedPitch = 0;
    private boolean smoothInitialized = false;

    private float hitJerkYaw = 0.0f;
    private float hitJerkPitch = 0.0f;

    @Override
    public void onAttack() {
        if (mc.player != null) {
            hitJerkYaw = (float) ThreadLocalRandom.current().nextDouble(-0.6, 0.6);
            hitJerkPitch = (float) ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
            snapTicks = 0;
        }
    }

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) return;

        if (!mc.player.isSubmergedInWater()) {
            FreeLookComponent.setActive(true);

            Box box = target.getBoundingBox();
            Vec3d vec = box.getCenter().subtract(mc.player.getEyePos());

            float rawYaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
            float rawPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))), -90F, 90F);

            if (canAttack) {
                snapTicks = ThreadLocalRandom.current().nextInt(1, 3);
            }

            boolean isSnapping = snapTicks > 0;
            if (snapTicks > 0) {
                snapTicks--;
            }

            float cameraYaw = FreeLookComponent.getFreeYaw();
            float cameraPitch = FreeLookComponent.getFreePitch();

            if (!smoothInitialized) {
                smoothedYaw = mc.player.getYaw();
                smoothedPitch = mc.player.getPitch();
                smoothInitialized = true;
            }

            float goalYaw;
            float goalPitch;
            float lerpFactor;

            if (isSnapping) {
                goalYaw = rawYaw;
                goalPitch = rawPitch;
                lerpFactor = 1.0f;
            } else {
                goalYaw = cameraYaw;
                goalPitch = cameraPitch;
                lerpFactor = 1.0f;
            }

            float yawDiff = MathHelper.wrapDegrees(goalYaw - smoothedYaw);
            smoothedYaw += yawDiff * lerpFactor;
            smoothedPitch += (goalPitch - smoothedPitch) * lerpFactor;
            smoothedPitch = MathHelper.clamp(smoothedPitch, -90F, 90F);

            aura.lastYaw = smoothedYaw;
            aura.lastPitch = smoothedPitch;

            hitJerkYaw *= 0.7f;
            hitJerkPitch *= 0.7f;

            float finalYaw = aura.lastYaw + hitJerkYaw;
            float finalPitch = MathHelper.clamp(aura.lastPitch + hitJerkPitch, -90F, 90F);

            float gcd = Rotation.gcd();
            finalYaw -= (finalYaw - mc.player.getYaw()) % gcd;
            finalPitch -= (finalPitch - mc.player.getPitch()) % gcd;

            RotationComponent.update(new Rotation(finalYaw, finalPitch), 360F, 360F, 360F, 360F, 0, 1, false);
        }
    }
}
