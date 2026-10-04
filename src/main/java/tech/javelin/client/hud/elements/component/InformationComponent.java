package tech.javelin.client.hud.elements.component;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.events.impl.other.EventWindowResize;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.impl.render.Interface;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class InformationComponent extends DraggableHudElement {
   private final Animation yAnimation;

   public InformationComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.yAnimation = new Animation(200L, Easing.CUBIC_OUT);
      EventManager.register(this);
   }

   @EventTarget
   private void onWindowResized(EventWindowResize e) {
      if (mc.currentScreen instanceof ChatScreen) {
         this.yAnimation.setValue((float)(mc.getWindow().getScaledHeight() - 15));
         this.yAnimation.setStartValue((float)(mc.getWindow().getScaledHeight() - 15));
      } else {
         this.yAnimation.setStartValue((float)mc.getWindow().getScaledHeight());
         this.yAnimation.setValue((float)mc.getWindow().getScaledHeight());
      }
   }

   @Native
   public void render(CustomDrawContext ctx) {
      if (Interface.INSTANCE.getModeSetting().is("Minced")) {
         this.renderMinced(ctx);
      } else {
         this.renderJavelin(ctx);
      }
   }

   @Native
   private void renderJavelin(CustomDrawContext ctx) {
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      if (mc.currentScreen instanceof ChatScreen) {
         this.yAnimation.update((float)(mc.getWindow().getScaledHeight() - 15));
      } else {
         this.yAnimation.update((float)mc.getWindow().getScaledHeight());
      }

      int px = (int)Math.floor(mc.player.getX());
      int py = (int)Math.floor(mc.player.getY());
      int pz = (int)Math.floor(mc.player.getZ());
      double speed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
      String coordsText = String.format(Locale.US, "%d %d %d", px, py, pz);
      String speedText = String.format("%.2f", speed * 20.0D).replace(",", ".");
      DrawUtil.drawBlur(ctx.getMatrices(), 4.0F, this.yAnimation.getValue() - 17.0F, Fonts.REGULAR.getWidth(coordsText, 7.75F) + Fonts.REGULAR.getWidth(speedText, 7.75F) + 49.5F, 14.0F, 11.0F, BorderRadius.all(2.0F), new ColorRGBA(80, 80, 80, 255));
      ctx.drawText(Fonts.ICONS2.getFont(7.5F), "\uf57d", 7.75F, this.yAnimation.getValue() - 12.5F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.5F), coordsText, 18.0F, this.yAnimation.getValue() - 12.5F, ColorRGBA.WHITE);
      DrawUtil.drawRoundedRect(ctx.getMatrices(), 18.5F + Fonts.REGULAR.getWidth(coordsText, 7.75F) + 3.0F, this.yAnimation.getValue() - 11.0F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(7.5F), "\uf70c", 24.0F + Fonts.REGULAR.getWidth(coordsText, 7.75F) + 3.5F, this.yAnimation.getValue() - 12.5F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.5F), speedText + " Б/С", 24.0F + Fonts.REGULAR.getWidth(coordsText, 7.75F) + 12.0F, this.yAnimation.getValue() - 12.5F, ColorRGBA.WHITE);
   }

   @Native
   private void renderMinced(CustomDrawContext ctx) {
      if (mc.player == null) {
         return;
      }

      float panelX = this.getX();
      float panelY = this.getY();
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      float iconSize = 9.0F;
      float textSize = 7.0F;
      float leftPadding = 6.0F;
      float rightPadding = 6.0F;
      float iconTextGap = 2.0F;
      float segmentGap = 6.0F;
      float panelHeight = 17.0F;

      int px = (int)Math.floor(mc.player.getX());
      int py = (int)Math.floor(mc.player.getY());
      int pz = (int)Math.floor(mc.player.getZ());
      double bps = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20.0D;
      String bpsIcon = "r";
      String bpsText = String.format(Locale.US, "%.1fBPS", bps);
      String coordsIcon = "w";
      String coordsText = String.format(Locale.US, "x:%d y:%d z:%d", px, py, pz);
      float coordsIconOffsetX = -1.5F;

      float bpsSegmentWidth = Fonts.MINCED.getWidth(bpsIcon, iconSize) + iconTextGap + Fonts.MEDIUM.getWidth(bpsText, textSize);
      float coordsSegmentWidth = Fonts.MINCED.getWidth(coordsIcon, iconSize) + iconTextGap + Fonts.MEDIUM.getWidth(coordsText, textSize);
      float panelWidth = leftPadding + bpsSegmentWidth + segmentGap + coordsSegmentWidth + rightPadding;

      DrawUtil.drawBlur(ctx.getMatrices(), panelX, panelY, panelWidth, panelHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(40, 40, 40, 255));
      float drawX = panelX + leftPadding;
      float iconY = panelY + 5.2F;
      float textY = panelY + 5.5F;

      ctx.drawText(Fonts.MINCED.getFont(iconSize), bpsIcon, drawX, iconY, theme.getColor());
      drawX += Fonts.MINCED.getWidth(bpsIcon, iconSize) + iconTextGap;
      ctx.drawText(Fonts.MEDIUM.getFont(textSize), bpsText, drawX, textY, ColorRGBA.WHITE);
      drawX += Fonts.MEDIUM.getWidth(bpsText, textSize) + segmentGap;

      float coordsIconX = drawX + coordsIconOffsetX;
      ctx.drawText(Fonts.MINCED.getFont(iconSize), coordsIcon, coordsIconX, iconY + 0.5F, theme.getColor());
      drawX = coordsIconX + Fonts.MINCED.getWidth(coordsIcon, iconSize) + iconTextGap;
      ctx.drawText(Fonts.MEDIUM.getFont(textSize), coordsText, drawX, textY, ColorRGBA.WHITE);

      this.width = panelWidth;
      this.height = panelHeight;
   }
}
