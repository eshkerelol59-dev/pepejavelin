package tech.javelin.client.hud.elements.component;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import tech.javelin.Javelin;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.base.color.ColorUtil;
import tech.javelin.utility.render.display.shader.DrawUtil;

/**
 * Дизайн-система Deluxe HUD: стеклянный хедер с иконкой, ряды-чипы с
 * дивайдерами, экспо-сдвиг при появлении. Размеры перенесены 1:1 из
 * HudPanelRenderer клиента Deluxe (base scale 2.0).
 */
public final class DeluxeHudKit {
   public static final float HEADER_HEIGHT = 13.0F;
   public static final float ROW_HEIGHT = 11.0F;
   public static final float ROW_STEP = 13.0F;
   public static final float HEADER_TO_ROWS = 15.0F;
   public static final float RADIUS = 4.5F;

   private DeluxeHudKit() {
   }

   public static Theme theme() {
      return Javelin.getInstance().getThemeManager().getCurrentTheme();
   }

   public static ColorRGBA primaryText(float alpha) {
      return ColorUtil.interpolate(theme().getColor(), ColorRGBA.WHITE, 0.92F).withAlpha(alpha);
   }

   public static ColorRGBA secondaryText(float alpha) {
      return ColorUtil.interpolate(theme().getColor(), ColorRGBA.WHITE, 0.68F).withAlpha(alpha);
   }

   public static void drawPanel(MatrixStack matrices, float x, float y, float width, float height, float alpha) {
      if (width > 0.5F && alpha > 0.003F) {
         DrawUtil.drawBlur(matrices, x, y, width, height, 9.0F, BorderRadius.all(RADIUS), new ColorRGBA(14, 14, 14, 185.0F * alpha));
      }
   }

   public static void drawHeader(CustomDrawContext ctx, float x, float y, float width, float alpha, Identifier icon, String title) {
      if (width > 0.5F && alpha > 0.003F) {
         DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, HEADER_HEIGHT, 11.0F, BorderRadius.all(RADIUS), new ColorRGBA(10, 10, 10, 205.0F * alpha));
         if (icon != null) {
            ctx.drawTexture(icon, x + 3.0F, y + 2.5F, 8.0F, 8.0F, theme().getColor().withAlpha(alpha));
         }
         ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), title, x + 16.0F, y + 2.85F, primaryText(alpha));
      }
   }

   public static void drawDivider(MatrixStack matrices, float x, float centerY, float alpha, float height) {
      DrawUtil.drawRoundedRect(matrices, x, centerY - height / 2.0F, 0.5F, height, BorderRadius.all(0.25F),
            ColorRGBA.WHITE.withAlpha(0.3F * 76.0F * alpha));
   }

   /** Экспо-сдвиг ряда при появлении: до 7px влево. */
   public static float openingOffset(float progress) {
      float squared = progress * progress;
      float eased = squared >= 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0D, -10.0D * squared);
      return 7.0F * (1.0F - eased);
   }

   /** Ширина чипа с текстом: паддинги 4+4. */
   public static float chipWidth(float textWidth) {
      return textWidth > 0.0F ? 8.0F + textWidth : 0.0F;
   }

   /** Ширина чипа с иконкой и текстом: 2 + icon + 4 + text + 4. */
   public static float iconChipWidth(float iconWidth, float textWidth) {
      float content = iconWidth > 0.0F && textWidth > 0.0F ? iconWidth + 4.0F + textWidth : iconWidth + textWidth;
      return content > 0.0F ? 2.0F + content + 4.0F : 0.0F;
   }

   /** Ширина хедера: 16 + title + 4. */
   public static float headerWidth(String title) {
      return 16.0F + Fonts.SEMIBOLD.getWidth(title, 6.0F) + 4.0F;
   }

   /** Левый чип ряда: иконка + дивайдер + текст по центру остатка. */
   public static void drawIconChip(CustomDrawContext ctx, float x, float y, float chipWidth, float alpha, float iconWidth,
                                   ChipIcon icon, String text) {
      MatrixStack matrices = ctx.getMatrices();
      float textWidth = Fonts.SEMIBOLD.getWidth(text, 6.0F);
      drawPanel(matrices, x, y, chipWidth, ROW_HEIGHT, alpha);
      float dividerX = x + chipWidth - 4.0F - textWidth - 1.0F;
      if (icon != null) {
         icon.draw(ctx, x + 2.0F + (dividerX - 1.0F - x - 2.0F - iconWidth) / 2.0F, y + ROW_HEIGHT / 2.0F, alpha);
      }
      drawDivider(matrices, dividerX, y + ROW_HEIGHT / 2.0F, alpha, 3.0F);
      float textStart = dividerX + 0.75F;
      ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), text, textStart, y + 2.85F, primaryText(alpha));
   }

   /** Правый текстовый чип ряда. */
   public static void drawTextChip(CustomDrawContext ctx, float x, float y, float chipWidth, float alpha, String text) {
      drawPanel(ctx.getMatrices(), x, y, chipWidth, ROW_HEIGHT, alpha);
      ctx.drawText(Fonts.SEMIBOLD.getFont(6.0F), text, x + 4.0F, y + 2.85F, primaryText(alpha));
   }

   @FunctionalInterface
   public interface ChipIcon {
      void draw(CustomDrawContext ctx, float left, float centerY, float alpha);
   }
}
