package tech.javelin.utility.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.javelin.client.modules.impl.movement.ElytraBooster;
import tech.javelin.utility.math.BoostUtils;

@Mixin({FireworkRocketEntity.class})
public abstract class FireworkRocketEntityMixin extends ProjectileEntity {
   @Unique
   private Vec3d rotation;
   @Shadow
   private LivingEntity field_7616;

   public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
      super(entityType, world);
   }

   @ModifyExpressionValue(
      method = {"tick"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"
)}
   )
   public Vec3d tick(Vec3d original) {
      this.rotation = original;
      return this.rotation;
   }

   @Redirect(
      method = {"tick"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;",
   ordinal = 0
)
   )
   public Vec3d tick(Vec3d instance, double x, double y, double z) {
      ElytraBooster elytraBooster = ElytraBooster.INSTANCE;
      if (elytraBooster != null && elytraBooster.isEnabled() && MinecraftClient.getInstance().player.isGliding()) {
         if (elytraBooster.getMode().getValue().getName().equals("Auto")) {
            return instance.add(this.rotation.x * 0.1D + (this.rotation.x * BoostUtils.getBoost(MinecraftClient.getInstance().player).x - instance.x) * 0.5D, this.rotation.y * 0.1D + (this.rotation.y * BoostUtils.getBoost(MinecraftClient.getInstance().player).y - instance.y) * 0.5D, this.rotation.z * 0.1D + (this.rotation.z * BoostUtils.getBoost(MinecraftClient.getInstance().player).z - instance.z) * 0.5D);
         } else {
            double boostValue = (double)elytraBooster.getBoost().getCurrent();
            return instance.add(this.rotation.x * 0.1D + (this.rotation.x * boostValue - instance.x) * 0.5D, this.rotation.y * 0.1D + (this.rotation.y * boostValue - instance.y) * 0.5D, this.rotation.z * 0.1D + (this.rotation.z * boostValue - instance.z) * 0.5D);
         }
      } else {
         return instance.add(this.rotation.x * 0.1D + (this.rotation.x * 1.5D - instance.x) * 0.5D, this.rotation.y * 0.1D + (this.rotation.y * 1.5D - instance.y) * 0.5D, this.rotation.z * 0.1D + (this.rotation.z * 1.5D - instance.z) * 0.5D);
      }
   }

   @Unique
   private Vec3d calculateRotationVector(float pitch, float yaw) {
      float f = pitch * 0.017453292F;
      float g = -yaw * 0.017453292F;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g);
      float j = MathHelper.cos(f);
      float k = MathHelper.sin(f);
      return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
   }
}
