package tech.javelin.utility.math;

import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.utility.interfaces.IClient;

public final class BoostUtils implements IClient {
   public static Vec3d getBoost(LivingEntity entity) {
      float yaw = Math.abs((entity.getYaw() - 360.0F) % 360.0F);
      float maxSpeed = 2.4F;
      float[] centers = new float[]{45.0F, 135.0F, 225.0F, 315.0F};
      float center = centers[0];
      float minDiff = 9999.0F;
      float[] var6 = centers;
      int var7 = centers.length;

      float f;
      for(int var8 = 0; var8 < var7; ++var8) {
         float c = var6[var8];
         f = Math.abs(yaw - c);
         if (f < minDiff) {
            minDiff = f;
            center = c;
         }
      }

      float diff = Math.abs(yaw - center);
      float speed = maxSpeed - diff * 0.05F;
      Vec3d vec3d = entity.getRotationVector();
      Vec3d oldVelocity = Vec3d.fromPolar(entity.getPitch(), entity.getYaw()).multiply((double)Math.max(speed, 1.61F));
      f = entity.getPitch() * 0.017453292F;
      double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
      double e = oldVelocity.horizontalLength();
      boolean bl = entity.getVelocity().y <= 0.0D;
      double g = bl && entity.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(entity.getFinalGravity(), 0.01D) : entity.getFinalGravity();
      double h = MathHelper.square(Math.cos((double)f));
      oldVelocity = oldVelocity.add(0.0D, g * (-1.0D + h * 0.75D), 0.0D);
      double i;
      if (oldVelocity.y < 0.0D && d > 0.0D) {
         i = oldVelocity.y * -0.1D * h;
         oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
      }

      if (f < 0.0F && d > 0.0D) {
         i = e * (double)(-MathHelper.sin(f)) * 0.04D;
         oldVelocity = oldVelocity.add(-vec3d.x * i / d, i * 3.2D, -vec3d.z * i / d);
      }

      if (d > 0.0D) {
         oldVelocity = oldVelocity.add((vec3d.x / d * e - oldVelocity.x) * 0.1D, 0.0D, (vec3d.z / d * e - oldVelocity.z) * 0.1D);
      }

      return (new Vec3d((new Vec3d(oldVelocity.x, oldVelocity.y, oldVelocity.z)).length(), (new Vec3d(oldVelocity.x, oldVelocity.y, oldVelocity.z)).length(), (new Vec3d(oldVelocity.x, oldVelocity.y, oldVelocity.z)).length())).multiply(0.99D, 0.98D, 0.99D);
   }

   @Generated
   private BoostUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
