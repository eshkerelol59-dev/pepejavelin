package tech.javelin.client.hud.elements.component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.network.PlayerListEntry;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.impl.misc.NameProtect;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

/**
 * Ватермарка в стиле Deluxe: стеклянная панель, лого с акцентом,
 * ник / fps / ping / время.
 */
public class DeluxeWatermarkComponent extends DraggableHudElement {
   private static final float HEIGHT = 16.0F;
   private static final float PADDING = 6.0F;

   public DeluxeWatermarkComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      float x = this.getX();
      float y = this.getY();
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();

      String fps = mc.getCurrentFps() + "fps";
      PlayerListEntry self = mc.getNetworkHandler() != null && mc.player != null ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) : null;
      String ping = (self != null ? self.getLatency() : 0) + "ms";
      String nick = mc.player != null ? (NameProtect.INSTANCE.isEnabled() ? NameProtect.getCustomName() : mc.player.getNameForScoreboard()) : "Javelin";
      String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

      float iconW = 11.0F;
      float iconH = 8.5F;
      float nameSize = 7.5F;
      float textSize = 6.75F;
      float textGap = 4.0F;
      float segmentGap = 7.0F;

      float nameW = Fonts.SEMIBOLD.getWidth(nick, nameSize);
      float fpsW = Fonts.REGULAR.getWidth(fps, textSize);
      float pingW = Fonts.REGULAR.getWidth(ping, textSize);
      float timeW = Fonts.REGULAR.getWidth(time, textSize);
      float width = PADDING + iconW + textGap + nameW + segmentGap + fpsW + segmentGap + 4.0F + pingW + segmentGap + 4.0F + timeW + PADDING;

      DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, HEIGHT, 11.0F, BorderRadius.all(3.5F), new ColorRGBA(16, 16, 16, 170));
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x, y + HEIGHT - 0.5F, width, 0.5F, BorderRadius.ZERO, theme.getColor().withAlpha(120.0F));
      DrawUtil.drawTexture(ctx.getMatrices(), tech.javelin.Javelin.id("icons/mainmenu/icons/logo.png"),
            x + PADDING, y + (HEIGHT - iconH) / 2.0F, iconW, iconH, theme.getColor());

      float textY = y + (HEIGHT - nameSize * 0.7F) / 2.0F;
      float drawX = x + PADDING + iconW + textGap;
      ctx.drawText(Fonts.SEMIBOLD.getFont(nameSize), nick, drawX, textY, ColorRGBA.WHITE);
      drawX += nameW + segmentGap;

      ctx.drawText(Fonts.REGULAR.getFont(textSize), fps, drawX, textY + 0.25F, new ColorRGBA(210, 210, 210, 255));
      drawX += fpsW + segmentGap;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), drawX, y + HEIGHT / 2.0F - 1.0F, 1.0F, 2.0F, BorderRadius.ZERO, new ColorRGBA(255, 255, 255, 40));
      drawX += 4.0F;
      ctx.drawText(Fonts.REGULAR.getFont(textSize), ping, drawX, textY + 0.25F, new ColorRGBA(210, 210, 210, 255));
      drawX += pingW + segmentGap;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), drawX, y + HEIGHT / 2.0F - 1.0F, 1.0F, 2.0F, BorderRadius.ZERO, new ColorRGBA(255, 255, 255, 40));
      drawX += 4.0F;
      ctx.drawText(Fonts.REGULAR.getFont(textSize), time, drawX, textY + 0.25F, new ColorRGBA(210, 210, 210, 255));

      this.width = width;
      this.height = HEIGHT;
   }
}
