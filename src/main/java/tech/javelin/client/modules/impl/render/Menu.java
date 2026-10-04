package tech.javelin.client.modules.impl.render;

import tech.javelin.Javelin;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "Menu",
   category = Category.RENDER,
   description = "Меню чита"
)
public final class Menu extends Module {
   public static final Menu INSTANCE = new Menu();

   private Menu() {
      this.setKeyCode(344);
   }

   public void onEnable() {
      if (mc.world == null) {
         this.setEnabled(false);
      } else {
         Javelin.getInstance().getMenuScreen().needToClose = false;
         if (mc.currentScreen != Javelin.getInstance().getMenuScreen()) {
            mc.setScreen(Javelin.getInstance().getMenuScreen());
            super.onEnable();
         }
      }
   }

   public void onDisable() {
      super.onDisable();
   }

   public void setKeyCode(int keyCode) {
      if (keyCode != -1) {
         super.setKeyCode(keyCode);
      }
   }
}
