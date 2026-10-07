package tech.javelin.client.hud.elements.component;

import net.minecraft.item.ItemStack;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.utility.render.display.base.CustomDrawContext;

/**
 * Инвентарь в стиле Deluxe: хедер Inventory и три ряда по 9 слотов
 * с панелями и дивайдерами между слотами.
 */
public class DeluxeInventoryComponent extends DraggableHudElement {
   private static final float GRID_WIDTH = 111.0F;
   private static final float SLOT_PITCH = 12.5F;
   private final Animation visibility = new Animation(400L, Easing.QUARTIC_OUT);

   public DeluxeInventoryComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      if (mc.player == null) {
         return;
      }

      boolean hasItems = false;
      for (int slot = 9; slot < 36 && !hasItems; ++slot) {
         hasItems = !mc.player.getInventory().getStack(slot).isEmpty();
      }
      this.visibility.update(hasItems || mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen ? 1.0F : 0.0F);
      float alpha = this.visibility.getValue();
      if (alpha <= 0.003F) {
         return;
      }

      float width = Math.max(DeluxeHudKit.headerWidth("Inventory"), GRID_WIDTH);
      float height = DeluxeHudKit.HEADER_HEIGHT + 2.0F + 3.0F * DeluxeHudKit.ROW_HEIGHT + 2.0F * 2.5F;

      float x = this.getX();
      float y = this.getY();
      DeluxeHudKit.drawHeader(ctx, x, y, width, alpha, Javelin.id("icons/hud/hud_inventory.png"), "Inventory");

      float gridX = x + (width - GRID_WIDTH) / 2.0F;
      float rowsY = y + DeluxeHudKit.HEADER_HEIGHT + 2.0F;
      for (int row = 0; row < 3; ++row) {
         float rowY = rowsY + row * (DeluxeHudKit.ROW_HEIGHT + 2.5F);
         DeluxeHudKit.drawPanel(ctx.getMatrices(), gridX, rowY, GRID_WIDTH, DeluxeHudKit.ROW_HEIGHT, alpha);
         for (int divider = 0; divider < 8; ++divider) {
            DeluxeHudKit.drawDivider(ctx.getMatrices(), gridX + divider * SLOT_PITCH + DeluxeHudKit.ROW_HEIGHT + 0.5F, rowY + DeluxeHudKit.ROW_HEIGHT / 2.0F, alpha, 1.5F);
         }
         for (int column = 0; column < 9; ++column) {
            ItemStack stack = mc.player.getInventory().getStack(9 + row * 9 + column);
            if (!stack.isEmpty()) {
               float itemX = Math.round((gridX + column * SLOT_PITCH + 1.5F) * 2.0F) / 2.0F;
               float itemY = Math.round((rowY + 1.5F) * 2.0F) / 2.0F;
               ctx.getMatrices().push();
               ctx.getMatrices().translate(itemX, itemY, 0.0D);
               ctx.getMatrices().scale(0.5F, 0.5F, 0.5F);
               ctx.drawItem(stack, 0, 0);
               ((tech.javelin.utility.mixin.accessors.DrawContextAccessor) ctx).callDrawItemBar(stack, 0, 0);
               ctx.getMatrices().pop();
            }
         }
      }

      this.width = width;
      this.height = height;
   }
}
