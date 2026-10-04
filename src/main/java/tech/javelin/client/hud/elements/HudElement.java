package tech.javelin.client.hud.elements;

import java.util.function.Supplier;
import lombok.Generated;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.font.Font;
import tech.javelin.base.font.Fonts;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;

public class HudElement {
   private final String icon;
   private final Supplier<String> text;
   private final String prefix;
   private float width;

   public HudElement(String icon, Supplier<String> text) {
      this(icon, text, "");
   }

   public void calculateWidth(Font font, float iconSize, float cellPadding, float iconTextSpacing) {
      float textWidth = this.text != null ? font.width((String)this.text.get()) + font.width(this.prefix) + (float)(this.prefix.isEmpty() ? 0 : 1) : 0.0F;
      this.width = cellPadding * 2.0F;
      this.width += font.width(this.icon) + iconTextSpacing + textWidth;
   }

   @Native
   public void drawContent(CustomDrawContext ctx, float blockX, float blockY, float blockHeight, float iconSize, float iconTextSpacing, ColorRGBA iconColor, ColorRGBA textColor, Font font) {
      Font iconFont = Fonts.ICONS.getFont(6.0F);
      float var10000 = blockY + (blockHeight - iconFont.height()) / 2.0F;
      float textY = blockY + (blockHeight - font.height()) / 2.0F;
      if (this.icon != null && this.text == null) {
         var10000 = blockX + (this.width - iconSize) / 2.0F;
      } else if (this.text != null) {
         float contentBlockWidth = this.text != null ? font.width((String)this.text.get()) + font.width(this.prefix) + (float)(this.prefix.isEmpty() ? 0 : 1) : 0.0F;
         contentBlockWidth += font.width(this.icon) + iconTextSpacing + iconTextSpacing / 2.0F;
         float contentX = blockX + (this.width - contentBlockWidth) / 2.0F;
         if (this.icon != null) {
            ctx.drawText(iconFont, this.icon, contentX, textY, iconColor);
            contentX += iconSize + iconTextSpacing;
         }

         ctx.drawText(font, (String)this.text.get(), contentX, textY, textColor);
         ctx.drawText(font, this.prefix, contentX + font.width((String)this.text.get()) + 1.0F, textY, Javelin.getInstance().getThemeManager().getCurrentTheme().getGrayLight());
      }

   }

   @Generated
   public HudElement(String icon, Supplier<String> text, String prefix) {
      this.icon = icon;
      this.text = text;
      this.prefix = prefix;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }
}
