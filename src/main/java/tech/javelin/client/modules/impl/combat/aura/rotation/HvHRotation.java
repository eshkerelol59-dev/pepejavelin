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

public class HvHRotation implements RotationAura {

    @Override
    public void onAttack() {
    }

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null || mc.world == null) {
            return;
        }

        FreeLookComponent.setActive(true);

        Vec3d eyePos = mc.player.getEyePos();
        Box box = target.getBoundingBox();
        Vec3d diff = box.getCenter().subtract(eyePos);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.hypot(diff.x, diff.z)));
        targetPitch = MathHelper.clamp(targetPitch, -89.9F, 89.9F);

        float gcd = Rotation.gcd();
        targetYaw -= (targetYaw - mc.player.getYaw()) % gcd;
        targetPitch -= (targetPitch - mc.player.getPitch()) % gcd;
        targetPitch = MathHelper.clamp(targetPitch, -89.9F, 89.9F);

        aura.lastYaw = targetYaw;
        aura.lastPitch = targetPitch;

        RotationComponent.update(
                new Rotation(targetYaw, targetPitch),
                360.0F,
                360.0F,
                360.0F,
                360.0F,
                0,
                1,
                false
        );

        mc.player.bodyYaw = targetYaw;
        mc.player.headYaw = targetYaw;
    }
}
