package tech.javelin.client.hud.elements.component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.network.PlayerListEntry;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.font.MsdfFont;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.impl.misc.NameProtect;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class WatermarkComponent extends DraggableHudElement {
   public WatermarkComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Native
   public void render(CustomDrawContext ctx) {
      float x = this.getX();
      float y = this.getY();
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      String name = "Javelin";
      String fps = mc.getCurrentFps() + "fps";
      PlayerListEntry list = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
      float width = 92.5F + Fonts.REGULAR.getWidth(NameProtect.INSTANCE.isEnabled() ? NameProtect.getCustomName() : mc.player.getNameForScoreboard(), 7.75F) + Fonts.REGULAR.getWidth(list != null ? list.getLatency() + "ms" : "0ms", 7.25F) + Fonts.REGULAR.getWidth(fps, 7.25F);
      DrawUtil.drawBlur(ctx.getMatrices(), x - 0.5F, y - 1.5F, width, 14.25F, 5.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255));
      ctx.drawText(Fonts.ICONS.getFont(6.5F), "B", x + 3.5F, y + 3.0F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), name, x + 12.75F, y + 3.25F, new ColorRGBA(255, 255, 255, 255));
      x += 34.0F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 4.0F, y + 5.25F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf007", x + 10.0F, y + 3.75F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), NameProtect.INSTANCE.isEnabled() ? NameProtect.getCustomName() : mc.player.getNameForScoreboard(), x + 18.0F, y + 3.25F, new ColorRGBA(255, 255, 255, 255));
      x += Fonts.REGULAR.getWidth(NameProtect.INSTANCE.isEnabled() ? NameProtect.getCustomName() : mc.player.getNameForScoreboard(), 7.75F) + 14.0F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 6.0F, y + 5.25F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf1eb", x + 12.0F, y + 3.65F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), list != null ? list.getLatency() + "ms" : "0ms", x + 21.5F, y + 3.25F, new ColorRGBA(255, 255, 255, 255));
      x += Fonts.REGULAR.getWidth(list != null ? list.getLatency() + "ms" : "0ms", 7.25F) + 21.0F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 4.5F, y + 5.25F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf624", x + 11.0F, y + 3.95F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), fps, x + 19.0F, y + 3.25F, new ColorRGBA(255, 255, 255, 255));
      float x2 = this.getX();
      float y2 = this.getY() + 15.0F;
      String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
      float var10000 = 54.0F + Fonts.REGULAR.getWidth(mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null ? mc.getCurrentServerEntry().address : "Неизвестно", 7.25F) + Fonts.REGULAR.getWidth(time, 7.25F);
      MsdfFont var10001 = Fonts.REGULAR;
      String var10002 = String.format("%.1f", Javelin.getInstance().getServerHandler().getTPS());
      float width2 = var10000 + var10001.getWidth(var10002.replace(",", ".") + "tps", 7.25F);
      DrawUtil.drawBlur(ctx.getMatrices(), x2 - 0.5F, y2 - 1.5F, width2, 14.25F, 5.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255));
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf0ac", x2 + 3.0F, y2 + 4.0F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null ? mc.getCurrentServerEntry().address : "Неизвестно", x2 + 11.5F, y2 + 3.5F, new ColorRGBA(255, 255, 255, 255));
      x2 += Fonts.REGULAR.getWidth(mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null ? mc.getCurrentServerEntry().address : "Неизвестно", 7.25F) + 13.0F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x2 + 4.0F, y2 + 5.25F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf017", x2 + 10.0F, y2 + 3.85F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), time, x2 + 18.5F, y2 + 3.25F, new ColorRGBA(255, 255, 255, 255));
      x2 += Fonts.REGULAR.getWidth(time, 7.25F) + 17.0F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), x2 + 5.0F, y2 + 5.25F, 2.0F, 2.0F, BorderRadius.all(0.5F), theme.getColor());
      ctx.drawText(Fonts.ICONS2.getFont(6.0F), "\uf68f", x2 + 11.5F, y2 + 3.5F, theme.getColor());
      ctx.drawText(Fonts.REGULAR.getFont(7.25F), String.format("%.1f", Javelin.getInstance().getServerHandler().getTPS()).replace(",", ".") + "tps", x2 + 19.0F, y2 + 3.25F, new ColorRGBA(255, 255, 255, 255));
      this.width = width;
      this.height = 29.0F;
   }
}
