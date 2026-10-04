package tech.javelin.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.render.EventCamera;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.MultiBooleanSetting;

@ModuleAnnotation(
   name = "NoRender",
   category = Category.RENDER,
   description = "Убирает лишние элементы с экрана"
)
public final class NoRender extends Module {
   public static final NoRender INSTANCE = new NoRender();
   private final MultiBooleanSetting settings = MultiBooleanSetting.create("Убрать", List.of("Огонь", "Плохие эффекты", "Камера клип"));

   @Native
   public boolean isRemoveFire() {
      return this.isEnabled() && this.settings.isEnable(0);
   }

   @Native
   public boolean isRemoveBadEffect() {
      return this.isEnabled() && this.settings.isEnable(1);
   }

   @EventTarget
   @Native
   private void onCamera(EventCamera e) {
      e.setCameraClip(this.settings.isEnable("Камера клип"));
      e.cancel();
   }
}
