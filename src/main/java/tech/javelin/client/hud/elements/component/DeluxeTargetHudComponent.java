package tech.javelin.client.hud.elements.component;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.misc.NameProtect;
import tech.javelin.client.modules.impl.misc.ScoreboardHealth;
import tech.javelin.utility.game.player.PlayerIntersectionUtil;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

/**
 * Таргет худ в стиле Deluxe: панель с головой цели, ником, HP и анимированной
 * полоской здоровья. Привязан к цели Aura.
 */
public class DeluxeTargetHudComponent extends DraggableHudElement {
   private static final float WIDTH = 118.0F;
   private static final float HEIGHT = 36.0F;

   private final Animation healthAnimation = new Animation(250L, Easing.CUBIC_OUT);
   private final Animation outdatedHealthAnimation = new Animation(650L, Easing.CUBIC_OUT);
   private final Animation absorptionAnimation = new Animation(250L, Easing.CUBIC_OUT);
   private final Animation toggleAnimation = new Animation(250L, Easing.CUBIC_OUT);
   private LivingEntity target;

   public DeluxeTargetHudComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, DraggableHudElement.Align align) {
      super(name, initialX, initialY, windowWidth, windowHeight, offsetX, offsetY, align);
   }

   @Override
   @Native
   public void render(CustomDrawContext ctx) {
      Aura aura = Aura.INSTANCE;
      LivingEntity target = mc.currentScreen instanceof ChatScreen ? mc.player : aura.getTarget();
      this.setTarget(target);
      float animation = this.toggleAnimation.getValue();
      if (this.target == null || animation <= 0.01F) {
         return;
      }

      float x = this.getX();
      float y = this.getY();
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
      float hp = ScoreboardHealth.INSTANCE.isEnabled() ? PlayerIntersectionUtil.getHealth(this.target) : this.target.getHealth();
      float healthProgress = MathHelper.clamp(hp / this.target.getMaxHealth(), 0.0F, 1.0F);
      this.healthAnimation.update(healthProgress);
      if (this.outdatedHealthAnimation.getValue() < this.healthAnimation.getValue()) {
         this.outdatedHealthAnimation.setValue(this.healthAnimation.getValue());
         this.outdatedHealthAnimation.setStartValue(this.healthAnimation.getValue());
      } else {
         this.outdatedHealthAnimation.update(healthProgress);
      }
      this.absorptionAnimation.update(MathHelper.clamp(this.target.getAbsorptionAmount() / this.target.getMaxHealth(), 0.0F, 1.0F));

      float scale = WIDTH * animation;
      float panelX = x + (WIDTH - scale) / 2.0F;
      DrawUtil.drawBlur(ctx.getMatrices(), panelX, y, scale, HEIGHT, 11.0F, BorderRadius.all(4.0F), new ColorRGBA(16, 16, 16, 190 * animation));
      DrawUtil.drawRoundedRect(ctx.getMatrices(), panelX, y + 6.0F, 1.25F, HEIGHT - 12.0F, BorderRadius.all(0.6F), theme.getColor().withAlpha(animation * 255.0F));

      DrawUtil.drawPlayerHeadWithRoundedShader(ctx.getMatrices(), this.resolveSkinTexture(this.target),
            panelX + 5.0F, y + 4.0F, 28.0F, BorderRadius.all(3.0F), ColorRGBA.WHITE.withAlpha(animation * 255.0F));

      float textX = panelX + 38.0F;
      String displayName = this.target == mc.player ? NameProtect.getCustomName() : this.target.getNameForScoreboard();
      ctx.drawText(Fonts.SEMIBOLD.getFont(7.5F), displayName, textX, y + 6.0F, ColorRGBA.WHITE.withAlpha(animation * 255.0F));

      String hpText = String.format(java.util.Locale.US, "%.1f", hp);
      float hpWidth = Fonts.SEMIBOLD.getWidth(hpText, 6.5F);
      ctx.drawText(Fonts.SEMIBOLD.getFont(6.5F), hpText, panelX + WIDTH - 4.0F - hpWidth, y + 6.25F, theme.getColor().withAlpha(animation * 255.0F));

      float barX = textX;
      float barY = y + HEIGHT - 9.5F;
      float barW = WIDTH - 38.0F - 6.0F;
      float barH = 3.5F;
      DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, barW, barH, BorderRadius.all(1.0F), new ColorRGBA(255, 255, 255, 30 * animation));
      float outdatedWidth = barW * MathHelper.clamp(this.outdatedHealthAnimation.getValue(), 0.0F, 1.0F);
      if (outdatedWidth > 0.0F) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, outdatedWidth, barH, BorderRadius.all(1.0F), new ColorRGBA(255, 255, 255, 70 * animation));
      }
      float healthWidth = barW * MathHelper.clamp(this.healthAnimation.getValue(), 0.0F, 1.0F);
      if (healthWidth > 0.0F) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, healthWidth, barH, BorderRadius.all(1.0F), theme.getColor().withAlpha(animation * 255.0F));
      }
      float absorptionProgress = MathHelper.clamp(this.absorptionAnimation.getValue(), 0.0F, 1.0F);
      if (absorptionProgress > healthProgress) {
         DrawUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, barW * absorptionProgress, barH, BorderRadius.all(1.0F), new ColorRGBA(255, 213, 35, 190 * animation));
      }

      this.width = WIDTH;
      this.height = HEIGHT;
   }

   private Identifier resolveSkinTexture(LivingEntity target) {
      if (mc.getNetworkHandler() != null) {
         for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile().getName().equals(target.getNameForScoreboard())) {
               return entry.getSkinTextures().texture();
            }
         }
      }
      return DefaultSkinHelper.getSteve().texture();
   }

   private void setTarget(LivingEntity target) {
      if (target == null) {
         this.toggleAnimation.update(0.0F);
         if (this.toggleAnimation.getValue() == 0.0F) {
            this.target = null;
         }
      } else {
         this.target = target;
         this.toggleAnimation.update(1.0F);
      }
   }
}
