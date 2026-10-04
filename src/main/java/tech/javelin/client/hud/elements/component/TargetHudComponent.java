package tech.javelin.client.hud.elements.component;

import java.util.Iterator;
import java.util.List;
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
      this.setTarget((LivingEntity)target);
      if (this.toggleAnimationMetanoise.getValue() != 0.0F && this.target != null) {
         this.renderTargetHud(ctx, this.target, this.toggleAnimation.getValue());
      }
   }

   @Native
   private void renderTargetHud(CustomDrawContext ctx, LivingEntity target, float animation) {
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
      Iterator var11 = mc.getNetworkHandler().getPlayerList().iterator();

      while(var11.hasNext()) {
         PlayerListEntry playerListEntry = (PlayerListEntry)var11.next();
         if (playerListEntry.getProfile().getName().equals(target.getNameForScoreboard())) {
            skinTextures = playerListEntry.getSkinTextures().texture();
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
