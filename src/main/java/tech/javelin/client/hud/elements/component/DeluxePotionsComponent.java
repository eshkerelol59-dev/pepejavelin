package tech.javelin.client.hud.elements.component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;

/**
 * Эффекты в стиле Deluxe: хедер Potions, ряды из двух чипов —
 * длительность с иконкой эффекта слева, имя с уровнем справа.
 */
public class DeluxePotionsComponent extends DraggableHudElement {
   private final Map<String, Row> rows = new LinkedHashMap<>();
   private final Animation panelAlpha = new Animation(250L, Easing.QUARTIC_OUT);

   public DeluxePotionsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      if (mc.player == null) {
         return;
      }

      this.updateRows();
      List<Row> visible = new ArrayList<>();
      for (Row row : this.rows.values()) {
         if (row.animation.getValue() > 0.003F || row.active) {
            visible.add(row);
         }
      }
      boolean anyVisible = visible.stream().anyMatch(r -> r.animation.getValue() > 0.003F);
      this.panelAlpha.update(anyVisible || mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen ? 1.0F : 0.0F);
      float alpha = this.panelAlpha.getValue();
      if (alpha <= 0.003F) {
         return;
      }

      float durationColumn = 0.0F;
      float nameColumn = 0.0F;
      float height = DeluxeHudKit.HEADER_HEIGHT;
      for (Row row : visible) {
         durationColumn = Math.max(durationColumn, DeluxeHudKit.iconChipWidth(8.0F, Fonts.SEMIBOLD.getWidth(row.durationText, 6.0F)));
         nameColumn = Math.max(nameColumn, DeluxeHudKit.chipWidth(row.nameWidth()));
         height += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }
      float width = Math.max(DeluxeHudKit.headerWidth("Potions"), durationColumn + (durationColumn > 0.0F && nameColumn > 0.0F ? 2.0F : 0.0F) + nameColumn);

      float x = this.getX();
      float y = this.getY();
      DeluxeHudKit.drawHeader(ctx, x, y, width, alpha, tech.javelin.Javelin.id("icons/hud/hud_potions.png"), "Potions");

      float rowY = y + DeluxeHudKit.HEADER_TO_ROWS;
      for (Row row : visible) {
         float rowAlpha = row.animation.getValue() * alpha;
         if (rowAlpha > 0.003F) {
            float offset = DeluxeHudKit.openingOffset(rowAlpha);
            float durationX = x - offset;
            float nameX = x + durationColumn + 2.0F - offset;
            Identifier icon = Identifier.of("minecraft", "textures/mob_effect/"
                  + row.effect.getEffectType().value().getTranslationKey().replace("effect.minecraft.", "") + ".png");
            DeluxeHudKit.drawIconChip(ctx, durationX, rowY, durationColumn, rowAlpha, 8.0F,
                  (c, left, centerY, a) -> c.drawTexture(icon, left, centerY - 4.0F, 8.0F, 8.0F, ColorRGBA.WHITE.withAlpha(a)), row.durationText);
            boolean harmful = row.effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
            DeluxeHudKit.drawPanel(ctx.getMatrices(), nameX, rowY, nameColumn, DeluxeHudKit.ROW_HEIGHT, rowAlpha);
            float textY = rowY + 2.85F;
            ctext(ctx, row.name, nameX + 4.0F, textY, harmful ? new ColorRGBA(255, 82, 82, 255).withAlpha(rowAlpha) : DeluxeHudKit.primaryText(rowAlpha));
            if (!row.amplifierSuffix.isEmpty()) {
               ctext(ctx, row.amplifierSuffix, nameX + 4.0F + Fonts.SEMIBOLD.getWidth(row.name, 6.0F), textY,
                     DeluxeHudKit.theme().getColor().withAlpha(rowAlpha));
            }
         }
         rowY += DeluxeHudKit.ROW_STEP * row.animation.getValue();
      }

      this.width = width;
      this.height = Math.max(height, DeluxeHudKit.HEADER_HEIGHT);
   }

   private static void ctext(CustomDrawContext ctx, String text, float x, float y, ColorRGBA color) {
      ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), text, x, y, color);
   }

   private void updateRows() {
      for (Row row : this.rows.values()) {
         row.active = false;
      }
      for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
         String key = effect.getTranslationKey() + ":" + effect.getAmplifier();
         Row row = this.rows.computeIfAbsent(key, k -> new Row(effect));
         row.effect = effect;
         row.active = true;
         row.durationText = durationText(effect);
      }
      this.rows.values().removeIf(row -> !row.active && row.animation.getValue() == 0.0F);
      List<Row> sorted = new ArrayList<>(this.rows.values());
      sorted.sort(Comparator.comparingInt((Row r) -> sortOrder(r.effect)).thenComparing(Row::nameWidth, Comparator.reverseOrder()));
      for (Row row : sorted) {
         row.animation.update(row.active ? 1.0F : 0.0F);
      }
   }

   private static int sortOrder(StatusEffectInstance effect) {
      StatusEffect status = (StatusEffect) effect.getEffectType().value();
      String id = status.getTranslationKey();
      if (id.endsWith("strength") || id.endsWith("speed") || id.endsWith("fire_resistance")
              || id.endsWith("resistance") || id.endsWith("absorption") || id.endsWith("regeneration")) {
         return 0;
      }
      StatusEffectCategory category = status.getCategory();
      return category == StatusEffectCategory.BENEFICIAL ? 1 : (category == StatusEffectCategory.NEUTRAL ? 2 : 3);
   }

   private static String durationText(StatusEffectInstance effect) {
      if (effect.isInfinite()) {
         return "INF";
      }
      int totalSeconds = effect.getDuration() / 20;
      int minutes = totalSeconds / 60;
      int seconds = totalSeconds % 60;
      return minutes > 0 ? minutes + "м " + seconds + "с" : seconds + "с";
   }

   private static final class Row {
      final String name;
      final String amplifierSuffix;
      final Animation animation = new Animation(600L, Easing.QUARTIC_OUT);
      StatusEffectInstance effect;
      String durationText = "";
      boolean active = true;

      Row(StatusEffectInstance effect) {
         this.effect = effect;
         this.name = I18n.translate(effect.getTranslationKey());
         this.amplifierSuffix = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
      }

      float nameWidth() {
         return Fonts.SEMIBOLD.getWidth(this.name, 6.0F) + Fonts.SEMIBOLD.getWidth(this.amplifierSuffix, 6.0F);
      }
   }
}
