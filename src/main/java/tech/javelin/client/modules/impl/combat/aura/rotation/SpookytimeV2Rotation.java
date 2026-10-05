package tech.javelin.client.modules.impl.combat.aura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;

import java.util.concurrent.ThreadLocalRandom;

public class SpookytimeV2Rotation implements RotationAura {
    protected float lastYaw = 0.0f;
    protected float lastPitch = 0.0f;
    private boolean initialized = false;
    private int lastTargetId = -1;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private float errorYawOffset = 0.0f;
    private float errorPitchOffset = 0.0f;
    private int errorTicks = 0;
    private int nextErrorChange = 20;

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) {
            return;
        }

        if (!this.initialized) {
            this.lastYaw = aura.lastYaw != 0 ? aura.lastYaw : mc.player.getYaw();
            this.lastPitch = aura.lastPitch != 0 ? aura.lastPitch : mc.player.getPitch();
            this.initialized = true;
        }

        int currentTargetId = target.getId();
        if (currentTargetId != this.lastTargetId) {
            this.lastTargetId = currentTargetId;
            this.resetHumanError();
        }

        ++this.errorTicks;
        if (this.errorTicks >= this.nextErrorChange) {
            this.errorTicks = 0;
            this.nextErrorChange = 15 + this.random.nextInt(25);
            this.errorYawOffset = (this.random.nextFloat() - 0.5f) * 1.8f;
            this.errorPitchOffset = (this.random.nextFloat() - 0.5f) * 0.9f;
        }

        Rotation angle = RotationUtil.fromVec3d(target.getBoundingBox().getCenter().subtract(mc.player.getEyePos()));
        float targetYaw = angle.getYaw() + this.errorYawOffset;
        float targetPitch = angle.getPitch() + this.errorPitchOffset;

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
        float deltaPitch = targetPitch - this.lastPitch;

        float humanSpeed = 0.28f + this.random.nextFloat() * 0.12f;
        double distance = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
        humanSpeed = distance < 3.0 ? 0.38f + this.random.nextFloat() * 0.1f : 0.22f + this.random.nextFloat() * 0.08f;

        float desiredYaw = this.lastYaw + deltaYaw * humanSpeed;
        float desiredPitch = this.lastPitch + deltaPitch * humanSpeed;

        float maxStep = 22.0f + this.random.nextFloat() * 6.0f;
        desiredYaw = this.lastYaw + MathHelper.clamp(desiredYaw - this.lastYaw, -maxStep, maxStep);
        desiredPitch = this.lastPitch + MathHelper.clamp(desiredPitch - this.lastPitch, -maxStep * 0.7f, maxStep * 0.7f);

        float gcd = Rotation.gcd();
        float outYaw = this.lastYaw + (float)Math.round((desiredYaw - this.lastYaw) / gcd) * gcd;
        float outPitch = this.lastPitch + (float)Math.round((desiredPitch - this.lastPitch) / gcd) * gcd;

        float microTremor = 0.005f + this.random.nextFloat() * 0.012f;
        outPitch += (this.random.nextFloat() - 0.5f) * (microTremor * 0.5f);
        outPitch = MathHelper.clamp(outPitch, -90.0f, 90.0f);

        if (Float.isNaN(outYaw += (this.random.nextFloat() - 0.5f) * microTremor) || Float.isNaN(outPitch)) {
            outYaw = this.lastYaw;
            outPitch = this.lastPitch;
        }

        Rotation rot = new Rotation(outYaw, outPitch);
        RotationComponent.update(rot, 360.0f, 360.0f, 360.0f, 360.0f, 0, 1, false);
        aura.lastYaw = rot.getYaw();
        aura.lastPitch = rot.getPitch();
    }

    @Override
    public void onAttack() {
        this.errorYawOffset += (this.random.nextFloat() - 0.5f) * 0.8f;
        this.errorPitchOffset += (this.random.nextFloat() - 0.5f) * 0.4f;
    }

    private void resetHumanError() {
        this.errorYawOffset = 0.0f;
        this.errorPitchOffset = 0.0f;
        this.errorTicks = 0;
        this.nextErrorChange = 20;
    }

    public void hardReset() {
        this.lastTargetId = -1;
        this.resetHumanError();
        if (mc.player != null) {
            this.lastYaw = mc.player.getYaw();
            this.lastPitch = mc.player.getPitch();
            this.initialized = true;
        }
    }
}
