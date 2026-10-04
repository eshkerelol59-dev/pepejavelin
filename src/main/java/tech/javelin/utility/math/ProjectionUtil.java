package tech.javelin.utility.math;

import lombok.Generated;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;
import tech.javelin.utility.interfaces.IMinecraft;
import tech.javelin.utility.render.level.Render3DUtil;

public final class ProjectionUtil implements IMinecraft {
   @NotNull
   public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
      Vector3f delta = pos.subtract(mc.getEntityRenderDispatcher().camera.getPos()).toVector3f();
      int[] viewport = new int[4];
      GL11.glGetIntegerv(2978, viewport);
      Vector3f target = new Vector3f();
      Vector4f transformedCoordinates = (new Vector4f(delta.x, delta.y, delta.z, 1.0F)).mul(Render3DUtil.getLastWorldSpaceMatrix());
      Matrix4f matrixProj = new Matrix4f(Render3DUtil.getLastProjMat());
      matrixProj.project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
      return new Vec3d((double)target.x / mc.getWindow().getScaleFactor(), (double)((float)mc.getWindow().getHeight() - target.y) / mc.getWindow().getScaleFactor(), (double)target.z);
   }

   public static boolean canSee(Vec3d vec3d) {
      Camera camera = mc.getEntityRenderDispatcher().camera;
      Rotation angle = RotationUtil.calculateAngle(vec3d);
      return Math.abs(MathHelper.wrapDegrees(angle.getYaw() - camera.getYaw())) < 90.0F && Math.abs(MathHelper.wrapDegrees(angle.getPitch() - camera.getPitch())) < 60.0F || canSee(new Box(BlockPos.ofFloored(vec3d)));
   }

   public static boolean canSee(Box box) {
      Frustum frustum = mc.worldRenderer.frustum;
      return box != null && frustum != null && frustum.isVisible(box);
   }

   public static boolean canSee(Vector4d vec) {
      return vec == null || vec.x < 0.0D && vec.z < 1.0D || vec.y < 0.0D && vec.w < 1.0D;
   }

   public static double centerX(Vector4d vec) {
      return vec.x + (vec.z - vec.x) / 2.0D;
   }

   @NotNull
   public static Vec3d[] getVec3ds(Entity ent, Vec3d pos) {
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + pos.x - 0.10000000149011612D, axisAlignedBB2.minY - ent.getY() + pos.y - 0.10000000149011612D, axisAlignedBB2.minZ - ent.getZ() + pos.z - 0.10000000149011612D, axisAlignedBB2.maxX - ent.getX() + pos.x + 0.10000000149011612D, axisAlignedBB2.maxY - ent.getY() + pos.y + 0.10000000149011612D, axisAlignedBB2.maxZ - ent.getZ() + pos.z + 0.10000000149011612D);
      return new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
   }

   public static Vector4d getVector4D(Entity ent) {
      Vector4d position = null;
      Vec3d[] var2 = getVec3ds(ent, MathUtil.interpolate(ent));
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Vec3d vector = var2[var4];
         vector = worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0D && vector.z < 1.0D) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0D);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      return position;
   }

   @Generated
   private ProjectionUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
