package tech.javelin.client.modules.impl.combat.aura;

import net.minecraft.entity.LivingEntity;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.utility.interfaces.IMinecraft;

public interface RotationAura extends IMinecraft {
    void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack);
    default void onAttack() {}
}
