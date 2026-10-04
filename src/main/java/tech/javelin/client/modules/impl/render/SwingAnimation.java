package tech.javelin.client.modules.impl.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "SwingAnimation",
   category = Category.RENDER,
   description = "Кастомные анимации удара"
)
public final class SwingAnimation extends Module {
   public static final SwingAnimation INSTANCE = new SwingAnimation();
   public ModeSetting animationMode = new ModeSetting("Режим", new String[]{"Smooth", "Self", "Self2", "Down", "Forward", "Touch", "Pander", "Curt", "BlockHit"});
   public NumberSetting swingPower = new NumberSetting("Сила", 5.0F, 1.0F, 10.0F, 1.0F, () -> {
      return !this.animationMode.is("BlockHit") && !this.animationMode.is("Pander") && !this.animationMode.is("Curt");
   });
   public NumberSetting speed = new NumberSetting("Скорость", 7.0F, 0.0F, 10.0F, 1.0F);
   public NumberSetting angle = new NumberSetting("Угол", 0.0F, 0.0F, 360.0F, 1.0F, () -> {
      return this.animationMode.is("Self") || this.animationMode.is("Self2");
   });

   private SwingAnimation() {
   }

   public void renderSwordAnimation(MatrixStack matrices, float swingProgress, float equipProgress, Arm arm) {
      float anim = (float)Math.sin((double)swingProgress * 1.5707963267948966D * 2.0D);
      float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
      String var7 = this.animationMode.get();
      byte var8 = -1;
      switch(var7.hashCode()) {
      case -1911677324:
         if (var7.equals("Pander")) {
            var8 = 7;
         }
         break;
      case -1814666802:
         if (var7.equals("Smooth")) {
            var8 = 0;
         }
         break;
      case -599960602:
         if (var7.equals("BlockHit")) {
            var8 = 8;
         }
         break;
      case 2112084:
         if (var7.equals("Curt")) {
            var8 = 6;
         }
         break;
      case 2136258:
         if (var7.equals("Down")) {
            var8 = 4;
         }
         break;
      case 2573164:
         if (var7.equals("Self")) {
            var8 = 3;
         }
         break;
      case 79768134:
         if (var7.equals("Self2")) {
            var8 = 1;
         }
         break;
      case 80998175:
         if (var7.equals("Touch")) {
            var8 = 5;
         }
         break;
      case 987507365:
         if (var7.equals("Forward")) {
            var8 = 2;
         }
      }

      float f;
      float g;
      float sinExtra;
      switch(var8) {
      case 0:
         matrices.translate(0.56F, -0.52F, -0.72F);
         f = this.swingPower.getCurrent() * 10.0F;
         g = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F + g * (-f / 4.0F)));
         sinExtra = MathHelper.sin(MathHelper.sqrt(swingProgress * swingProgress) * 3.1415927F);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sinExtra * -(f / 4.0F)));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sinExtra * -f));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0F));
         break;
      case 1:
         matrices.translate(0.56F, -0.52F, -0.72F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-this.angle.getCurrent() - this.swingPower.getCurrent() * 10.0F * anim));
         break;
      case 2:
         matrices.translate(0.56F, -0.52F, -0.72F);
         f = 35.0F;
         matrices.translate(0.0D, 0.0D, -0.3D * (double)sin2);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -f));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * f));
         break;
      case 3:
         matrices.translate(0.56F, -0.52F, -0.72F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-this.angle.getCurrent() - this.swingPower.getCurrent() * 10.0F * anim));
         break;
      case 4:
         matrices.translate(0.56F, -0.52F - anim * this.swingPower.getCurrent() / 24.0F, -0.72F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
         break;
      case 5:
         matrices.translate(0.56F, -0.52F, -0.72F);
         matrices.scale(1.0F, 1.0F, 1.0F + anim * this.swingPower.getCurrent() / 4.0F);
         matrices.translate(0.0F, 0.0F, -0.265F);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
         break;
      case 6:
         matrices.translate(0.56F, -0.52F, -0.72F);
         f = MathHelper.sqrt(swingProgress);
         g = MathHelper.sin(f * 3.1415927F);
         sinExtra = MathHelper.sin(swingProgress * 3.1415927F);
         matrices.translate(0.4F - g * 0.2F, -0.2F + g * 0.3F, -0.5F - sinExtra * 0.2F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(91.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F + g * -100.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
         break;
      case 7:
         matrices.translate(0.56F, -0.52F, -0.72F);
         matrices.scale(0.8F, 0.8F, 0.8F);
         f = 1.0F - MathHelper.lerp(mc.getRenderTickCounter().getTickDelta(true), mc.gameRenderer.firstPersonRenderer.prevEquipProgressMainHand, mc.gameRenderer.firstPersonRenderer.equipProgressMainHand);
         matrices.translate(0.3D - (double)(anim * 0.15F), (double)(0.2F - f * 0.12F), (double)(-0.15F - anim * 0.13F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0F - 10.0F * anim));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-16.0F - 8.0F * anim));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-83.0F - 26.0F * anim));
         break;
      case 8:
         matrices.translate(0.56F, -0.52F, -0.72F);
         f = MathHelper.sin((float)((double)(swingProgress * swingProgress) * 3.141592653589793D));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
         g = MathHelper.sin((float)((double)MathHelper.sqrt(swingProgress) * 3.141592653589793D));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -20.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(g * -20.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
         matrices.translate(0.4F, 0.2F, 0.2F);
         matrices.translate(-0.5F, 0.08F, 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
      }

   }

   private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -20.0F)));
      float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
   }
}
