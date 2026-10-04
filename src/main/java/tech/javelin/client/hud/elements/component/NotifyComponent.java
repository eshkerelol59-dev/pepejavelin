package tech.javelin.client.hud.elements.component;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Font;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.api.Module;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class NotifyComponent extends DraggableHudElement {
   private final Animation toggleAnimation;
   private final List<NotifyComponent.BaseNotification> notifications;

   public NotifyComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.toggleAnimation = new Animation(200L, Easing.CUBIC_OUT);
      this.notifications = new ArrayList();
   }

   public void addNotification(Module module, boolean enabled) {
      this.notifications.addLast(new NotifyComponent.ModuleNotification(module, enabled));
   }

   public void addTextNotification(String icon, Text text) {
      this.notifications.addLast(new NotifyComponent.TextNotification(icon, text));
   }

   public void addTotemNotification(String name, boolean enchanted) {
      this.notifications.addLast(new NotifyComponent.TotemNotification(name, enchanted));
   }

   @Native
   public void render(CustomDrawContext ctx) {
      Iterator<NotifyComponent.BaseNotification> iterator = this.notifications.iterator();
      this.toggleAnimation.update(mc.currentScreen instanceof ChatScreen && this.notifications.isEmpty());
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      Font textFont = Fonts.REGULAR.getFont(6.75F);
      Font iconFont = Fonts.ICONS2.getFont(6.75F);
      float notificationHeight = 12.0F;
      float x = (float)mc.getWindow().getScaledWidth() / 2.0F - 44.0F;
      float y = (float)mc.getWindow().getScaledHeight() / 2.0F + 16.0F;
      DrawUtil.drawBlur(ctx.getMatrices(), x + 0.5F, y, 88.0F, notificationHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * this.toggleAnimation.getValue()));
      ColorRGBA color = new ColorRGBA(76, 255, 76);
      String icon = "\uf058";
      if (System.currentTimeMillis() % 2500L > 500L) {
         icon = "\uf057";
         color = new ColorRGBA(255, 76, 76);
      }

      if (System.currentTimeMillis() % 2500L > 1000L) {
         icon = "\uf06a";
         color = new ColorRGBA(255, 234, 13);
      }

      if (System.currentTimeMillis() % 2500L > 1500L) {
         icon = "\uf05a";
         color = new ColorRGBA(255, 255, 255);
      }

      if (System.currentTimeMillis() % 2500L > 2000L) {
         icon = "";
         ctx.drawTexture(Identifier.of("minecraft", "textures/item/totem_of_undying.png"), x + 3.15F, y + 2.0F, 8.0F, 8.0F, ColorRGBA.WHITE.withAlpha(this.toggleAnimation.getValue() * 255.0F));
      }

      DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 13.0F, y + 1.0F, 0.5F, notificationHeight - 1.5F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * this.toggleAnimation.getValue()));
      if (System.currentTimeMillis() % 2500L < 2000L) {
         ctx.drawText(iconFont, icon, x + (14.5F - iconFont.width(icon)) / 2.0F, y + 0.25F + (notificationHeight - iconFont.height()) / 2.0F, color.withAlpha(255.0F * this.toggleAnimation.getValue()));
      }

      ctx.drawText(textFont, "Пример уведомления", x + 17.0F, y + (12.0F - textFont.height()) / 2.0F, ColorRGBA.WHITE.withAlpha(255.0F * this.toggleAnimation.getValue()));

      NotifyComponent.BaseNotification n;
      for(Iterator var11 = Lists.reverse(this.notifications).iterator(); var11.hasNext(); y += 8.0F * n.alphaAnimation.getValue()) {
         n = (NotifyComponent.BaseNotification)var11.next();
         y += 4.0F * n.alphaAnimation.getValue();
         n.render(ctx, (float)mc.getWindow().getScaledWidth() / 2.0F, y - 4.0F, textFont, theme, notificationHeight, this);
      }

      while(true) {
         while(iterator.hasNext()) {
            NotifyComponent.BaseNotification notification = (NotifyComponent.BaseNotification)iterator.next();
            if (!notification.fadingOut && System.currentTimeMillis() - notification.timestamp > 1500L) {
               notification.fadingOut = true;
               notification.alphaAnimation.update(0.0F);
            }

            if (notification.fadingOut && notification.alphaAnimation.getValue() < 0.01F) {
               iterator.remove();
            } else {
               notification.alphaAnimation.update(notification.fadingOut ? 0.0F : 1.0F);
            }
         }

         return;
      }
   }

   private static class ModuleNotification extends NotifyComponent.BaseNotification {
      final Module module;
      final boolean enabled;

      ModuleNotification(Module module, boolean enabled) {
         this.module = module;
         this.enabled = enabled;
      }

      @Native
      void render(CustomDrawContext ctx, float x, float y, Font textFont, Theme theme, float notificationHeight, NotifyComponent parent) {
         if (this.timestamp == 0L) {
            this.timestamp = System.currentTimeMillis();
         }

         float iconBgWidth = 16.0F;
         ColorRGBA textColor = this.enabled ? new ColorRGBA(76, 255, 76, this.alphaAnimation.getValue() * 255.0F) : new ColorRGBA(255, 76, 76, this.alphaAnimation.getValue() * 255.0F);
         String var10000 = this.module.getName();
         String moduleName = " " + var10000;
         String statusText = this.enabled ? " включена" : " выключена";
         float moduleNameWidth = Fonts.REGULAR.getWidth(moduleName, 7.25F);
         float statusTextWidth = textFont.width(statusText);
         float width = iconBgWidth + 4.0F + moduleNameWidth + statusTextWidth;
         Font iconFont = Fonts.ICONS2.getFont(6.75F);
         String icon = this.enabled ? "\uf058" : "\uf057";
         x -= width / 2.0F;
         DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, notificationHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, this.alphaAnimation.getValue() * 255.0F));
         DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 13.0F, y + 1.0F, 0.5F, notificationHeight - 1.5F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, this.alphaAnimation.getValue() * 255.0F));
         float iconX = x + (14.5F - iconFont.width(icon)) / 2.0F;
         float iconY = y + 0.25F + (notificationHeight - iconFont.height()) / 2.0F;
         ctx.drawText(iconFont, icon, iconX, iconY, textColor);
         float textX = x + iconBgWidth;
         float textY = y + (notificationHeight - textFont.height()) / 2.0F;
         ctx.drawText(textFont, moduleName, textX - 1.0F, textY, ColorRGBA.WHITE.withAlpha(this.alphaAnimation.getValue() * 255.0F));
         ctx.drawText(textFont, statusText, textX - 1.0F + moduleNameWidth, textY, textColor);
      }
   }

   private static class TextNotification extends NotifyComponent.BaseNotification {
      final String icon;
      final Text text;

      TextNotification(String icon, Text text) {
         this.icon = icon;
         this.text = text;
      }

      void render(CustomDrawContext ctx, float x, float y, Font textFont, Theme theme, float notificationHeight, NotifyComponent parent) {
         if (this.timestamp == 0L) {
            this.timestamp = System.currentTimeMillis();
         }

         float iconBgWidth = this.text.getString().contains("Игрок ") && this.text.getString().contains("просит о наблюдении") ? 9.0F : 14.0F;
         ColorRGBA textColor = this.icon.equals("\uf06a") ? new ColorRGBA(255, 234, 13, this.alphaAnimation.getValue() * 255.0F) : ColorRGBA.WHITE.withAlpha(this.alphaAnimation.getValue() * 255.0F);
         Text moduleName = this.text;
         float moduleNameWidth = textFont.width(moduleName);
         float width = iconBgWidth + (float)(this.text.getString().contains("Игрок ") && this.text.getString().contains("просит о наблюдении") ? 8 : 6) + moduleNameWidth;
         Font iconFont = Fonts.ICONS2.getFont(6.75F);
         String icon = this.icon;
         x -= width / 2.0F;
         DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, notificationHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, this.alphaAnimation.getValue() * 255.0F));
         DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 13.0F, y + 1.0F, 0.5F, notificationHeight - 1.5F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, this.alphaAnimation.getValue() * 255.0F));
         float iconX = x + (14.5F - iconFont.width(icon)) / 2.0F;
         float iconY = y + 0.25F + (notificationHeight - iconFont.height()) / 2.0F;
         ctx.drawText(iconFont, icon, iconX, iconY, textColor);
         float textX = x + iconBgWidth + 3.0F;
         float textY = y + (notificationHeight - textFont.height()) / 2.0F;
         ctx.drawText(textFont, moduleName, textX + (this.text.getString().contains("Игрок ") && this.text.getString().contains("просит о наблюдении") ? 5.5F : 0.0F), textY, this.alphaAnimation.getValue() * 255.0F);
      }
   }

   private static class TotemNotification extends NotifyComponent.BaseNotification {
      final String name;
      final boolean enchanted;

      TotemNotification(String icon, boolean enchanted) {
         this.name = icon;
         this.enchanted = enchanted;
      }

      void render(CustomDrawContext ctx, float x, float y, Font textFont, Theme theme, float notificationHeight, NotifyComponent parent) {
         if (this.timestamp == 0L) {
            this.timestamp = System.currentTimeMillis();
         }

         float iconBgWidth = 32.0F;
         String moduleName = "Игрок %s потерял тотем, зачарован: ".formatted(new Object[]{this.name});
         float moduleNameWidth = textFont.width(moduleName);
         float width = iconBgWidth + 4.0F + moduleNameWidth;
         x -= width / 2.0F;
         DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, notificationHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, this.alphaAnimation.getValue() * 255.0F));
         DrawUtil.drawRoundedRect(ctx.getMatrices(), x + 13.0F, y + 1.0F, 0.5F, notificationHeight - 1.5F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, this.alphaAnimation.getValue() * 255.0F));
         float iconX = x + 3.15F;
         float iconY = y + 2.0F;
         ctx.drawTexture(Identifier.of("minecraft", "textures/item/totem_of_undying.png"), iconX, iconY, 8.0F, 8.0F, ColorRGBA.WHITE.withAlpha(this.alphaAnimation.getValue() * 255.0F));
         float textX = x + iconBgWidth - 14.0F;
         float textY = y + (notificationHeight - textFont.height()) / 2.0F;
         ctx.drawText(textFont, moduleName, textX, textY, ColorRGBA.WHITE.withAlpha(this.alphaAnimation.getValue() * 255.0F));
         DrawUtil.drawRoundedRect(ctx.getMatrices(), x + width - 10.0F, y + 3.5F, 5.0F, 5.0F, BorderRadius.all(2.0F), new ColorRGBA(this.enchanted ? 32 : 255, this.enchanted ? 255 : 32, 32, this.alphaAnimation.getValue() * 255.0F));
      }
   }

   private abstract static class BaseNotification {
      long timestamp;
      boolean fadingOut = false;
      final Animation alphaAnimation;

      private BaseNotification() {
         this.alphaAnimation = new Animation(300L, Easing.CUBIC_OUT);
      }

      abstract void render(CustomDrawContext var1, float var2, float var3, Font var4, Theme var5, float var6, NotifyComponent var7);
   }
}
