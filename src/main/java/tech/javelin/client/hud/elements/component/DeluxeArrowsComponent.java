package tech.javelin.client.hud.elements.component;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;

/**
 * Счётчик стрел в стиле Deluxe: предмет стрелы + количество рядом с прицелом.
 */
public class DeluxeArrowsComponent extends DraggableHudElement {
   private final Animation visibility = new Animation(400L, Easing.QUARTIC_OUT);

   public DeluxeArrowsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      if (mc.player == null) {
         return;
      }

      ItemStack displayArrow = null;
      int count = 0;
      for (int slot = 0; slot < mc.player.getInventory().size(); ++slot) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && (stack.isOf(Items.ARROW) || stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW))) {
            count += stack.getCount();
            if (displayArrow == null) {
               displayArrow = stack;
            }
         }
      }
      ItemStack offHand = mc.player.getOffHandStack();
      if (!offHand.isEmpty() && (offHand.isOf(Items.ARROW) || offHand.isOf(Items.TIPPED_ARROW) || offHand.isOf(Items.SPECTRAL_ARROW))) {
         count += offHand.getCount();
         if (displayArrow == null) {
            displayArrow = offHand;
         }
      }

      this.visibility.update(count > 0 ? 1.0F : 0.0F);
      float alpha = this.visibility.getValue();
      if (alpha <= 0.003F || displayArrow == null) {
         return;
      }

      String countText = String.valueOf(count);
      float textWidth = Fonts.SEMIBOLD.getWidth(countText, 7.0F);
      float width = 13.0F + textWidth;
      float height = 10.0F;

      float x = this.getX();
      float y = this.getY();
      ctx.getMatrices().push();
      ctx.getMatrices().translate(x, y, 0.0D);
      ctx.getMatrices().scale(0.625F, 0.625F, 0.625F);
      ctx.drawItem(displayArrow, 0, 0);
      ctx.getMatrices().pop();
      ctx.drawText(Fonts.SEMIBOLD.getFont(7.0F), countText, x + 13.0F, y + 1.5F, DeluxeHudKit.primaryText(alpha));

      this.width = width;
      this.height = height;
   }
}
