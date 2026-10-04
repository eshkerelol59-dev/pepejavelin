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

public class SlothRotation implements RotationAura {

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) return;

        FreeLookComponent.setActive(true);

        Box box = target.getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = box.getCenter().subtract(eyePos);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.hypot(diff.x, diff.z)));

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = targetPitch - currentPitch;

        float speed = (float) (20.0 + ThreadLocalRandom.current().nextDouble(10.0));

        float clampedYaw = currentYaw + MathHelper.clamp(yawDelta, -speed, speed);
        float clampedPitch = MathHelper.clamp(currentPitch + MathHelper.clamp(pitchDelta, -speed, speed), -89.5F, 89.5F);

        float gcd = Rotation.gcd();
        clampedYaw -= (clampedYaw - currentYaw) % gcd;
        clampedPitch -= (clampedPitch - currentPitch) % gcd;

        RotationComponent.update(new Rotation(clampedYaw, clampedPitch), 360F, 360F, 360F, 360F, 0, 1, false);
        aura.lastYaw = clampedYaw;
        aura.lastPitch = clampedPitch;
    }
}
