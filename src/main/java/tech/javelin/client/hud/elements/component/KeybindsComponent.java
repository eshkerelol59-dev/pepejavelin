package tech.javelin.client.hud.elements.component;

import java.util.Iterator;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.impl.render.Interface;
import tech.javelin.utility.render.display.Keyboard;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class KeybindsComponent extends DraggableHudElement {
   private final Animation widthAnimation;
   private final Animation xLine;
   private final Animation alpha;

   public KeybindsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.widthAnimation = new Animation(200L, Easing.CUBIC_OUT);
      this.xLine = new Animation(170L, Easing.SINE_OUT);
      this.alpha = new Animation(200L, Easing.CUBIC_OUT);
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
      float posX = this.getX();
      float posY = this.getY();
      float defaultWidth = 53.0F;
      float height = 14.5F;
      boolean isFound = false;
      Iterator var7 = Javelin.getInstance().getModuleManager().getModules().iterator();

      while(var7.hasNext()) {
         Module module = (Module)var7.next();
         if (module.isEnabled() && module.getKeyCode() != -1) {
            this.alpha.update(1.0F);
            isFound = true;
         }
      }

      if (!isFound && !(mc.currentScreen instanceof ChatScreen)) {
         this.alpha.update(0.0F);
      }

      if (mc.currentScreen instanceof ChatScreen) {
         this.alpha.update(1.0F);
      }

      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      DrawUtil.drawBlur(ctx.getMatrices(), posX, posY, this.widthAnimation.getValue(), 14.5F, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * this.alpha.getValue()));
      DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 15.0F, posY + 1.5F, 0.5F, 12.25F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * this.alpha.getValue()));
      ctx.drawText(Fonts.ICONS2.getFont(7.0F), "\uf11c", posX + 4.0F, posY + 5.0F, theme.getColor().withAlpha(255.0F * this.alpha.getValue()));
      ctx.drawText(Fonts.REGULAR.getFont(7.0F), "KeyBinds", posX + 19.5F, posY + 4.75F, (new ColorRGBA(-1)).withAlpha(255.0F * this.alpha.getValue()));
      posY += 14.5F;
      float bindWidth = 0.0F;
      Iterator var9 = Javelin.getInstance().getModuleManager().getModules().iterator();

      Module module;
      while(var9.hasNext()) {
         module = (Module)var9.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            float localBindWidth = Fonts.REGULAR.getWidth(Keyboard.getKeyName(module.getKeyCode()), 6.75F);
            if (localBindWidth > bindWidth) {
               bindWidth = localBindWidth;
            }
         }
      }

      this.xLine.update(bindWidth);
      var9 = Javelin.getInstance().getModuleManager().getModules().iterator();

      while(var9.hasNext()) {
         module = (Module)var9.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            height += 11.0F;
            String bind = Keyboard.getKeyName(module.getKeyCode());
            String moduleName = module.getName();
            float elementsWidth = Fonts.REGULAR.getWidth(moduleName, 6.75F) + Fonts.REGULAR.getWidth(bind, 6.75F) + 40.0F;
            DrawUtil.drawBlur(ctx.getMatrices(), posX, posY + module.getAnimation().getValue() * 3.0F - 3.0F, this.widthAnimation.getValue(), 11.0F, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * module.getAnimation().getValue() * this.alpha.getValue()));
            DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + this.widthAnimation.getValue() - 6.5F - this.xLine.getValue(), posY + module.getAnimation().getValue() * 3.0F - 3.0F + 1.5F, 0.5F, 8.75F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * module.getAnimation().getValue() * this.alpha.getValue()));
            ctx.drawText(Fonts.ICONS.getFont(5.0F), module.getCategory().getIcon(), posX + 3.15F, posY + module.getAnimation().getValue() * 3.0F - 3.0F + 3.5F, theme.getColor().withAlpha(module.getAnimation().getValue() * 255.0F * this.alpha.getValue()));
            ctx.drawText(Fonts.REGULAR.getFont(6.5F), moduleName, posX + 10.5F, posY + module.getAnimation().getValue() * 3.0F - 3.0F + 3.25F, (new ColorRGBA(-1)).withAlpha(module.getAnimation().getValue() * 255.0F * this.alpha.getValue()));
            ctx.drawText(Fonts.REGULAR.getFont(6.5F), bind, posX + this.widthAnimation.getValue() - 3.0F - this.xLine.getValue() - Fonts.REGULAR.getWidth(bind, 6.75F) / 2.0F + this.xLine.getValue() / 2.0F, posY + module.getAnimation().getValue() * 3.0F - 3.0F + 3.25F, (new ColorRGBA(-1)).withAlpha(module.getAnimation().getValue() * 255.0F * this.alpha.getValue()));
            if (elementsWidth > defaultWidth) {
               defaultWidth = elementsWidth;
            }

            posY += 11.0F * module.getAnimation().getValue();
         }
      }

      this.widthAnimation.update(defaultWidth);
      this.width = this.widthAnimation.getValue();
      this.height = height;
   }

   @Native
   private void renderMinced(CustomDrawContext ctx) {
      float posX = this.getX();
      float posY = this.getY();
      float headerHeight = 17.0F;
      float rowHeight = 14.0F;
      float rowSpacing = 1.5F;
      float rowGap = 2.0F;
      float defaultWidth = 0.0F;
      float moduleIconLeftPadding = 5.0F;
      float moduleRightPadding = 6.0F;
      float titleX = 6.0F;
      float iconRightPadding = 3.0F;
      float bindRightPadding = 0.0F;
      float headerGap = 4.0F;
      String headerTitle = "Keybinds";
      String headerIcon = "t";
      float height = headerHeight;
      boolean isFound = false;
      Iterator var7 = Javelin.getInstance().getModuleManager().getModules().iterator();

      while(var7.hasNext()) {
         Module module = (Module)var7.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            isFound = true;
            break;
         }
      }

      if (isFound || mc.currentScreen instanceof ChatScreen) {
         this.alpha.update(1.0F);
      } else {
         this.alpha.update(0.0F);
      }

      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      float titleWidth = Fonts.MEDIUM.getWidth(headerTitle, 8.0F);
      float headerIconWidth = Fonts.MINCED.getWidth(headerIcon, 10.0F);
      float headerContentWidth = titleX + titleWidth + headerGap + headerIconWidth + iconRightPadding;
      defaultWidth = Math.max(defaultWidth, headerContentWidth);
      float bindWidth = 0.0F;
      Iterator var9 = Javelin.getInstance().getModuleManager().getModules().iterator();

      Module module;
      while(var9.hasNext()) {
         module = (Module)var9.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            float localBindWidth = Fonts.MEDIUM.getWidth(Keyboard.getKeyName(module.getKeyCode()), 6.75F);
            if (localBindWidth > bindWidth) {
               bindWidth = localBindWidth;
            }
         }
      }

      this.xLine.update(bindWidth);
      var9 = Javelin.getInstance().getModuleManager().getModules().iterator();

      float maxFunctionWidth = 0.0F;
      while(var9.hasNext()) {
         module = (Module)var9.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            float rowAnim = module.getAnimation().getValue();
            height += (rowHeight + rowSpacing) * rowAnim;
            String moduleName = module.getName();
            float functionWidth = moduleIconLeftPadding + Fonts.REGULAR.getWidth(moduleName, 7.0F) + moduleRightPadding;
            if (functionWidth > maxFunctionWidth) {
               maxFunctionWidth = functionWidth;
            }
         }
      }

      float functionBoxWidth = Math.max(12.0F, maxFunctionWidth);
      float bindBoxWidth = Math.max(12.0F, this.xLine.getValue() + 6.0F);
      float totalWidth = functionBoxWidth + rowGap + bindBoxWidth + bindRightPadding;
      this.widthAnimation.update(Math.max(defaultWidth, totalWidth));
      float panelWidth = this.widthAnimation.getValue();
      DrawUtil.drawBlur(ctx.getMatrices(), posX, posY, panelWidth, headerHeight, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(40, 40, 40, 255.0F * this.alpha.getValue()));
      float iconX = posX + panelWidth - iconRightPadding - headerIconWidth;
      ctx.drawText(Fonts.MINCED.getFont(10.0F), headerIcon, iconX, posY + 5.8F, theme.getColor().withAlpha(255.0F * this.alpha.getValue()));
      ctx.drawText(Fonts.MEDIUM.getFont(8.0F), headerTitle, posX + titleX, posY + 5.5F, ColorRGBA.WHITE.withAlpha(255.0F * this.alpha.getValue()));
      float currentY = posY + headerHeight + rowSpacing;
      var9 = Javelin.getInstance().getModuleManager().getModules().iterator();

      while(var9.hasNext()) {
         module = (Module)var9.next();
         if (module.getAnimation().getValue() != 0.0F && module.getKeyCode() != -1) {
            String bind = Keyboard.getKeyName(module.getKeyCode());
            String moduleName = module.getName();
            float rowAnim = module.getAnimation().getValue();
            float rowAlpha = rowAnim * this.alpha.getValue();
            float rowOffset = (1.0F - rowAnim) * 3.0F;
            float rowY = currentY + rowOffset;
            float bindBoxX = posX + panelWidth - bindBoxWidth - bindRightPadding - 1.0F;
            float functionWidth = Math.max(12.0F, bindBoxX - posX - rowGap);
            DrawUtil.drawBlur(ctx.getMatrices(), posX, rowY, functionWidth, rowHeight, 9.0F, BorderRadius.all(2.5F), new ColorRGBA(72, 72, 72, 255.0F * rowAlpha));
            DrawUtil.drawBlur(ctx.getMatrices(), bindBoxX, rowY, bindBoxWidth, rowHeight, 9.0F, BorderRadius.all(2.5F), new ColorRGBA(72, 72, 72, 255.0F * rowAlpha));
            float textX = posX + moduleIconLeftPadding;
            ctx.drawText(Fonts.MEDIUM.getFont(7.0F), moduleName, textX, rowY + 4.2F, ColorRGBA.WHITE.withAlpha(rowAlpha * 255.0F));
            float bindX = bindBoxX + (bindBoxWidth - Fonts.MEDIUM.getWidth(bind, 7.0F)) / 1.8F;
            ctx.drawText(Fonts.MEDIUM.getFont(7.0F), bind, bindX, rowY + 4.5F, ColorRGBA.WHITE.withAlpha(rowAlpha * 255.0F));
            currentY += (rowHeight + rowSpacing) * rowAnim;
         }
      }

      this.width = panelWidth;
      this.height = height;
   }
}
