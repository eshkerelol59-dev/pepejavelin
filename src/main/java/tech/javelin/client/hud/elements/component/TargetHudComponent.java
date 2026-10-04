package tech.javelin.client.hud.elements.component;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Font;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.font.MsdfRenderer;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.misc.NameProtect;
import tech.javelin.client.modules.impl.misc.ScoreboardHealth;
import tech.javelin.client.modules.impl.render.Interface;
import tech.javelin.utility.game.player.PlayerIntersectionUtil;
import tech.javelin.utility.mixin.accessors.DrawContextAccessor;
import tech.javelin.utility.render.display.StencilUtil;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class TargetHudComponent extends DraggableHudElement {
   private final Animation healthAnimation;
   private final Animation outdatedHealthAnimation;
   private final Animation gappleAnimation;
   private final Animation toggleAnimation;
   private final Animation toggleAnimationMetanoise;
   private LivingEntity target;

   public TargetHudComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
      this.healthAnimation = new Animation(250L, Easing.CUBIC_OUT);
      this.outdatedHealthAnimation = new Animation(650L, Easing.CUBIC_OUT);
      this.gappleAnimation = new Animation(250L, Easing.CUBIC_OUT);
      this.toggleAnimation = new Animation(250L, Easing.CUBIC_OUT);
      this.toggleAnimationMetanoise = new Animation(1850L, Easing.CUBIC_OUT);
   }

   @Native
   public void render(CustomDrawContext ctx) {
      Aura aura = Aura.INSTANCE;
      LivingEntity target = mc.currentScreen instanceof ChatScreen ? mc.player : aura.getTarget();
      this.setTarget(target);
      if (this.toggleAnimationMetanoise.getValue() != 0.0F && this.target != null) {
         if (Interface.INSTANCE.getModeSetting().is("Minced")) {
            this.renderMincedTargetHud(ctx, this.target, this.toggleAnimation.getValue());
         } else {
            this.renderJavelinTargetHud(ctx, this.target, this.toggleAnimation.getValue());
         }
      }
   }

   @Native
   private void renderJavelinTargetHud(CustomDrawContext ctx, LivingEntity target, float animation) {
      float posX = this.x;
      float posY = this.y;
      float width = 86.0F;
      float height = 30.0F;
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      float hp = ScoreboardHealth.INSTANCE.isEnabled() ? PlayerIntersectionUtil.getHealth(target) : target.getHealth();
      this.healthAnimation.update(hp / target.getMaxHealth());
      if (this.outdatedHealthAnimation.getValue() < this.healthAnimation.getValue()) {
         this.outdatedHealthAnimation.setValue(this.healthAnimation.getValue());
         this.outdatedHealthAnimation.setStartValue(this.healthAnimation.getValue());
      } else {
         this.outdatedHealthAnimation.update(hp / target.getMaxHealth());
      }

      this.gappleAnimation.update(target.getAbsorptionAmount() / target.getMaxHealth());
      StencilUtil.push();
      DrawUtil.drawMetanoise(ctx.getMatrices(), posX, posY, width, height, this.toggleAnimationMetanoise.getValue(), 3.0F, new ColorRGBA(0, 0, 0, 140), theme.getColor().withAlpha(255));
      StencilUtil.read(1);
      DrawUtil.drawBlur(ctx.getMatrices(), posX, posY, width, height, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(255, 255, 255, 255.0F * animation));
      DrawUtil.drawMetanoise(ctx.getMatrices(), posX, posY, width, height, this.toggleAnimationMetanoise.getValue(), 3.0F, new ColorRGBA(0, 0, 0, 140), theme.getColor().withAlpha(255));
      Identifier skinTextures = null;
      if (mc.getNetworkHandler() != null) {
         Iterator var11 = mc.getNetworkHandler().getPlayerList().iterator();
         while(var11.hasNext()) {
            PlayerListEntry playerListEntry = (PlayerListEntry)var11.next();
            if (playerListEntry.getProfile().getName().equals(target.getNameForScoreboard())) {
               skinTextures = playerListEntry.getSkinTextures().texture();
            }
         }
      }

      if (skinTextures == null) {
         skinTextures = DefaultSkinHelper.getSteve().texture();
      }

      DrawUtil.drawPlayerHeadWithRoundedShader(ctx.getMatrices(), skinTextures, posX + 4.0F, posY + 4.0F, 22.0F, BorderRadius.all(3.0F), ColorRGBA.WHITE.withAlpha(animation * 255.0F));
      MsdfRenderer.renderText(Fonts.REGULAR, target == mc.player ? NameProtect.getCustomName() : target.getNameForScoreboard(), 7.25F, ColorRGBA.WHITE.withAlpha(animation * 255.0F).getRGB(), ctx.getMatrices().peek().getPositionMatrix(), posX + 29.0F, posY + 5.5F, 0.0F, true, 0.7F, 1.0F, 56.0F);
      ctx.drawText(Fonts.REGULAR.getFont(6.5F), "HP: " + String.format("%.0f", hp) + (target.getAbsorptionAmount() > 0.0F ? String.format(" (%.1f)", target.getAbsorptionAmount()) : "").replace(",", "."), posX + 29.75F, posY + 14.25F, ColorRGBA.WHITE.withAlpha(animation * 255.0F));
      DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 29.0F, posY + 22.0F, width - 33.0F, 3.25F, BorderRadius.all(0.25F), theme.getSecondColor().darker(0.5F).withAlpha(animation * 255.0F), theme.getSecondColor().darker(0.5F).withAlpha(animation * 255.0F), theme.getColor().darker(0.5F).withAlpha(animation * 255.0F), theme.getColor().darker(0.5F).withAlpha(animation * 255.0F));
      DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 29.0F, posY + 22.0F, MathHelper.clamp((width - 33.0F) * this.outdatedHealthAnimation.getValue(), 0.0F, width - 33.0F), 3.25F, BorderRadius.all(0.25F), theme.getSecondColor().darker(0.35F).withAlpha(animation * 255.0F), theme.getSecondColor().darker(0.35F).withAlpha(animation * 255.0F), theme.getColor().darker(0.35F).withAlpha(animation * 255.0F), theme.getColor().darker(0.35F).withAlpha(animation * 255.0F));
      if (this.gappleAnimation.getValue() < this.healthAnimation.getValue()) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 29.0F, posY + 22.0F, MathHelper.clamp((width - 33.0F) * this.healthAnimation.getValue(), 0.0F, width - 33.0F), 3.25F, BorderRadius.all(0.25F), theme.getSecondColor().withAlpha(animation * 255.0F), theme.getSecondColor().withAlpha(animation * 255.0F), theme.getColor().withAlpha(animation * 255.0F), theme.getColor().withAlpha(animation * 255.0F));
      }

      DrawUtil.drawRoundedRect(ctx.getMatrices(), posX + 29.0F, posY + 22.0F, MathHelper.clamp((width - 33.0F) * this.gappleAnimation.getValue(), 0.0F, width - 33.0F), 3.25F, BorderRadius.all(0.25F), new ColorRGBA(255, 209, 0, animation * 255.0F), new ColorRGBA(255, 209, 0, animation * 255.0F), new ColorRGBA(255, 246, 20, animation * 255.0F), new ColorRGBA(255, 246, 20, animation * 255.0F));
      StencilUtil.pop();
      if (target instanceof PlayerEntity) {
         this.drawArmor(ctx, (PlayerEntity)target, posX + 3.0F, posY - 12.0F, 0.0F, 0.0F, 0.0F);
      }

      this.width = width;
      this.height = height;
   }

   @Native
   private void renderMincedTargetHud(CustomDrawContext ctx, LivingEntity target, float animation) {
      float posX = this.x;
      float posY = this.y;
      float headSize = 22.0F;
      float padding = 4.0F;
      float contentSpacing = 3.0F;
      float barHeight = 3.5F;
      float infoHeight = 30.0F;
      float bottomAreaHeight = 8.5F;
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      float hp = ScoreboardHealth.INSTANCE.isEnabled() ? PlayerIntersectionUtil.getHealth(target) : target.getHealth();
      this.healthAnimation.update(hp / target.getMaxHealth());
      if (this.outdatedHealthAnimation.getValue() < this.healthAnimation.getValue()) {
         this.outdatedHealthAnimation.setValue(this.healthAnimation.getValue());
         this.outdatedHealthAnimation.setStartValue(this.healthAnimation.getValue());
      } else {
         this.outdatedHealthAnimation.update(hp / target.getMaxHealth());
      }

      this.gappleAnimation.update(target.getAbsorptionAmount() / target.getMaxHealth());
      String displayName = target == mc.player ? NameProtect.getCustomName() : target.getNameForScoreboard();
      String hpText = String.format(Locale.US, "%.1fhp", hp);
      float nameWidth = Fonts.MEDIUM.getWidth(displayName, 8.0F);
      float hpWidth = Fonts.MEDIUM.getWidth(hpText, 7.0F);
      int equipmentCount = target instanceof PlayerEntity ? 6 : 0;
      float equipmentIconSize = 8.0F;
      float equipmentGap = 1.0F;
      float equipmentRowWidth = equipmentCount > 0 ? equipmentCount * equipmentIconSize + (equipmentCount - 1) * equipmentGap : 0.0F;
      float textAreaWidth = Math.max(62.0F, Math.max(nameWidth + 4.0F + hpWidth + 3.0F, equipmentRowWidth + 2.0F));
      float width = padding + headSize + contentSpacing + textAreaWidth + padding;
      float height = infoHeight + bottomAreaHeight;
      DrawUtil.drawBlur(ctx.getMatrices(), posX, posY, width, height, 11.0F, BorderRadius.all(3.0F), new ColorRGBA(40, 40, 40, 255.0F * animation));
      float headX = posX + padding;
      float headY = posY + padding;
      Identifier skinTexture = this.resolveSkinTexture(target);
      DrawUtil.drawPlayerHeadWithRoundedShader(ctx.getMatrices(), skinTexture, headX, headY, headSize, BorderRadius.all(3.0F), ColorRGBA.WHITE.withAlpha(animation * 255.0F));

      float textX = headX + headSize + contentSpacing;
      float nameY = posY + 6.0F;
      ctx.drawText(Fonts.MEDIUM.getFont(8.0F), displayName, textX, nameY, ColorRGBA.WHITE.withAlpha(animation * 255.0F));
      float hpX = posX + width - padding - hpWidth;
      ctx.drawText(Fonts.MEDIUM.getFont(7.0F), hpText, hpX, nameY + 0.2F, ColorRGBA.WHITE.withAlpha(animation * 255.0F));
      if (target instanceof PlayerEntity player && animation > 0.01F) {
         this.drawEquipmentRow(ctx, player, textX, nameY + 9.5F);
      }

      float healthProgress = MathHelper.clamp(this.healthAnimation.getValue(), 0.0F, 1.0F);
      float outdatedHealthProgress = MathHelper.clamp(this.outdatedHealthAnimation.getValue(), 0.0F, 1.0F);
      float absorptionProgress = MathHelper.clamp(this.gappleAnimation.getValue(), 0.0F, 1.0F);
      float barX = posX + padding;
      float barY = posY + height - padding - barHeight;
      float barWidth = width - padding * 2.0F;
      BorderRadius barRadius = new BorderRadius(1.0F, 1.0F, 1.0F, 1.0F);
      DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, barWidth, barHeight, barRadius, new ColorRGBA(20, 20, 20, animation * 220.0F));
      float delayedBarWidth = barWidth * outdatedHealthProgress;
      float healthBarWidth = barWidth * healthProgress;
      if (delayedBarWidth > 0.0F) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, delayedBarWidth, barHeight, barRadius, theme.getSecondColor().darker(0.35F).withAlpha(animation * 170.0F));
      }
      if (healthBarWidth > 0.0F) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, healthBarWidth, barHeight, barRadius, theme.getColor().withAlpha(animation * 255.0F));
      }
      if (absorptionProgress > healthProgress) {
         float absorbWidth = barWidth * Math.min(1.0F, absorptionProgress);
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, absorbWidth, barHeight, barRadius, new ColorRGBA(255, 213, 35, animation * 190.0F));
      }

      this.width = width;
      this.height = height;
   }

   private void drawEquipmentRow(CustomDrawContext ctx, PlayerEntity player, float x, float y) {
      float iconSize = 8.0F;
      float gap = 1.0F;
      float scale = iconSize / 16.0F;
      ItemStack[] stacks = new ItemStack[]{
         (ItemStack)player.getInventory().armor.get(3),
         (ItemStack)player.getInventory().armor.get(2),
         (ItemStack)player.getInventory().armor.get(1),
         (ItemStack)player.getInventory().armor.get(0),
         player.getMainHandStack(),
         player.getOffHandStack()
      };

      for(int i = 0; i < stacks.length; ++i) {
         ItemStack stack = stacks[i];
         if (!stack.isEmpty()) {
            float itemX = x + i * (iconSize + gap);
            ctx.getMatrices().push();
            ctx.getMatrices().translate((double)itemX, (double)y, 0.0D);
            ctx.getMatrices().scale(scale, scale, 1.0F);
            ctx.drawItem(stack, 0, 0);
            ctx.getMatrices().pop();
         }
      }
   }

   private Identifier resolveSkinTexture(LivingEntity target) {
      if (mc.getNetworkHandler() != null) {
         for(PlayerListEntry playerListEntry : mc.getNetworkHandler().getPlayerList()) {
            if (playerListEntry.getProfile().getName().equals(target.getNameForScoreboard())) {
               return playerListEntry.getSkinTextures().texture();
            }
         }
      }

      return DefaultSkinHelper.getSteve().texture();
   }

   private void drawArmor(CustomDrawContext ctx, PlayerEntity player, float posX, float posY, float headSize, float padding, float fontSize) {
      float boxSizeItem = 10.0F;
      float paddingItem = 0.0F;
      float iconX = posX + (5.0F - this.toggleAnimation.getValue() * 5.0F);
      float iconY = posY + 1.0F + (5.0F - this.toggleAnimation.getValue() * 5.0F);
      List<ItemStack> armor = player.getInventory().armor;
      ItemStack[] items = new ItemStack[]{player.getMainHandStack(), player.getOffHandStack(), (ItemStack)armor.get(3), (ItemStack)armor.get(2), (ItemStack)armor.get(1), (ItemStack)armor.get(0)};
      Font font = Fonts.MEDIUM.getFont(5.0F);
      ItemStack[] var15 = items;
      int var16 = items.length;

      for(int var17 = 0; var17 < var16; ++var17) {
         ItemStack stack = var15[var17];
         if (!stack.isEmpty()) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate((double)iconX + ((double)boxSizeItem - 9.6D) / 2.0D, (double)iconY + ((double)boxSizeItem - 9.6D) / 2.0D, 0.0D);
            ctx.getMatrices().scale(0.6F * this.toggleAnimation.getValue(), 0.6F * this.toggleAnimation.getValue(), 0.6F * this.toggleAnimation.getValue());
            ctx.drawItem(stack, 0, 0);
            ((DrawContextAccessor)ctx).callDrawItemBar(stack, 0, 0);
            ((DrawContextAccessor)ctx).callDrawCooldownProgress(stack, 0, 0);
            ctx.getMatrices().pop();
            iconX += boxSizeItem + paddingItem;
         }
      }
   }

   public void setTarget(LivingEntity target) {
      if (target == null) {
         this.toggleAnimation.update(0.0F);
         this.toggleAnimationMetanoise.update(0.0F);
         this.toggleAnimationMetanoise.setDuration(2200L);
         this.toggleAnimationMetanoise.setEasing(Easing.CIRC_OUT);
         if (this.toggleAnimationMetanoise.getValue() == 0.0F) {
            this.target = null;
         }
      } else {
         this.target = target;
         this.toggleAnimationMetanoise.update(1.0F);
         this.toggleAnimationMetanoise.setDuration(1300L);
         this.toggleAnimationMetanoise.setEasing(Easing.CIRC_OUT);
         this.toggleAnimation.update(1.0F);
      }
   }
}
