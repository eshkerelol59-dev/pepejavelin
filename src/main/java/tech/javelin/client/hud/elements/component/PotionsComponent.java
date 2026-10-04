package tech.javelin.client.hud.elements.component;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class PotionsComponent extends DraggableHudElement {
   private final BooleanSetting s1 = new BooleanSetting("1", true);
   private final BooleanSetting s2 = new BooleanSetting("2", true);
   private final BooleanSetting s3 = new BooleanSetting("3", true);
   private final BooleanSetting s4 = new BooleanSetting("4", true);
   private final BooleanSetting s5 = new BooleanSetting("5", true);
   private final BooleanSetting s6 = new BooleanSetting("6", true);
   private final BooleanSetting s7 = new BooleanSetting("7", true);
   private final Animation widthAnimation;
   private final Animation xLine;
   private final Animation alpha;
   private final List<PotionsComponent.PotionItem> potionItems;

   public PotionsComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.widthAnimation = new Animation(200L, Easing.CUBIC_OUT);
      this.xLine = new Animation(170L, Easing.SINE_OUT);
      this.alpha = new Animation(200L, Easing.CUBIC_OUT);
      this.potionItems = new CopyOnWriteArrayList();
   }

   @Native
   public void render(CustomDrawContext ctx) {
      if (mc.player != null) {
         this.updatePotions();
         float posX = this.getX();
         float posY = this.getY();
         float defaultWidth = 47.0F;
         float height = 14.5F;
         this.potionItems.sort(Comparator.comparing((pi) -> {
            return pi.name;
         }));
         boolean isFound = false;
         float durationWidth = 0.0F;
         Iterator var8 = this.potionItems.iterator();

         String duration;
         while(var8.hasNext()) {
            PotionsComponent.PotionItem item = (PotionsComponent.PotionItem)var8.next();
            item.animation.update(item.active);
            if (item.animation.getValue() != 0.0F) {
               int seconds = item.durationTicks / 20;
               int minutes = seconds / 60;
               int sec = seconds % 60;
               duration = String.format("%d:%02d", minutes, sec);
               durationWidth = Fonts.REGULAR.getWidth(duration, 6.75F) + 4.0F;
               height += 11.0F * item.animation.getValue();
               if (item.animation.getValue() != 0.0F) {
                  this.alpha.update(1.0F);
                  isFound = true;
               }
            }
         }

         this.xLine.update(durationWidth);
         if (!isFound && !(mc.currentScreen instanceof ChatScreen)) {
            this.alpha.update(0.0F);
         }

         if (mc.currentScreen instanceof ChatScreen) {
            this.alpha.update(1.0F);
         }

         Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
         DrawUtil.drawBlur(ctx.getMatrices(), posX, posY, this.widthAnimation.getValue(), 14.5F, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * this.alpha.getValue()));
         DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 15.0F, posY + 1.5F, 0.5F, 12.25F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * this.alpha.getValue()));
         ctx.drawText(Fonts.ICONS2.getFont(7.0F), "\uf6e1", posX + 5.0F, posY + 5.0F, theme.getColor().withAlpha(255.0F * this.alpha.getValue()));
         ctx.drawText(Fonts.REGULAR.getFont(7.0F), "Potions", posX + 19.5F, posY + 4.75F, (new ColorRGBA(-1)).withAlpha(255.0F * this.alpha.getValue()));
         posY += 14.5F;
         if (this.s1.isEnabled()) {
            Iterator var17 = this.potionItems.iterator();

            while(var17.hasNext()) {
               PotionsComponent.PotionItem item = (PotionsComponent.PotionItem)var17.next();
               if (item.animation.getValue() != 0.0F) {
                  String name = I18n.translate(item.name, new Object[0]);
                  String amp = this.getAmplifierText(item.amplifier);
                  duration = this.formatDuration(item.durationTicks);
                  Identifier icon = this.getEffectIcon((StatusEffect)item.effect.getEffectType().value());
                  height += 11.0F;
                  float elementsWidth = Fonts.REGULAR.getWidth(name, 6.75F) + Fonts.REGULAR.getWidth(amp, 6.75F) + Fonts.REGULAR.getWidth(duration, 6.75F) + 40.0F;
                  if (this.s2.isEnabled()) {
                     DrawUtil.drawBlur(ctx.getMatrices(), posX, posY + item.animation.getValue() * 3.0F - 3.0F, this.widthAnimation.getValue(), 11.0F, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(80, 80, 80, 255.0F * item.animation.getValue() * this.alpha.getValue()));
                  }

                  if (this.s3.isEnabled()) {
                     DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + this.widthAnimation.getValue() - 6.5F - this.xLine.getValue(), posY + item.animation.getValue() * 3.0F - 3.0F + 1.5F, 0.5F, 8.75F, BorderRadius.all(0.0F), new ColorRGBA(166, 166, 166, 255.0F * item.animation.getValue() * this.alpha.getValue()));
                  }

                  if (this.s4.isEnabled()) {
                     ctx.drawTexture(icon, posX + 2.5F, posY + item.animation.getValue() * 3.0F - 3.0F + 2.25F, 6.25F, 6.25F, ColorRGBA.WHITE.withAlpha(item.animation.getValue() * 255.0F * this.alpha.getValue()));
                  }

                  if (this.s5.isEnabled()) {
                     ctx.drawText(Fonts.REGULAR.getFont(6.5F), name, posX + 11.5F, posY + item.animation.getValue() * 3.0F - 3.0F + 3.25F, (new ColorRGBA(-1)).withAlpha(item.animation.getValue() * 255.0F * this.alpha.getValue()));
                  }

                  if (Integer.parseInt(amp) > 0 && this.s6.isEnabled()) {
                     ctx.drawText(Fonts.REGULAR.getFont(6.5F), amp, posX + Fonts.REGULAR.getWidth(name, 6.75F) + 13.5F, posY + item.animation.getValue() * 3.0F - 3.0F + 3.25F, theme.getColor().withAlpha(item.animation.getValue() * 255.0F * this.alpha.getValue()));
                  }

                  if (this.s7.isEnabled()) {
                     ctx.drawText(Fonts.REGULAR.getFont(6.5F), duration, posX + this.widthAnimation.getValue() - 3.0F - this.xLine.getValue() - Fonts.REGULAR.getWidth(duration, 6.75F) / 2.0F + this.xLine.getValue() / 2.0F, posY + item.animation.getValue() * 3.0F - 3.0F + 3.25F, (new ColorRGBA(-1)).withAlpha(item.animation.getValue() * 255.0F * this.alpha.getValue()));
                  }

                  if (elementsWidth > defaultWidth) {
                     defaultWidth = elementsWidth;
                  }

                  posY += 11.0F * item.animation.getValue();
               }
            }
         }

         this.widthAnimation.update(defaultWidth);
         this.width = this.widthAnimation.getValue();
         this.height = height;
      }
   }

   private String getAmplifierText(int amplifier) {
      return String.valueOf(amplifier + 1);
   }

   private String formatDuration(int durationTicks) {
      int totalSeconds = durationTicks / 20;
      int minutes = totalSeconds / 60;
      int seconds = totalSeconds % 60;
      return String.format("%02d:%02d", minutes, seconds);
   }

   private Identifier getEffectIcon(StatusEffect effect) {
      String id = effect.getTranslationKey().replace("effect.minecraft.", "").replace("effect.", "");
      return Identifier.of("minecraft", "textures/mob_effect/" + id + ".png");
   }

   @Native
   public void updatePotions() {
      if (mc.player != null) {
         Map<String, StatusEffectInstance> currentEffects = (Map)mc.player.getStatusEffects().stream().collect(Collectors.toMap((e) -> {
            String var10000 = Text.translatable(e.getTranslationKey()).getString();
            return var10000 + ":" + e.getAmplifier();
         }, (e) -> {
            return e;
         }, (e1, e2) -> {
            return e1;
         }));
         this.potionItems.forEach((item) -> {
            String key = item.name + ":" + item.amplifier;
            StatusEffectInstance effect = (StatusEffectInstance)currentEffects.get(key);
            if (effect != null) {
               item.durationTicks = effect.getDuration();
               if (!item.active) {
                  item.animation.setValue(1.0F);
               }

               item.active = true;
               currentEffects.remove(key);
            } else {
               item.active = false;
            }

         });
         currentEffects.forEach((key, effect) -> {
            this.potionItems.add(new PotionsComponent.PotionItem(Text.translatable(effect.getTranslationKey()).getString(), effect.getAmplifier(), effect.getDuration(), effect));
         });
         this.potionItems.removeIf((item) -> {
            return !item.active && item.animation.getValue() == 0.0F;
         });
      }
   }

   private static class PotionItem {
      String name;
      int amplifier;
      int durationTicks;
      boolean active;
      StatusEffectInstance effect;
      Animation animation;

      PotionItem(String name, int amplifier, int durationTicks, StatusEffectInstance effect) {
         this.animation = new Animation(250L, Easing.CUBIC_OUT);
         this.name = name;
         this.amplifier = amplifier;
         this.durationTicks = durationTicks;
         this.active = true;
         this.effect = effect;
      }
   }

   private static class PotionModule {
      private final Animation animation;
      private StatusEffectInstance effect;

      public PotionModule(StatusEffectInstance effect) {
         this.animation = new Animation(150L, 0.01F, Easing.QUAD_IN_OUT);
         this.effect = effect;
      }

      public boolean isDelete() {
         return this.animation.getValue() == 0.0F;
      }
   }
}
