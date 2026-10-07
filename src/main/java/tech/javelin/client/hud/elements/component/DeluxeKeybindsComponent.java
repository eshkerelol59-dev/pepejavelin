package tech.javelin.client.hud.elements.component;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.api.Module;
import tech.javelin.utility.render.display.Keyboard;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

/**
 * Бинды в стиле Deluxe: хедер Hotkeys, ряды с тумблером и клавишей слева,
 * именем модуля справа. Показывает включенные модули с биндами.
 */
public class DeluxeKeybindsComponent extends DraggableHudElement {
   private final Map<Module, Row> rowsByModule = new IdentityHashMap<>();
   private final Animation panelAlpha = new Animation(250L, Easing.QUARTIC_OUT);

   public DeluxeKeybindsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      List<Row> visible = new ArrayList<>();
      for (Module module : Javelin.getInstance().getModuleManager().getModules()) {
         boolean shouldShow = module.isEnabled() && module.getKeyCode() != -1;
         Row row = this.rowsByModule.get(module);
         if (row == null) {
            if (!shouldShow) {
               continue;
            }
            row = new Row(module);
            this.rowsByModule.put(module, row);
         }
         row.bindingName = module.getKeyCode() != -1 ? Keyboard.getKeyName(module.getKeyCode()) : row.bindingName;
         row.animation.update(shouldShow ? 1.0F : 0.0F);
         row.enabledAnimation.update(module.isEnabled() ? 1.0F : 0.0F);
         if (row.animation.getValue() > 0.003F || shouldShow) {
            visible.add(row);
         }
      }
      this.rowsByModule.values().removeIf(row -> row.animation.getValue() == 0.0F && !row.module.isEnabled());

      this.panelAlpha.update(!visible.isEmpty() || mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen ? 1.0F : 0.0F);
      float alpha = this.panelAlpha.getValue();
      if (alpha <= 0.003F) {
         return;
      }

      float bindColumn = 0.0F;
      float moduleColumn = 0.0F;
      float height = DeluxeHudKit.HEADER_HEIGHT;
      for (Row row : visible) {
         bindColumn = Math.max(bindColumn, DeluxeHudKit.iconChipWidth(10.0F, Fonts.SEMIBOLD.getWidth(row.bindingName, 6.0F)));
         moduleColumn = Math.max(moduleColumn, DeluxeHudKit.chipWidth(Fonts.SEMIBOLD.getWidth(row.module.getName(), 6.0F)));
         height += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }
      float width = Math.max(DeluxeHudKit.headerWidth("Hotkeys"), bindColumn + (bindColumn > 0.0F && moduleColumn > 0.0F ? 2.0F : 0.0F) + moduleColumn);

      float x = this.getX();
      float y = this.getY();
      DeluxeHudKit.drawHeader(ctx, x, y, width, alpha, Javelin.id("icons/hud/hud_keybinds.png"), "Hotkeys");

      float rowY = y + DeluxeHudKit.HEADER_TO_ROWS;
      for (Row row : visible) {
         float rowAlpha = row.animation.getValue() * alpha;
         if (rowAlpha > 0.003F) {
            float offset = DeluxeHudKit.openingOffset(rowAlpha);
            float bindX = x - offset;
            float moduleX = x + bindColumn + 2.0F - offset;
            float enabled = row.enabledAnimation.getValue();

            DeluxeHudKit.drawIconChip(ctx, bindX, rowY, bindColumn, rowAlpha, 10.0F, (c, left, centerY, a) -> {
               float toggleX = left + 1.0F;
               float toggleY = centerY - 5.0F;
               DrawUtil.drawRoundedRect(c.getMatrices(), toggleX, toggleY, 10.0F, 10.0F, BorderRadius.all(5.0F),
                       new ColorRGBA(30, 30, 30, 200.0F * a));
               DrawUtil.drawRoundedRect(c.getMatrices(), toggleX + 1.5F + enabled * 4.0F, toggleY + 1.5F, 7.0F, 7.0F, BorderRadius.all(3.5F),
                       DeluxeHudKit.theme().getColor().withAlpha(a));
            }, row.bindingName);
            DeluxeHudKit.drawTextChip(ctx, moduleX, rowY, moduleColumn, rowAlpha, row.module.getName());
         }
         rowY += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }

      this.width = width;
      this.height = Math.max(height, DeluxeHudKit.HEADER_HEIGHT);
   }

   private static final class Row {
      final Module module;
      final Animation animation = new Animation(600L, Easing.QUARTIC_OUT);
      final Animation enabledAnimation = new Animation(400L, Easing.QUARTIC_OUT);
      String bindingName = "";

      Row(Module module) {
         this.module = module;
      }
   }
}
