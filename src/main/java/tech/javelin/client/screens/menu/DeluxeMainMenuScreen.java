package tech.javelin.client.screens.menu;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.javelin.Javelin;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.theme.Theme;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.base.color.ColorUtil;
import tech.javelin.utility.render.display.shader.DrawUtil;
import tech.javelin.utility.interfaces.IMinecraft;

/**
 * Главное меню в стиле Deluxe: фон на весь экран, левая колонка с лого,
 * кнопкой онлайн-игры, рядами действий и соцсетями, слайд/ховер анимации.
 */
public class DeluxeMainMenuScreen extends Screen implements IMinecraft {
   private static final float DESIGN_W = 960.0F;
   private static final float DESIGN_H = 540.0F;
   private static final float COLUMN_W = 329.5F;
   private static final float CONTENT_X = 51.5F;
   private static final float ROW_W = 226.5F;
   private static final float ROW_H = 19.5F;
   private static final float BUTTON_H = 37.0F;

   private static final Identifier BACKGROUND = Javelin.id("icons/mainmenu/menu.png");
   private static final Identifier BACKGROUND_BLUR = Javelin.id("icons/mainmenu/menu_blur.png");
   private static final Identifier LOGO = Javelin.id("icons/mainmenu/icons/logo.png");
   private static final Identifier PATTERN = Javelin.id("icons/mainmenu/icons/pattern.png");
   private static final Identifier ICON_SINGLEPLAYER = Javelin.id("icons/mainmenu/icons/singleplayer.png");
   private static final Identifier ICON_ACCOUNTS = Javelin.id("icons/mainmenu/icons/accounts.png");
   private static final Identifier ICON_COSMETICS = Javelin.id("icons/mainmenu/icons/cos_menu.png");
   private static final Identifier ICON_SETTINGS = Javelin.id("icons/mainmenu/icons/settings.png");
   private static final Identifier ICON_EXIT = Javelin.id("icons/mainmenu/icons/exit.png");
   private static final Identifier ICON_BELL = Javelin.id("icons/mainmenu/icons/bell.png");
   private static final Identifier ICON_CHEVRON = Javelin.id("icons/mainmenu/icons/chevron.png");
   private static final Identifier ICON_TELEGRAM = Javelin.id("icons/mainmenu/icons/telegram.png");
   private static final Identifier ICON_DISCORD = Javelin.id("icons/mainmenu/icons/discord.png");

   private static final ColorRGBA DESC_COLOR = new ColorRGBA(255, 255, 255, 145);
   private static final ColorRGBA ROW_TEXT_COLOR = new ColorRGBA(198, 198, 198, 255);
   private static final ColorRGBA DANGER_COLOR = new ColorRGBA(235, 64, 52, 255);
   private static final Logger LOG = LoggerFactory.getLogger(DeluxeMainMenuScreen.class);

   private final Map<String, Float> hovers = new HashMap<>();
   private final List<ActionRegion> actionRegions = new ArrayList<>();
   private final long startTime = System.currentTimeMillis();
   private float lastFrameTime = System.nanoTime();

   public DeluxeMainMenuScreen() {
      super(Text.of("mainmenu"));
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      float dt = this.frameDelta();
      float scale = Math.min(this.width / DESIGN_W, this.height / DESIGN_H);
      float offY = (this.height - DESIGN_H * scale) / 2.0F;
      this.actionRegions.clear();

      CustomDrawContext ctx = CustomDrawContext.of(context);
      MatrixStack matrices = ctx.getMatrices();
      Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();

      float columnProgress = this.easeOutQuad(this.elapsed(0L, 350L));
      float columnX = -COLUMN_W * scale * (1.0F - columnProgress);

      DrawUtil.drawTexture(matrices, BACKGROUND, 0.0F, 0.0F, this.width, this.height, ColorRGBA.WHITE);

      float visibleColumn = COLUMN_W * scale + Math.min(0.0F, columnX);
      if (visibleColumn > 1.0F) {
         context.enableScissor(0, 0, (int) visibleColumn, this.height);
         DrawUtil.drawTexture(matrices, BACKGROUND_BLUR, 0.0F, 0.0F, this.width, this.height, ColorRGBA.WHITE);
         context.disableScissor();
      }

      DrawUtil.drawRoundedRect(matrices, columnX, 0.0F, COLUMN_W * scale, this.height, BorderRadius.ZERO, new ColorRGBA(255, 255, 255, 3));
      DrawUtil.drawRoundedRect(matrices, columnX + COLUMN_W * scale - 0.5F * scale, 0.0F, 0.5F * scale, this.height, BorderRadius.ZERO, new ColorRGBA(255, 255, 255, 8));

      this.drawChrome(ctx, columnX, this.height, scale, (float) mouseX, (float) mouseY, dt, theme);
      this.drawMenuColumn(ctx, columnX, offY, scale, (float) mouseX, (float) mouseY, dt, theme);
   }

   private void drawMenuColumn(CustomDrawContext ctx, float columnX, float offY, float scale, float mouseX, float mouseY, float dt, Theme theme) {
      float contentX = columnX + CONTENT_X * scale;
      float headerProgress = this.easeOutQuad(this.elapsed(0L, 450L));
      ColorRGBA headerFade = ColorRGBA.WHITE.withAlpha(headerProgress * 255.0F);
      float headerX = contentX - (1.0F - headerProgress) * 16.0F * scale;

      this.text(ctx, "Javelin client  /  Главная", headerX, offY + 127.5F * scale, 6.5F * scale, this.mulAlpha(DESC_COLOR, headerProgress));
      this.text(ctx, "Рады видеть тебя,", headerX, offY + 142.0F * scale, 20.0F * scale, this.mulAlpha(ColorRGBA.WHITE, headerProgress));
      this.text(ctx, mc.getSession().getUsername() + ".", headerX, offY + 166.2F * scale, 20.0F * scale, this.mulAlpha(ColorRGBA.WHITE, headerProgress));
      this.text(ctx, "Всё готово - выбирай режим и начинай играть.", headerX, offY + 201.0F * scale, 7.5F * scale, this.mulAlpha(DESC_COLOR, headerProgress));

      this.drawOnlineButton(ctx, contentX, offY + 227.0F * scale, scale, mouseX, mouseY, dt, theme,
            () -> mc.setScreen(new MultiplayerScreen(this)));
      this.drawRow(ctx, contentX, offY + 270.0F * scale, scale, "singleplayer", ICON_SINGLEPLAYER, 6.0F,
            "Одиночная игра", null, ColorRGBA.WHITE, mouseX, mouseY, dt, () -> mc.setScreen(new SelectWorldScreen(this)));
      this.drawRow(ctx, contentX, offY + 295.5F * scale, scale, "accounts", ICON_ACCOUNTS, 6.0F,
            "Ваши аккаунты", ICON_BELL, DANGER_COLOR, mouseX, mouseY, dt, this::openAccounts);
      this.drawRow(ctx, contentX, offY + 321.0F * scale, scale, "cosmetics", ICON_COSMETICS, 6.5F,
            "Косметика", ICON_CHEVRON, DESC_COLOR, mouseX, mouseY, dt, () -> {
            });
      this.drawRow(ctx, contentX, offY + 346.5F * scale, scale, "settings", ICON_SETTINGS, 7.0F,
            "Настройки игры", null, ColorRGBA.WHITE, mouseX, mouseY, dt,
            () -> mc.setScreen(new OptionsScreen(this, mc.options)));
      this.drawRow(ctx, contentX, offY + 379.5F * scale, scale, "exit", ICON_EXIT, 7.0F,
            "Выйти из игры", ICON_CHEVRON, DESC_COLOR, mouseX, mouseY, dt, mc::scheduleStop);
   }

   private void drawOnlineButton(CustomDrawContext ctx, float x, float y, float scale, float mouseX, float mouseY, float dt, Theme theme, Runnable action) {
      float reveal = this.easeOutQuad(this.elapsed(70L, 450L));
      float buttonX = x - (1.0F - reveal) * 16.0F * scale;
      float hover = this.hover("Онлайн", this.isHovered(mouseX, mouseY, buttonX, y, ROW_W * scale, BUTTON_H * scale), dt);

      ColorRGBA accent = theme.getColor();
      ColorRGBA fill = ColorUtil.interpolate(accent, accent.brighter(0.35F), hover * 0.55F);
      DrawUtil.drawRoundedRect(ctx.getMatrices(), buttonX, y, ROW_W * scale, BUTTON_H * scale, BorderRadius.all(6.0F * scale), fill.withAlpha(reveal * 255.0F));
      DrawUtil.drawRoundedBorder(ctx.getMatrices(), buttonX, y, ROW_W * scale, BUTTON_H * scale, 0.5F * scale, BorderRadius.all(6.0F * scale), this.mulAlpha(accent.brighter(0.6F), reveal));

      DrawUtil.drawTexture(ctx.getMatrices(), PATTERN,
            buttonX + 173.0F * scale + hover * 2.0F * scale, y + 5.0F * scale, 44.0F * scale, 32.0F * scale,
            ColorRGBA.WHITE.withAlpha(0.25F * 255.0F * reveal));

      this.text(ctx, "Онлайн игра", buttonX + 8.5F * scale, y + 6.5F * scale, 7.0F * scale, this.mulAlpha(ColorRGBA.WHITE, reveal));
      this.text(ctx, "Подключайтесь к серверам и играйте вместе с другими.", buttonX + 8.5F * scale, y + 16.5F * scale, 5.0F * scale, this.mulAlpha(DESC_COLOR, reveal * 0.6F));

      if (reveal > 0.7F) {
         this.actionRegions.add(new ActionRegion(buttonX, y, ROW_W * scale, BUTTON_H * scale, action));
      }
   }

   private void drawRow(CustomDrawContext ctx, float x, float y, float scale, String key, Identifier icon, float iconSize,
                        String label, Identifier trailing, ColorRGBA trailingColor, float mouseX, float mouseY, float dt, Runnable action) {
      float reveal = this.easeOutQuad(this.elapsed(80L + this.rowIndex(key) * 70L, 450L));
      float rowX = x - (1.0F - reveal) * 16.0F * scale;
      float hover = this.hover(key, this.isHovered(mouseX, mouseY, rowX, y, ROW_W * scale, ROW_H * scale), dt);
      rowX += hover * 2.0F * scale;

      DrawUtil.drawRoundedRect(ctx.getMatrices(), rowX, y, ROW_W * scale, ROW_H * scale, BorderRadius.all(6.0F * scale),
            ColorRGBA.WHITE.withAlpha((0.031F + 0.05F * hover) * 255.0F * reveal));
      DrawUtil.drawRoundedBorder(ctx.getMatrices(), rowX, y, ROW_W * scale, ROW_H * scale, 0.5F * scale, BorderRadius.all(6.0F * scale),
            ColorRGBA.WHITE.withAlpha((0.078F + 0.09F * hover) * 255.0F * reveal));

      ColorRGBA contentColor = ColorUtil.interpolate(ROW_TEXT_COLOR, ColorRGBA.WHITE, hover);
      DrawUtil.drawTexture(ctx.getMatrices(), icon, rowX + (11.0F - iconSize / 2.0F) * scale, y + (ROW_H - iconSize) / 2.0F * scale,
            iconSize * scale, iconSize * scale, this.mulAlpha(contentColor, reveal));
      this.text(ctx, label, rowX + 18.0F * scale, y + 6.25F * scale, 7.0F * scale, this.mulAlpha(contentColor, reveal));
      if (trailing != null) {
         DrawUtil.drawTexture(ctx.getMatrices(), trailing, rowX + (214.5F - 3.5F + hover * 1.5F) * scale, y + (ROW_H - 7.0F) / 2.0F * scale,
               7.0F * scale, 7.0F * scale, this.mulAlpha(trailingColor, reveal));
      }

      if (reveal > 0.7F) {
         this.actionRegions.add(new ActionRegion(rowX, y, ROW_W * scale, ROW_H * scale, action));
      }
   }

   private void drawChrome(CustomDrawContext ctx, float columnX, float screenHeight, float scale, float mouseX, float mouseY, float dt, Theme theme) {
      float logoProgress = this.easeOutQuad(this.elapsed(60L, 420L));
      DrawUtil.drawTexture(ctx.getMatrices(), LOGO,
            columnX + CONTENT_X * scale, 40.0F * scale - (1.0F - logoProgress) * 8.0F * scale,
            13.5F * scale, 10.5F * scale, this.mulAlpha(theme.getColor(), logoProgress));

      float socialProgress = this.easeOutQuad(this.elapsed(300L, 420L));
      float socialY = screenHeight - 50.75F * scale;
      float telegramHover = this.hover("socialTelegram", this.isHovered(mouseX, mouseY, columnX + 47.5F * scale, screenHeight - 59.0F * scale, 16.5F * scale, 16.5F * scale), dt);
      DrawUtil.drawTexture(ctx.getMatrices(), ICON_TELEGRAM, columnX + (55.75F - 4.25F) * scale, socialY - 4.25F * scale - telegramHover * 1.5F * scale,
            8.5F * scale, 8.5F * scale, this.mulAlpha(ColorUtil.interpolate(ROW_TEXT_COLOR, ColorRGBA.WHITE, telegramHover), socialProgress));
      float discordHover = this.hover("socialDiscord", this.isHovered(mouseX, mouseY, columnX + 65.0F * scale, screenHeight - 59.0F * scale, 16.5F * scale, 16.5F * scale), dt);
      DrawUtil.drawTexture(ctx.getMatrices(), ICON_DISCORD, columnX + (73.25F - 4.25F) * scale, socialY - 4.25F * scale - discordHover * 1.5F * scale,
            8.5F * scale, 8.5F * scale, this.mulAlpha(ColorUtil.interpolate(ROW_TEXT_COLOR, ColorRGBA.WHITE, discordHover), socialProgress));

      if (socialProgress > 0.7F) {
         this.actionRegions.add(new ActionRegion(columnX + 47.5F * scale, screenHeight - 59.0F * scale, 16.5F * scale, 16.5F * scale,
               () -> Util.getOperatingSystem().open(URI.create("https://t.me/delnmt"))));
         this.actionRegions.add(new ActionRegion(columnX + 65.0F * scale, screenHeight - 59.0F * scale, 16.5F * scale, 16.5F * scale,
               () -> Util.getOperatingSystem().open(URI.create("https://discord.gg/wWsxQkF2tP"))));
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         for (int i = this.actionRegions.size() - 1; i >= 0; --i) {
            ActionRegion region = this.actionRegions.get(i);
            if (this.isHovered((float) mouseX, (float) mouseY, region.x(), region.y(), region.w(), region.h())) {
               region.action().run();
               return true;
            }
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   /**
    * Открывает окно аккаунтов мода IAS (In-Game Account Switcher).
    * IAS — внешний мод из run/mods, поэтому загружаем через рефлексию:
    * если мода нет, просто логируем и остаёмся в меню.
    */
   private void openAccounts() {
      try {
         Class<?> screenClass = Class.forName("ru.vidtu.ias.screen.AccountScreen");
         Object screen = screenClass.getConstructor(Screen.class).newInstance(this);
         mc.setScreen((Screen) screen);
      } catch (Throwable t) {
         LOG.warn("IAS not found, cannot open account screen", t);
      }
   }

   private void text(CustomDrawContext ctx, String value, float x, float y, float size, ColorRGBA color) {
      ctx.drawText(Fonts.SEMIBOLD.getFont(Math.max(1.0F, size)), value, x, y, color);
   }

   private ColorRGBA mulAlpha(ColorRGBA color, float factor) {
      return color.withAlpha(color.getAlpha() * factor);
   }

   private float hover(String key, boolean hovered, float dt) {
      float current = this.hovers.getOrDefault(key, 0.0F);
      current += ((hovered ? 1.0F : 0.0F) - current) * (1.0F - (float) Math.exp(-dt * 14.0));
      this.hovers.put(key, current);
      return current;
   }

   private boolean isHovered(float mouseX, float mouseY, float x, float y, float w, float h) {
      return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
   }

   private float elapsed(long delayMs, long durationMs) {
      return MathHelper.clamp((float) (System.currentTimeMillis() - this.startTime - delayMs) / (float) durationMs, 0.0F, 1.0F);
   }

   private float easeOutQuad(float t) {
      return 1.0F - (1.0F - t) * (1.0F - t);
   }

   private int rowIndex(String key) {
      return switch (key) {
         case "singleplayer" -> 2;
         case "accounts" -> 3;
         case "cosmetics" -> 4;
         case "settings" -> 5;
         case "exit" -> 6;
         default -> 1;
      };
   }

   private float frameDelta() {
      long now = System.nanoTime();
      float dt = (float) ((double) (now - this.lastFrameTime) / 1.0E9D);
      this.lastFrameTime = (float) now;
      return MathHelper.clamp(dt, 0.0F, 0.1F);
   }

   private record ActionRegion(float x, float y, float w, float h, Runnable action) {
   }
}
