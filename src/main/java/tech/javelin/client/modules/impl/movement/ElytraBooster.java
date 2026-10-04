package tech.javelin.client.modules.impl.movement;

import lombok.Generated;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(
   name = "ElytraBooster",
   category = Category.MOVEMENT,
   description = "Усиливает ваш фейерверк"
)
public final class ElytraBooster extends Module {
   public static final ElytraBooster INSTANCE = new ElytraBooster();
   private final ModeSetting mode = new ModeSetting("Mode", new String[]{""});
   private final NumberSetting boost = new NumberSetting("Boost", 1.0F, 0.1F, 5.0F, 0.1F, () -> {
      return !this.mode.getValue().getName().equals("Auto");
   });
   private ModeSetting.Value custom;
   private ModeSetting.Value auto;

   private ElytraBooster() {
      this.custom = new ModeSetting.Value(this.mode, "Custom", "");
      this.auto = new ModeSetting.Value(this.mode, "Auto", "");
      this.mode.setValue(this.custom);
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public NumberSetting getBoost() {
      return this.boost;
   }

   @Generated
   public ModeSetting.Value getCustom() {
      return this.custom;
   }

   @Generated
   public ModeSetting.Value getAuto() {
      return this.auto;
   }
}
