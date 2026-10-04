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

public class SpookyTimeRotation implements RotationAura {

    private float currentSpeedYaw = 40;
    private float currentSpeedPitch = 25;

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) return;

        FreeLookComponent.setActive(true);

        Box box = target.getBoundingBox();
        Vec3d vec = box.getCenter().subtract(mc.player.getEyePos());

        float yawJitter = (float) (Math.cos(System.currentTimeMillis() / 200D) * (6 + ThreadLocalRandom.current().nextDouble(2)));
        float pitchJitter = (float) (Math.sin(System.currentTimeMillis() / 200D) * (6 + ThreadLocalRandom.current().nextDouble(2)));

        float targetSpeedYaw = (float) (52 + ThreadLocalRandom.current().nextDouble(8));
        float targetSpeedPitch = (float) (12 + ThreadLocalRandom.current().nextDouble(6));

        float rawYaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
        float rawPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))), -90F, 90F);

        float smoothFactor = 0.4f;

        currentSpeedYaw += (targetSpeedYaw - currentSpeedYaw) * smoothFactor;
        currentSpeedPitch += (targetSpeedPitch - currentSpeedPitch) * smoothFactor;

        float finalYaw = rawYaw + yawJitter;
        float finalPitch = MathHelper.clamp(rawPitch + pitchJitter, -90F, 90F);

        double torsoMiddleY = (target.getY() + target.getHeight() * 0.4) - mc.player.getEyeY();
        float maxDownPitch = (float) -Math.toDegrees(Math.atan2(torsoMiddleY, Math.hypot(vec.x, vec.z)));
        finalPitch = Math.min(finalPitch, maxDownPitch);
        finalPitch = MathHelper.clamp(finalPitch, -90F, 90F);

        float gcd = Rotation.gcd();
        finalYaw -= (finalYaw - aura.lastYaw) % gcd;
        finalPitch -= (finalPitch - aura.lastPitch) % gcd;

        RotationComponent.update(new Rotation(finalYaw, finalPitch), currentSpeedYaw, currentSpeedPitch, 360F, 360F, 3, 1, false);
        aura.lastYaw = finalYaw;
        aura.lastPitch = finalPitch;
    }
}
