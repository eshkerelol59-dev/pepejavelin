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

public class WellMineRotation implements RotationAura {

    private float smoothedYaw = 0;
    private float smoothedPitch = 0;

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) return;

        FreeLookComponent.setActive(true);

        Box box = target.getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d diff = box.getCenter().subtract(eyePos);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.hypot(diff.x, diff.z)));

        float jitterYaw = (float) ThreadLocalRandom.current().nextDouble(-0.8, 0.8);
        float jitterPitch = (float) ThreadLocalRandom.current().nextDouble(-0.6, 0.6);

        float finalYaw = targetYaw + jitterYaw;
        float finalPitch = MathHelper.clamp(targetPitch + jitterPitch, -89.5F, 89.5F);

        float gcd = Rotation.gcd();
        finalYaw -= (finalYaw - mc.player.getYaw()) % gcd;
        finalPitch -= (finalPitch - mc.player.getPitch()) % gcd;

        RotationComponent.update(new Rotation(finalYaw, finalPitch), 360F, 360F, 360F, 360F, 0, 1, false);
        aura.lastYaw = finalYaw;
        aura.lastPitch = finalPitch;
    }
}
