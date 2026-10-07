package tech.javelin.client.hud.elements.component;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;

/**
 * Кулдауны в стиле Deluxe: хедер Cooldowns, ряды с предметом и оставшимся
 * временем. Оставшееся время оценивается по скорости убывания прогресса
 * кулдауна (без доступа к внутренним тикам менеджера).
 */
public class DeluxeCooldownsComponent extends DraggableHudElement {
   private final Map<Item, Row> rowsByItem = new IdentityHashMap<>();
   private final Animation panelAlpha = new Animation(250L, Easing.QUARTIC_OUT);

   public DeluxeCooldownsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      if (mc.player == null) {
         return;
      }

      List<Row> visible = this.updateRows();
      this.panelAlpha.update(!visible.isEmpty() || mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen ? 1.0F : 0.0F);
      float alpha = this.panelAlpha.getValue();
      if (alpha <= 0.003F) {
         return;
      }

      float durationColumn = 0.0F;
      float nameColumn = 0.0F;
      float height = DeluxeHudKit.HEADER_HEIGHT;
      for (Row row : visible) {
         durationColumn = Math.max(durationColumn, DeluxeHudKit.iconChipWidth(8.0F, Fonts.SEMIBOLD.getWidth(row.durationText, 6.0F)));
         nameColumn = Math.max(nameColumn, DeluxeHudKit.chipWidth(Fonts.SEMIBOLD.getWidth(row.name, 6.0F)));
         height += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }
      float width = Math.max(DeluxeHudKit.headerWidth("Cooldowns"), durationColumn + (durationColumn > 0.0F && nameColumn > 0.0F ? 2.0F : 0.0F) + nameColumn);

      float x = this.getX();
      float y = this.getY();
      DeluxeHudKit.drawHeader(ctx, x, y, width, alpha, Javelin.id("icons/hud/hud_cooldowns.png"), "Cooldowns");

      float rowY = y + DeluxeHudKit.HEADER_TO_ROWS;
      for (Row row : visible) {
         float rowAlpha = row.animation.getValue() * alpha;
         if (rowAlpha > 0.003F) {
            float offset = DeluxeHudKit.openingOffset(rowAlpha);
            float durationX = x - offset;
            float nameX = x + durationColumn + 2.0F - offset;

            ItemStack stack = row.displayStack;
            DeluxeHudKit.drawIconChip(ctx, durationX, rowY, durationColumn, rowAlpha, 8.0F, (c, left, centerY, a) -> {
               c.getMatrices().push();
               c.getMatrices().translate(left, centerY - 4.0F, 0.0D);
               c.getMatrices().scale(0.5F, 0.5F, 0.5F);
               c.drawItem(stack, 0, 0);
               c.getMatrices().pop();
            }, row.durationText);
            DeluxeHudKit.drawTextChip(ctx, nameX, rowY, nameColumn, rowAlpha, row.name);
         }
         rowY += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }

      this.width = width;
      this.height = Math.max(height, DeluxeHudKit.HEADER_HEIGHT);
   }

   private List<Row> updateRows() {
      long now = System.currentTimeMillis();
      for (Row row : this.rowsByItem.values()) {
         row.active = false;
      }

      List<ItemStack> stacks = new ArrayList<>();
      for (int slot = 0; slot < 9; ++slot) {
         stacks.add(mc.player.getInventory().getStack(slot));
      }
      stacks.add(mc.player.getOffHandStack());

      for (ItemStack stack : stacks) {
         if (stack.isEmpty()) {
            continue;
         }
         float progress = mc.player.getItemCooldownManager().getCooldownProgress(stack, 0.0F);
         if (progress <= 0.001F) {
            continue;
         }
         Row row = this.rowsByItem.computeIfAbsent(stack.getItem(), item -> new Row(stack));
         if (row.displayStack == null || row.displayStack.getItem() != stack.getItem()) {
            row.displayStack = stack.copy();
            row.name = stack.getName().getString();
         }
         row.active = true;

         if (row.lastProgress <= 0.0F || progress > row.lastProgress) {
            row.lastProgress = progress;
            row.lastSampleMillis = now;
            row.remainingMillis = -1.0F;
         } else if (progress < row.lastProgress) {
            float progressDelta = row.lastProgress - progress;
            long timeDelta = Math.max(1L, now - row.lastSampleMillis);
            if (progressDelta > 0.0001F) {
               float totalMillis = (float) timeDelta / progressDelta;
               row.remainingMillis = (1.0F - progress) * totalMillis;
            }
            row.lastProgress = progress;
            row.lastSampleMillis = now;
         }
         row.durationText = row.remainingMillis < 0.0F ? "..." : formatRemaining(row.remainingMillis);
      }

      this.rowsByItem.values().removeIf(row -> !row.active && row.animation.getValue() == 0.0F);
      List<Row> visible = new ArrayList<>();
      for (Row row : this.rowsByItem.values()) {
         row.animation.update(row.active ? 1.0F : 0.0F);
         if (row.active || row.animation.getValue() > 0.003F) {
            visible.add(row);
         }
      }
      return visible;
   }

   private static String formatRemaining(float millis) {
      float seconds = millis / 1000.0F;
      if (seconds >= 60.0F) {
         return String.format(java.util.Locale.US, "%.0fм %.0fс", seconds / 60.0F, seconds % 60.0F);
      }
      return String.format(java.util.Locale.US, "%.1fс", seconds);
   }

   private static final class Row {
      final Animation animation = new Animation(600L, Easing.QUARTIC_OUT);
      ItemStack displayStack;
      String name = "";
      String durationText = "...";
      float lastProgress = -1.0F;
      long lastSampleMillis;
      float remainingMillis = -1.0F;
      boolean active = true;

      Row(ItemStack stack) {
         this.displayStack = stack.copy();
         this.name = stack.getName().getString();
      }
   }
}
