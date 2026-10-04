package tech.javelin.utility.mixin.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import tech.javelin.utility.interfaces.IMinecraft;

@Mixin({LivingEntity.class})
public class LivingEntityMixin implements IMinecraft {
}
