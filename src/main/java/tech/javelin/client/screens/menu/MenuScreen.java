package tech.javelin.client.screens.menu;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.glfw.GLFW;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.animations.base.Animation;
import tech.javelin.base.animations.base.Easing;
import tech.javelin.base.font.Fonts;
import tech.javelin.base.font.MsdfRenderer;
import tech.javelin.base.theme.Theme;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.setting.Setting;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;
import tech.javelin.client.modules.api.setting.impl.KeySetting;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.javelin.client.modules.api.setting.impl.NumberSetting;
import tech.javelin.client.modules.impl.render.Menu;
import tech.javelin.client.screens.menu.components.UserComponent;
import tech.javelin.utility.game.other.ReplaceUtil;
import tech.javelin.utility.game.player.PlayerInventoryComponent;
import tech.javelin.utility.interfaces.IClient;
import tech.javelin.utility.interfaces.IMinecraft;
import tech.javelin.utility.math.MathUtil;
import tech.javelin.utility.render.display.Keyboard;
import tech.javelin.utility.render.display.StencilUtil;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.Gradient;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.base.color.ColorUtil;
import tech.javelin.utility.render.display.shader.DrawUtil;

public class MenuScreen extends Screen implements IClient {
   protected LinkedHashMap<Category, Animation> categories = new LinkedHashMap();
   protected LinkedHashMap<Module, Animation> descriptionAnimation = new LinkedHashMap();
   private final UserComponent userComponent = new UserComponent();
   private final int width = 353;
   private final int height = 199;
   private float needToScroll;
   protected Category currentCategory;
   protected final Animation openAnimation;
   public final Animation openAnimationMetanoise;
   protected final Animation animation;
   protected final Animation yCategoryAnimation;
   private final Animation scroll;
   private final Animation searchAnimation;
   private boolean firstLoad;
   public boolean needToClose;
   private Module currentModuleBinding;
   private KeySetting currentSettingBinding;
   private NumberSetting currentDrag;
   private Vec2f columns;
   public boolean search;
   private String searchText;
   private int cursorPosition;
   private int selectionStart;
   private int selectionEnd;
   private float animatedCursorX;
   private long lastInputTime;
   private float cursorAlpha;
   public Runnable savedRunnable;

   public MenuScreen() {
      super(Text.of("пов ти пидр"));
      this.currentCategory = Category.COMBAT;
      this.openAnimation = new Animation(250L, Easing.CUBIC_OUT);
      this.openAnimationMetanoise = new Animation(1650L, Easing.CUBIC_OUT);
      this.animation = new Animation(550L, Easing.CUBIC_OUT);
      this.yCategoryAnimation = new Animation(200L, Easing.CUBIC_OUT);
      this.scroll = new Animation(200L, Easing.CUBIC_OUT);
      this.searchAnimation = new Animation(200L, Easing.CUBIC_OUT);
      this.searchText = "";
      this.cursorPosition = 0;
      this.selectionStart = -1;
      this.selectionEnd = -1;
      this.animatedCursorX = 0.0F;
      this.lastInputTime = 0L;
      this.cursorAlpha = 1.0F;
      Category[] var1 = Category.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Category category = var1[var3];
         this.categories.put(category, new Animation(250L, Easing.CUBIC_OUT));
      }

      Iterator var5 = Javelin.getInstance().getModuleManager().getModules().iterator();

      while(var5.hasNext()) {
         Module module = (Module)var5.next();
         this.descriptionAnimation.put(module, new Animation(250L, Easing.CUBIC_OUT));
      }

   }

   @Native
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (!this.needToClose) {
         this.savedRunnable = new Runnable() {
            public void run() {
               IMinecraft.mc.getWindow().scaleFactor = 2.0D;
               int i3 = IMinecraft.mc.getWindow().framebufferWidth / 2;
               IMinecraft.mc.getWindow().scaledWidth = IMinecraft.mc.getWindow().framebufferWidth / 2 > i3 ? i3 + 1 : i3;
               int j3 = IMinecraft.mc.getWindow().framebufferHeight / 2;
               IMinecraft.mc.getWindow().scaledHeight = IMinecraft.mc.getWindow().framebufferHeight / 2 > j3 ? j3 + 1 : j3;
               if (!MenuScreen.this.needToClose) {
                  PlayerInventoryComponent.updateMoveKeys();
               }

               int x = IMinecraft.mc.getWindow().getScaledWidth() / 2 - 176;
               int y = IMinecraft.mc.getWindow().getScaledHeight() / 2 - 99;
               float lineX = 74.0F;
               MenuScreen.this.openAnimationMetanoise.setEasing(Easing.CIRC_OUT);
               if (MenuScreen.this.needToClose) {
                  MenuScreen.this.openAnimationMetanoise.setDuration(2200L);
               } else {
                  MenuScreen.this.openAnimationMetanoise.setDuration(1400L);
               }

               MenuScreen.this.updateScroll(y);
               MenuScreen.this.updateAnimations();
               Theme theme = Javelin.getInstance().getThemeManager().getCurrentTheme();
               CustomDrawContext drawContext = CustomDrawContext.of(context);
               StencilUtil.push();
               DrawUtil.drawMetanoise(drawContext.getMatrices(), (float)x, (float)y, 353.0F, 199.0F, MenuScreen.this.openAnimationMetanoise.getValue(), 10.0F, new ColorRGBA(0, 0, 0, 211), theme.getColor());
               StencilUtil.read(1);
               DrawUtil.drawBlur(drawContext.getMatrices(), (float)x, (float)y, 353.0F, 199.0F, 15.0F, BorderRadius.all(10.0F), new ColorRGBA(255, 255, 255, MenuScreen.this.openAnimation.getValue() * 255.0F));
               DrawUtil.drawMetanoise(drawContext.getMatrices(), (float)x, (float)y, 353.0F, 199.0F, MenuScreen.this.openAnimationMetanoise.getValue(), 10.0F, new ColorRGBA(0, 0, 0, 211), theme.getColor().withAlpha(100));
               DrawUtil.drawRoundedRect(drawContext.getMatrices(), (float)x + lineX, (float)y, 0.25F, 199.0F, BorderRadius.ZERO, (new ColorRGBA(-1)).withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F));
               DrawUtil.drawRoundedRect(drawContext.getMatrices(), (float)x, (float)(y + 31), 353.0F, 0.25F, BorderRadius.ZERO, (new ColorRGBA(-1)).withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F));
               DrawUtil.drawRoundedRect(drawContext.getMatrices(), (float)x, (float)(y + 173), 74.0F, 0.25F, BorderRadius.ZERO, (new ColorRGBA(-1)).withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.ICONS.getFont(5.5F), "7", (float)(x + 84), (float)(y + 13), new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.REGULAR.getFont(7.0F), "Javelin", (float)(x + 92), (float)(y + 13), new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.REGULAR.getFont(5.0F), ">", (float)(x + 116), (float)y + 13.5F, new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.REGULAR.getFont(7.0F), "Categories", (float)x + 128.5F, (float)y + 13.0F, new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.ICONS.getFont(5.5F), "4", (float)(x + 121), (float)(y + 13), new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.REGULAR.getFont(5.0F), ">", (float)(x + 165), (float)y + 13.5F, new ColorRGBA(67, 67, 67, MenuScreen.this.openAnimation.getValue() * 255.0F));
               drawContext.drawText(Fonts.ICONS.getFont(5.0F), MenuScreen.this.currentCategory.getIcon(), (float)x + 170.75F, (float)y + 13.5F, theme.getColor().withAlpha(255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
               drawContext.drawText(Fonts.REGULAR.getFont(6.5F), MenuScreen.this.currentCategory.getName(), (float)(x + 178), (float)y + 13.25F, (new ColorRGBA(-1)).withAlpha(255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
               drawContext.drawText(Fonts.REGULAR.getFont(7.0F), "Javelin", (float)(x + 22), (float)(y + 10), new ColorRGBA(247, 251, 252, MenuScreen.this.openAnimation.getValue() * 255.0F));
               MsdfRenderer.renderText(Fonts.ICONS, "B", 12.0F, Gradient.of(theme.getColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), theme.getSecondColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), theme.getColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), theme.getSecondColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F)), drawContext.getMatrices().peek().getPositionMatrix(), (float)(x + 6), (float)y + 11.25F, 0.0F, true, (float)(x + 6), (float)(x + 14));
               drawContext.drawText(Fonts.REGULAR.getFont(6.0F), "Minecraft client", (float)(x + 22), (float)(y + 18), new ColorRGBA(99, 99, 99, MenuScreen.this.openAnimation.getValue() * 255.0F));
               float categoryY = (float)(y + 42);

               int start;
               int end;
               for(Iterator var9 = MenuScreen.this.categories.entrySet().iterator(); var9.hasNext(); categoryY += 19.0F) {
                  Entry<Category, Animation> category = (Entry)var9.next();
                  ((Animation)category.getValue()).update(MenuScreen.this.currentCategory == category.getKey());
                  start = ColorUtil.interpolate(ColorRGBA.GRAY.withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), (new ColorRGBA(-1)).withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), ((Animation)category.getValue()).getValue()).getRGB();
                  end = ColorUtil.interpolate(ColorRGBA.GRAY.withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), theme.getColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F), ((Animation)category.getValue()).getValue()).getRGB();
                  if (MenuScreen.this.currentCategory == category.getKey()) {
                     if (!MenuScreen.this.firstLoad) {
                        MenuScreen.this.yCategoryAnimation.setValue(categoryY);
                        MenuScreen.this.yCategoryAnimation.setStartValue(categoryY);
                        MenuScreen.this.firstLoad = true;
                     } else {
                        MenuScreen.this.yCategoryAnimation.update(categoryY);
                     }
                  }

                  drawContext.drawText(Fonts.REGULAR.getFont(7.0F), ((Category)category.getKey()).getName(), (float)(x + 17), categoryY + 0.25F, new ColorRGBA(start));
                  drawContext.drawText(Fonts.ICONS.getFont(6.0F), ((Category)category.getKey()).getIcon(), (float)(x + 8), categoryY, new ColorRGBA(end));
               }

               DrawUtil.drawSquircle(drawContext.getMatrices(), (float)x + lineX - 2.5F, MenuScreen.this.yCategoryAnimation.getValue(), 2.5F, 6.5F, 5.0F, BorderRadius.all(0.5F), theme.getColor().withAlpha(MenuScreen.this.openAnimation.getValue() * 255.0F));
               float userX = (float)(x + 8);
               float userY = (float)(y + 199 - 6);
               MenuScreen.this.userComponent.render(context, userX, userY, MenuScreen.this.openAnimation.getValue());
               float moduleX;
               float moduleY;
               if (MenuScreen.this.currentCategory == Category.THEMES) {
                  moduleX = (float)(x + 83);
                  moduleY = (float)(y + 21);
                  int i = 0;
                  Iterator var48 = Javelin.getInstance().getThemeManager().getThemes().iterator();

                  while(var48.hasNext()) {
                     Theme theme1 = (Theme)var48.next();
                     if (i % 2 == 0) {
                        moduleX = (float)(x + 83);
                        moduleY += 18.0F;
                     }

                     DrawUtil.drawRoundedBorder(drawContext.getMatrices(), moduleX, moduleY, 129.0F, 15.0F, -1.0F, BorderRadius.all(2.0F), new ColorRGBA(211, 211, 211, Javelin.getInstance().getThemeManager().getCurrentTheme() == theme1 ? (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue() : (float)((int)(130.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()));
                     drawContext.drawText(Fonts.REGULAR.getFont(8.0F), theme1.getName(), moduleX + 4.0F, moduleY + 5.0F, (new ColorRGBA(222, 222, 222)).withAlpha(Javelin.getInstance().getThemeManager().getCurrentTheme() == theme1 ? (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue() : (float)((int)(130.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()));
                     theme1.drawTheme(drawContext.getMatrices(), Javelin.getInstance().getThemeManager().getCurrentTheme() == theme1 ? (double)((float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()) : (double)((float)((int)(130.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()));
                     moduleX += 133.0F;
                     ++i;
                     theme1.setX(moduleX);
                     theme1.setY(moduleY);
                  }

                  StencilUtil.pop();
               } else {
                  DrawUtil.drawRoundedRect(drawContext.getMatrices(), (float)x + lineX + 232.0F, (float)(y + 10), 38.0F, 11.0F, BorderRadius.all(2.0F), new ColorRGBA(30, 30, 30, MenuScreen.this.openAnimation.getValue() * 255.0F));
                  if (MenuScreen.this.searchAnimation.getValue() == 0.0F) {
                     MenuScreen.this.searchText = "";
                     MenuScreen.this.cursorPosition = 0;
                     MenuScreen.this.clearSelection();
                  }

                  float leftColumnY;
                  float leftColumnY2;
                  float rightColumnY;
                  float rightColumnY2;
                  if (MenuScreen.this.searchAnimation.getValue() != 0.0F) {
                     if (MenuScreen.this.search && MenuScreen.this.selectionStart != -1 && MenuScreen.this.selectionEnd != -1 && MenuScreen.this.selectionStart != MenuScreen.this.selectionEnd) {
                        start = Math.max(0, Math.min(MenuScreen.this.getStartOfSelection(), MenuScreen.this.searchText.length()));
                        end = Math.max(0, Math.min(MenuScreen.this.getEndOfSelection(), MenuScreen.this.searchText.length()));
                        if (start < end && start != 0) {
                           leftColumnY = (float)x + lineX + 235.0F + Fonts.REGULAR.getWidth(MenuScreen.this.searchText.substring(0, start), 7.0F);
                           leftColumnY2 = (float)x + lineX + 235.0F + Fonts.REGULAR.getWidth(MenuScreen.this.searchText.substring(0, end), 7.0F);
                           rightColumnY = leftColumnY2 - leftColumnY;
                           DrawUtil.drawRoundedRect(drawContext.getMatrices(), leftColumnY, (float)(y + 11), rightColumnY, 8.0F, BorderRadius.all(2.5F), theme.getColor().withAlpha((float)((int)(0.4F * MenuScreen.this.searchAnimation.getValue() * 255.0F)) * MenuScreen.this.openAnimation.getValue()));
                        }
                     }

                     String var10001 = MenuScreen.this.searchText;
                     MsdfRenderer.renderText(Fonts.REGULAR, var10001 + (MenuScreen.this.search && System.currentTimeMillis() % 1000L > 500L ? "|" : ""), 7.0F, (new ColorRGBA(222, 222, 222, (float)((int)(MenuScreen.this.searchAnimation.getValue() * 255.0F)) * MenuScreen.this.openAnimation.getValue())).getRGB(), drawContext.getMatrices().peek().getPositionMatrix(), (float)x + lineX + 235.0F, (float)y + 12.75F, 0.0F, true, 0.1F, 0.29F, 125.0F);
                     long currentTime = System.currentTimeMillis();
                     boolean isInputting = currentTime - MenuScreen.this.lastInputTime < 300L;
                     boolean shouldBlink = !isInputting && MenuScreen.this.search && (MenuScreen.this.selectionStart == -1 || MenuScreen.this.selectionStart == MenuScreen.this.selectionEnd);
                     if (!MenuScreen.this.search || MenuScreen.this.selectionStart != -1 && MenuScreen.this.selectionStart != MenuScreen.this.selectionEnd) {
                        MenuScreen.this.animatedCursorX = Fonts.REGULAR.getWidth(MenuScreen.this.searchText.substring(0, MenuScreen.this.cursorPosition), 7.0F);
                        MenuScreen.this.cursorAlpha = MathUtil.interpolateSmooth(3.0D, MenuScreen.this.cursorAlpha, 0.0F);
                     } else {
                        if (isInputting) {
                           MenuScreen.this.cursorAlpha = MathUtil.interpolateSmooth(2.0D, MenuScreen.this.cursorAlpha, 1.0F);
                        } else if (shouldBlink) {
                           boolean isBlinkPhase = currentTime % 1000L < 500L;
                           rightColumnY2 = isBlinkPhase ? 1.0F : 0.0F;
                           MenuScreen.this.cursorAlpha = MathUtil.interpolateSmooth(6.0D, MenuScreen.this.cursorAlpha, rightColumnY2);
                        } else {
                           MenuScreen.this.cursorAlpha = MathUtil.interpolateSmooth(6.0D, MenuScreen.this.cursorAlpha, 0.0F);
                        }

                        rightColumnY = Fonts.REGULAR.getWidth(MenuScreen.this.searchText.substring(0, MenuScreen.this.cursorPosition), 7.0F);
                        MenuScreen.this.animatedCursorX = MathUtil.interpolateSmooth(1.5D, MenuScreen.this.animatedCursorX, rightColumnY);
                        if (MenuScreen.this.cursorAlpha > 0.01F) {
                           ColorRGBA cursorColor = theme.getColor().withAlpha(MenuScreen.this.cursorAlpha * MenuScreen.this.searchAnimation.getValue() * MenuScreen.this.openAnimation.getValue());
                           DrawUtil.drawRoundedRect(drawContext.getMatrices(), (float)x + lineX + 235.0F + MenuScreen.this.animatedCursorX, (float)y + 11.5F, 1.0F, 7.0F, BorderRadius.all(0.5F), cursorColor);
                        }
                     }
                  }

                  if (MenuScreen.this.searchAnimation.getValue() != 1.0F) {
                     drawContext.drawText(Fonts.LUPA.getFont(5.25F), "\uf002", (float)x + lineX + 237.0F, (float)y + 13.75F, new ColorRGBA(222, 222, 222, (float)((int)(255.0F - MenuScreen.this.searchAnimation.getValue() * 255.0F)) * MenuScreen.this.openAnimation.getValue()));
                     drawContext.drawText(Fonts.REGULAR.getFont(7.0F), "Search", (float)x + lineX + 244.0F, (float)(y + 13), new ColorRGBA(222, 222, 222, (float)((int)(255.0F - MenuScreen.this.searchAnimation.getValue() * 255.0F)) * MenuScreen.this.openAnimation.getValue()));
                  }

                  moduleY = MenuScreen.this.scroll.getValue() + (float)y + 40.0F;
                  leftColumnY = moduleY;
                  leftColumnY2 = (float)(y + 40);
                  rightColumnY = moduleY;
                  rightColumnY2 = (float)(y + 40);
                  drawContext.enableScissor(x, y + 31, x + 353, y + 199);
                  Iterator var17 = Javelin.getInstance().getModuleManager().getModules().iterator();

                  label372:
                  while(true) {
                     Module module;
                     boolean placeLeft;
                     do {
                        do {
                           do {
                              if (!var17.hasNext()) {
                                 MenuScreen.this.columns = new Vec2f(leftColumnY2, rightColumnY2);
                                 StencilUtil.pop();
                                 drawContext.disableScissor();
                                 IMinecraft.mc.getWindow().scaleFactor = IMinecraft.mc.getWindow().getScaleFactor();
                                 int i2 = (int)((double)IMinecraft.mc.getWindow().framebufferWidth / IMinecraft.mc.getWindow().getScaleFactor());
                                 IMinecraft.mc.getWindow().scaledWidth = (double)IMinecraft.mc.getWindow().framebufferWidth / IMinecraft.mc.getWindow().getScaleFactor() > (double)i2 ? i2 + 1 : i2;
                                 int j2 = (int)((double)IMinecraft.mc.getWindow().framebufferHeight / IMinecraft.mc.getWindow().getScaleFactor());
                                 IMinecraft.mc.getWindow().scaledHeight = (double)IMinecraft.mc.getWindow().framebufferHeight / IMinecraft.mc.getWindow().getScaleFactor() > (double)j2 ? j2 + 1 : j2;
                                 return;
                              }

                              module = (Module)var17.next();
                           } while(module.getCategory() != MenuScreen.this.currentCategory && MenuScreen.this.searchText.isEmpty());
                        } while(!MenuScreen.this.searchText.isEmpty() && !MenuScreen.this.searchText.toLowerCase().contains(module.getName().toLowerCase()) && !module.getName().toLowerCase().contains(MenuScreen.this.searchText.toLowerCase()) && !ReplaceUtil.toQwerty(MenuScreen.this.searchText).contains(module.getName().toLowerCase()) && !module.getName().toLowerCase().contains(ReplaceUtil.toQwerty(MenuScreen.this.searchText).toLowerCase()));

                        placeLeft = leftColumnY <= rightColumnY;
                        moduleY = placeLeft ? leftColumnY : rightColumnY;
                     } while(moduleY >= (float)(y + 199));

                     float settingHeight = 0.0F;
                     Iterator var21 = module.getSettings().iterator();

                     while(true) {
                        Setting setting;
                        do {
                           if (!var21.hasNext()) {
                              moduleX = (float)(x + (placeLeft ? 83 : 216));
                              float baseHeight = 18.0F;
                              float totalHeight = baseHeight + settingHeight;
                              ColorRGBA color = ColorUtil.interpolate(new ColorRGBA(112, 112, 112, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), new ColorRGBA(255, 255, 255, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), module.getAnimation().getValue());
                              ColorRGBA colorTextModule = ColorUtil.interpolate(new ColorRGBA(155, 155, 155, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), new ColorRGBA(222, 222, 222, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), module.getAnimation().getValue());
                              ColorRGBA colorTextBindMark = ColorUtil.interpolate(new ColorRGBA(67, 67, 67, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), new ColorRGBA(111, 111, 111, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), module.getAnimation().getValue());
                              ColorRGBA colorTextIcon = ColorUtil.interpolate(new ColorRGBA(89, 89, 89, (float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha((float)((int)(255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()), module.getAnimation().getValue());
                              if (moduleY + baseHeight > (float)y - settingHeight + 40.0F) {
                                 DrawUtil.drawRoundedBorder(drawContext.getMatrices(), moduleX, moduleY, 129.0F, totalHeight, -1.0F, BorderRadius.all(4.0F), color);
                                 if (module.getAnimation().getValue() > 0.0F) {
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX, moduleY, 129.0F, totalHeight, BorderRadius.all(3.5F), (new ColorRGBA(22, 22, 22)).withAlpha(module.getAnimation().getValue() * 130.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
                                 }

                                 if (MenuScreen.this.currentModuleBinding != null && MenuScreen.this.currentModuleBinding == module) {
                                    DrawUtil.drawRoundedBorder(drawContext.getMatrices(), moduleX + 114.0F, moduleY + 4.0F, 10.0F, 10.0F, -1.0F, BorderRadius.all(2.0F), color);
                                    drawContext.drawText(Fonts.REGULAR.getFont(7.0F), "?", moduleX + 117.65F, moduleY + 6.5F, ColorUtil.gradient(3, 0, new ColorRGBA(65, 65, 65, MenuScreen.this.animation.getValue() * 255.0F * MenuScreen.this.openAnimation.getValue()), new ColorRGBA(144, 144, 144, MenuScreen.this.animation.getValue() * 255.0F * MenuScreen.this.openAnimation.getValue())));
                                 } else if (module.getKeyCode() != -1) {
                                    String bind = Keyboard.getKeyName(module.getKeyCode());
                                    DrawUtil.drawRoundedBorder(drawContext.getMatrices(), moduleX + 119.5F - Fonts.REGULAR.getWidth(bind, 7.0F), moduleY + 4.0F, 5.0F + Fonts.REGULAR.getWidth(bind, 7.15F), 10.0F, -1.0F, BorderRadius.all(2.0F), color);
                                    drawContext.drawText(Fonts.REGULAR.getFont(7.0F), Keyboard.getKeyName(module.getKeyCode()), moduleX + 122.85F - Fonts.REGULAR.getWidth(bind, 7.15F), moduleY + 6.75F, colorTextModule);
                                 } else {
                                    DrawUtil.drawRoundedBorder(drawContext.getMatrices(), moduleX + 114.0F, moduleY + 4.0F, 10.0F, 10.0F, -1.0F, BorderRadius.all(2.0F), color);
                                    drawContext.drawText(Fonts.ICONS.getFont(6.5F), "M", moduleX + 116.4F, moduleY + 6.5F, colorTextBindMark);
                                 }

                                 drawContext.drawText(Fonts.REGULAR.getFont(7.0F), module.getName(), moduleX + (float)(module.getInfo().description().isEmpty() ? 5 : 13), moduleY + 6.5F, colorTextModule);
                                 if (!module.getInfo().description().isEmpty()) {
                                    drawContext.drawText(Fonts.ICONS2.getFont(5.5F), "\uf05a", moduleX + 5.0F, moduleY + 7.25F, colorTextIcon);
                                    module.getDescAnimation().update(MathUtil.isHovered((double)mouseX, (double)mouseY, (double)(moduleX + 5.0F), (double)(moduleY + 6.0F), 4.5D, 5.0D));
                                    if (module.getDescAnimation().getValue() > 0.0F) {
                                       DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 12.5F, moduleY + 4.0F, Fonts.REGULAR.getWidth(module.getInfo().description(), 6.75F) + 5.0F, 10.0F, BorderRadius.all(2.0F), ColorUtil.interpolate(new ColorRGBA(89, 89, 89, (float)((int)(module.getDescAnimation().getValue() * 255.0F)) * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha((float)((int)(module.getDescAnimation().getValue() * 255.0F)) * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), module.getAnimation().getValue()));
                                       drawContext.drawText(Fonts.REGULAR.getFont(6.5F), module.getInfo().description(), moduleX + 15.0F, moduleY + 6.75F, new ColorRGBA(255, 255, 255, (float)((int)(module.getDescAnimation().getValue() * 255.0F)) * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
                                    }
                                 }
                              }

                              float settingY = moduleY + baseHeight;
                              Iterator var29 = module.getSettings().iterator();

                              while(true) {
                                 Setting settingx;
                                 do {
                                    do {
                                       if (!var29.hasNext()) {
                                          if (placeLeft) {
                                             leftColumnY2 += totalHeight + 5.0F;
                                             leftColumnY += totalHeight + 5.0F;
                                          } else {
                                             rightColumnY2 += totalHeight + 5.0F;
                                             rightColumnY += totalHeight + 5.0F;
                                          }
                                          continue label372;
                                       }

                                       settingx = (Setting)var29.next();
                                    } while(settingY <= (float)y - settingHeight + 40.0F);
                                 } while(settingx.getAnimationAlpha().getValue() == 0.0F && !settingx.isVisible());

                                 ColorRGBA colorText = new ColorRGBA(222, 222, 222, (float)((int)(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue());
                                 float newY;
                                 if (settingx instanceof BooleanSetting) {
                                    BooleanSetting bool = (BooleanSetting)settingx;
                                    settingY += 13.0F * settingx.getAnimationAlpha().getValue();
                                    newY = settingY - 13.0F;
                                    ColorRGBA colorCheckbox = ColorUtil.interpolate((new ColorRGBA(40, 40, 40)).withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), bool.getAnimation().getValue());
                                    ColorRGBA colorGalochka = new ColorRGBA(222, 222, 222, (float)((int)((float)((int)(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue())) * bool.getAnimation().getValue())) * MenuScreen.this.openAnimation.getValue());
                                    ColorRGBA colorMark = new ColorRGBA(100, 100, 100, (float)((int)((float)((int)(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue())) - bool.getAnimation().getValue() * 255.0F)) * MenuScreen.this.openAnimation.getValue());
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 4.5F, newY + 0.5F, 8.5F, 8.5F, BorderRadius.all(1.0F), colorCheckbox);
                                    drawContext.drawText(Fonts.ICONS.getFont(6.5F), "S", moduleX + 6.5F, newY + 2.35F, colorGalochka);
                                    drawContext.drawText(Fonts.ICONS.getFont(5.25F), "M", moduleX + 6.75F, newY + 2.85F, colorMark);
                                    drawContext.drawText(Fonts.REGULAR.getFont(7.0F), bool.getName(), moduleX + 16.25F, newY + 2.75F, colorText);
                                 }

                                 ColorRGBA colorMode;
                                 float min;
                                 float max;
                                 Iterator var68;
                                 if (settingx instanceof ModeSetting) {
                                    ModeSetting modex = (ModeSetting)settingx;
                                    settingY += 22.0F * settingx.getAnimationAlpha().getValue();
                                    newY = settingY - 22.0F;
                                    drawContext.drawText(Fonts.REGULAR.getFont(6.5F), modex.getName(), moduleX + 5.0F, newY + 2.0F, colorText);
                                    min = moduleX + 4.5F;
                                    max = newY + 9.0F;

                                    ModeSetting.Value valuexxxx;
                                    for(var68 = modex.getValues().iterator(); var68.hasNext(); min += Fonts.REGULAR.getWidth(valuexxxx.getName(), 6.15F) + 6.0F) {
                                       valuexxxx = (ModeSetting.Value)var68.next();
                                       if (min >= moduleX + 90.0F) {
                                          min = moduleX + 4.5F;
                                          max += 10.0F;
                                          settingY += 10.0F;
                                       }

                                       colorMode = ColorUtil.interpolate((new ColorRGBA(40, 40, 40)).withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), valuexxxx.getAnimation().getValue());
                                       DrawUtil.drawRoundedRect(drawContext.getMatrices(), min, max, Fonts.REGULAR.getWidth(valuexxxx.getName(), 6.25F) + 3.75F, 8.5F, BorderRadius.all(2.0F), colorMode);
                                       drawContext.drawText(Fonts.REGULAR.getFont(6.0F), valuexxxx.getName(), min + 2.5F, max + 2.0F, colorText);
                                    }
                                 }

                                 if (settingx instanceof MultiBooleanSetting) {
                                    MultiBooleanSetting modexx = (MultiBooleanSetting)settingx;
                                    settingY += 22.0F * settingx.getAnimationAlpha().getValue();
                                    newY = settingY - 22.0F;
                                    drawContext.drawText(Fonts.REGULAR.getFont(6.5F), modexx.getName(), moduleX + 5.0F, newY + 2.0F, colorText);
                                    min = moduleX + 4.5F;
                                    max = newY + 9.0F;

                                    MultiBooleanSetting.Value valuexx;
                                    for(var68 = modexx.getBooleanSettings().iterator(); var68.hasNext(); min += Fonts.REGULAR.getWidth(valuexx.getName(), 6.15F) + 6.0F) {
                                       valuexx = (MultiBooleanSetting.Value)var68.next();
                                       if (min >= moduleX + 90.0F) {
                                          min = moduleX + 4.5F;
                                          max += 10.0F;
                                          settingY += 10.0F;
                                       }

                                       colorMode = ColorUtil.interpolate((new ColorRGBA(40, 40, 40)).withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), valuexx.getAnimation().getValue());
                                       DrawUtil.drawRoundedRect(drawContext.getMatrices(), min, max, Fonts.REGULAR.getWidth(valuexx.getName(), 6.25F) + 3.75F, 8.5F, BorderRadius.all(2.0F), colorMode);
                                       drawContext.drawText(Fonts.REGULAR.getFont(6.0F), valuexx.getName(), min + 2.5F, max + 2.0F, colorText);
                                    }
                                 }

                                 if (settingx instanceof KeySetting) {
                                    KeySetting key = (KeySetting)settingx;
                                    settingY += 13.0F * settingx.getAnimationAlpha().getValue();
                                    newY = settingY - 13.0F;
                                    String keyDisplay = key.getNameKey();
                                    if (key.getKeyCode() == -1) {
                                       keyDisplay = "Нету";
                                    }

                                    if (MenuScreen.this.currentSettingBinding != null && MenuScreen.this.currentSettingBinding == key) {
                                       keyDisplay = "...";
                                    }

                                    drawContext.drawText(Fonts.REGULAR.getFont(7.0F), key.getName(), moduleX + 5.5F, newY + 2.5F, colorText);
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 123.0F - Fonts.REGULAR.getWidth(keyDisplay, 6.65F) - 2.25F, newY, Fonts.REGULAR.getWidth(keyDisplay, 6.65F) + 4.0F, 9.0F, BorderRadius.all(2.0F), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
                                    drawContext.drawText(Fonts.REGULAR.getFont(6.25F), keyDisplay, moduleX + 123.5F - Fonts.REGULAR.getWidth(keyDisplay, 6.65F), newY + 2.25F, colorText);
                                 }

                                 if (settingx instanceof NumberSetting) {
                                    NumberSetting number = (NumberSetting)settingx;
                                    settingY += 16.0F * settingx.getAnimationAlpha().getValue();
                                    newY = settingY - 16.0F;
                                    min = number.getMin();
                                    max = number.getMax();
                                    float inc = number.getIncrement();
                                    if (MenuScreen.this.currentDrag == number) {
                                       float percent = ((float)mouseX - (moduleX + 4.5F)) / 120.0F;
                                       percent = MathHelper.clamp(percent, 0.0F, 1.0F);
                                       float valuexxx = min + percent * (max - min);
                                       float steps = (float)Math.round(valuexxx / inc);
                                       valuexxx = steps * inc;
                                       number.setCurrent(MathHelper.clamp(valuexxx, min, max));
                                    }

                                    int scale = Math.max(0, String.valueOf(inc).contains(".") ? String.valueOf(inc).split("\\.")[1].length() : 0);
                                    String format = "%." + scale + "f";
                                    String bdDisplay = String.format(Locale.US, format, number.getCurrent()).replaceAll("\\.?0+$", "");
                                    drawContext.drawText(Fonts.REGULAR.getFont(6.5F), number.getName(), moduleX + 5.0F, newY + 1.5F, colorText);
                                    drawContext.drawText(Fonts.REGULAR.getFont(6.5F), bdDisplay, moduleX + 125.0F - Fonts.REGULAR.getWidth(bdDisplay, 7.0F), newY + 1.5F, colorText);
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 4.5F, newY + 9.0F, 120.0F, 3.0F, BorderRadius.all(0.5F), new ColorRGBA(40, 40, 40, (float)((int)(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()));
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 4.5F, newY + 9.0F, 120.0F * (number.getCurrent() - number.getMin()) / (number.getMax() - number.getMin()), 3.0F, BorderRadius.all(0.5F), theme.getColor().darker(0.75F).withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().darker(0.75F).withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()), theme.getColor().withAlpha(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue() * MenuScreen.this.openAnimation.getValue()));
                                    DrawUtil.drawRoundedRect(drawContext.getMatrices(), moduleX + 1.5F + MathHelper.clamp(120.0F * (number.getCurrent() - number.getMin()) / (number.getMax() - number.getMin()), 3.0F, 119.0F), newY + 8.0F, 5.0F, 5.0F, BorderRadius.all(2.0F), new ColorRGBA(255, 255, 255, (float)((int)(settingx.getAnimationAlpha().getValue() * 255.0F * MenuScreen.this.animation.getValue())) * MenuScreen.this.openAnimation.getValue()));
                                 }
                              }
                           }

                           setting = (Setting)var21.next();
                           setting.getAnimationAlpha().update(setting.isVisible());
                        } while(setting.getAnimationAlpha().getValue() == 0.0F && !setting.isVisible());

                        if (setting instanceof BooleanSetting) {
                           settingHeight += 13.0F * setting.getAnimationAlpha().getValue();
                        }

                        float valueX;
                        Iterator var25;
                        if (setting instanceof ModeSetting) {
                           ModeSetting modexxx = (ModeSetting)setting;
                           settingHeight += 22.0F * setting.getAnimationAlpha().getValue();
                           valueX = 87.5F;

                           ModeSetting.Value valuex;
                           for(var25 = modexxx.getValues().iterator(); var25.hasNext(); valueX += Fonts.REGULAR.getWidth(valuex.getName(), 6.15F) + 6.0F) {
                              valuex = (ModeSetting.Value)var25.next();
                              if (valueX >= 173.0F) {
                                 valueX = 87.5F;
                                 settingHeight += 10.0F;
                              }
                           }
                        }

                        if (setting instanceof MultiBooleanSetting) {
                           MultiBooleanSetting mode = (MultiBooleanSetting)setting;
                           settingHeight += 22.0F * setting.getAnimationAlpha().getValue();
                           valueX = 87.5F;

                           MultiBooleanSetting.Value value;
                           for(var25 = mode.getBooleanSettings().iterator(); var25.hasNext(); valueX += Fonts.REGULAR.getWidth(value.getName(), 6.15F) + 6.0F) {
                              value = (MultiBooleanSetting.Value)var25.next();
                              if (valueX >= 173.0F) {
                                 valueX = 87.5F;
                                 settingHeight += 10.0F * setting.getAnimationAlpha().getValue();
                              }
                           }
                        }

                        if (setting instanceof KeySetting) {
                           settingHeight += 13.0F * setting.getAnimationAlpha().getValue();
                        }

                        if (setting instanceof NumberSetting) {
                           settingHeight += 16.0F * setting.getAnimationAlpha().getValue();
                        }
                     }
                  }
               }
            }
         };
         this.savedRunnable.run();
      }
   }

   @Native
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (this.currentModuleBinding != null) {
            this.currentModuleBinding = null;
            return super.mouseClicked(mouseX, mouseY, button);
         }

         this.currentSettingBinding = null;
         this.currentDrag = null;
      }

      if (this.currentSettingBinding != null) {
         this.currentSettingBinding.setKeyCode(button);
         this.currentSettingBinding = null;
         return super.mouseClicked(mouseX, mouseY, button);
      } else if (this.currentModuleBinding != null) {
         this.currentModuleBinding.setKeyCode(button);
         this.currentModuleBinding = null;
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         int x = mc.getWindow().getScaledWidth() / 2 - 176;
         int y = mc.getWindow().getScaledHeight() / 2 - 99;
         float categoryY = (float)(y + 42);
         Category[] var9 = Category.values();
         int var10 = var9.length;

         int i;
         Iterator var13;
         Module module;
         for(i = 0; i < var10; ++i) {
            Category category1 = var9[i];
            if (MathUtil.isHovered(mouseX, mouseY, (double)(x + 6), (double)(categoryY - 10.0F), 55.0D, 18.0D) && this.currentCategory != category1) {
               this.currentCategory = category1;
               this.animation.setValue(0.0F);
               this.animation.setStartValue(0.0F);
               var13 = Javelin.getInstance().getModuleManager().getModules().iterator();

               while(var13.hasNext()) {
                  module = (Module)var13.next();
                  Iterator var15 = module.getSettings().iterator();

                  while(var15.hasNext()) {
                     Setting setting = (Setting)var15.next();
                     setting.getAnimationAlpha().setValue(0.0F);
                     setting.getAnimationAlpha().setStartValue(0.0F);
                  }
               }
            }

            categoryY += 20.0F;
         }

         float moduleX;
         float moduleY;
         if (this.currentCategory == Category.THEMES) {
            moduleX = (float)(x + 83);
            moduleY = (float)(y + 21);
            i = 0;
            Iterator var33 = Javelin.getInstance().getThemeManager().getThemes().iterator();

            while(var33.hasNext()) {
               Theme theme1 = (Theme)var33.next();
               if (i % 2 == 0) {
                  moduleX = (float)(x + 83);
                  moduleY += 18.0F;
               }

               if (MathUtil.isHovered(mouseX, mouseY, (double)moduleX, (double)moduleY, 129.0D, 15.0D) && Javelin.getInstance().getThemeManager().getCurrentTheme() != theme1) {
                  theme1.getAnimation().setValue(0.0F);
                  theme1.startAnimation(Javelin.getInstance().getThemeManager().getCurrentTheme().getColor1(), Javelin.getInstance().getThemeManager().getCurrentTheme().getColor2());
                  Javelin.getInstance().getThemeManager().setCurrentTheme(theme1);
               }

               moduleX += 133.0F;
               ++i;
               theme1.setX(moduleX);
               theme1.setY(moduleY);
            }

            return super.mouseClicked(mouseX, mouseY, button);
         } else {
            if (MathUtil.isHovered(mouseX, mouseY, (double)(x + 74 + 227), (double)(y + 9), 45.0D, 12.0D)) {
               this.search = !this.search;
            } else {
               this.search = false;
            }

            moduleY = this.scroll.getValue() + (float)y + 40.0F;
            float leftColumnY = moduleY;
            float rightColumnY = moduleY;
            var13 = Javelin.getInstance().getModuleManager().getModules().iterator();

            label289:
            while(true) {
               boolean placeLeft;
               do {
                  do {
                     do {
                        if (!var13.hasNext()) {
                           return super.mouseClicked(mouseX, mouseY, button);
                        }

                        module = (Module)var13.next();
                     } while(module.getCategory() != this.currentCategory && this.searchText.isEmpty());
                  } while(!this.searchText.isEmpty() && !this.searchText.toLowerCase().contains(module.getName().toLowerCase()) && !module.getName().toLowerCase().contains(this.searchText.toLowerCase()) && !ReplaceUtil.toQwerty(this.searchText).contains(module.getName().toLowerCase()) && !module.getName().toLowerCase().contains(ReplaceUtil.toQwerty(this.searchText).toLowerCase()));

                  placeLeft = leftColumnY <= rightColumnY;
                  moduleY = placeLeft ? leftColumnY : rightColumnY;
               } while(moduleY >= (float)(y + 199));

               float settingHeight = 0.0F;
               Iterator var17 = module.getSettings().iterator();

               while(true) {
                  Setting setting;
                  float settingY;
                  Iterator var21;
                  do {
                     if (!var17.hasNext()) {
                        moduleX = (float)(x + (placeLeft ? 83 : 216));
                        float baseHeight = 18.0F;
                        float totalHeight = baseHeight + settingHeight;
                        if (MathUtil.isHovered(mouseX, mouseY, (double)moduleX, (double)moduleY, 129.0D, 17.0D) && moduleY + baseHeight > (float)y - settingHeight + 40.0F) {
                           if (button == 0 && this.currentModuleBinding == null) {
                              module.toggle();
                           }

                           if (button == 2) {
                              this.currentModuleBinding = module;
                           }
                        }

                        settingY = moduleY + baseHeight;
                        var21 = module.getSettings().iterator();

                        while(true) {
                           do {
                              do {
                                 if (!var21.hasNext()) {
                                    if (placeLeft) {
                                       leftColumnY += totalHeight + 5.0F;
                                    } else {
                                       rightColumnY += totalHeight + 5.0F;
                                    }
                                    continue label289;
                                 }

                                 setting = (Setting)var21.next();
                              } while(settingY <= (float)y - settingHeight + 40.0F);
                           } while(setting.getAnimationAlpha().getValue() <= 0.93F);

                           float newY;
                           if (setting instanceof BooleanSetting) {
                              BooleanSetting bool = (BooleanSetting)setting;
                              settingY += 13.0F * setting.getAnimationAlpha().getValue();
                              newY = settingY - 13.0F;
                              if (MathUtil.isHovered(mouseX, mouseY, (double)(moduleX + 4.5F), (double)(newY + 0.5F), 8.5D, 8.5D)) {
                                 bool.setEnabled(!bool.isEnabled());
                              }
                           }

                           float valueX;
                           float valueY;
                           Iterator var27;
                           if (setting instanceof ModeSetting) {
                              ModeSetting mode = (ModeSetting)setting;
                              settingY += 22.0F * setting.getAnimationAlpha().getValue();
                              newY = settingY - 22.0F;
                              valueX = moduleX + 4.5F;
                              valueY = newY + 9.0F;

                              ModeSetting.Value value;
                              for(var27 = mode.getValues().iterator(); var27.hasNext(); valueX += Fonts.REGULAR.getWidth(value.getName(), 6.15F) + 6.0F) {
                                 value = (ModeSetting.Value)var27.next();
                                 if (valueX >= moduleX + 90.0F) {
                                    valueX = moduleX + 4.5F;
                                    valueY += 10.0F;
                                    settingY += 10.0F;
                                 }

                                 if (MathUtil.isHovered(mouseX, mouseY, (double)valueX, (double)valueY, (double)(Fonts.REGULAR.getWidth(value.getName(), 6.25F) + 3.75F), 8.5D)) {
                                    mode.setValue(value);
                                 }
                              }
                           }

                           if (setting instanceof MultiBooleanSetting) {
                              MultiBooleanSetting mode = (MultiBooleanSetting)setting;
                              settingY += 22.0F * setting.getAnimationAlpha().getValue();
                              newY = settingY - 22.0F;
                              valueX = moduleX + 4.5F;
                              valueY = newY + 9.0F;

                              MultiBooleanSetting.Value value;
                              for(var27 = mode.getBooleanSettings().iterator(); var27.hasNext(); valueX += Fonts.REGULAR.getWidth(value.getName(), 6.15F) + 6.0F) {
                                 value = (MultiBooleanSetting.Value)var27.next();
                                 if (valueX >= moduleX + 90.0F) {
                                    valueX = moduleX + 4.5F;
                                    valueY += 10.0F;
                                    settingY += 10.0F;
                                 }

                                 if (MathUtil.isHovered(mouseX, mouseY, (double)valueX, (double)valueY, (double)(Fonts.REGULAR.getWidth(value.getName(), 6.25F) + 3.75F), 8.5D)) {
                                    value.setEnabled(!value.isEnabled());
                                 }
                              }
                           }

                           if (setting instanceof KeySetting) {
                              KeySetting key = (KeySetting)setting;
                              settingY += 13.0F * setting.getAnimationAlpha().getValue();
                              newY = settingY - 13.0F;
                              String keyDisplay = key.getNameKey();
                              if (key.getKeyCode() == -1) {
                                 keyDisplay = "Нету";
                              }

                              if (this.currentSettingBinding != null && this.currentSettingBinding == key) {
                                 keyDisplay = "...";
                              }

                              if (MathUtil.isHovered(mouseX, mouseY, (double)(moduleX + 123.0F - Fonts.REGULAR.getWidth(keyDisplay, 6.65F) - 2.25F), (double)newY, (double)(Fonts.REGULAR.getWidth(keyDisplay, 6.65F) + 4.0F), 9.0D)) {
                                 this.currentSettingBinding = key;
                              }
                           }

                           if (setting instanceof NumberSetting) {
                              NumberSetting number = (NumberSetting)setting;
                              settingY += 16.0F * setting.getAnimationAlpha().getValue();
                              newY = settingY - 16.0F;
                              if (MathUtil.isHovered(mouseX, mouseY, (double)(moduleX + 4.5F), (double)(newY + 7.0F), 120.0D, 7.0D)) {
                                 this.currentDrag = number;
                              }
                           }
                        }
                     }

                     setting = (Setting)var17.next();
                     setting.getAnimationAlpha().update(setting.isVisible());
                  } while(setting.getAnimationAlpha().getValue() <= 0.93F);

                  if (setting instanceof BooleanSetting) {
                     settingHeight += 13.0F * setting.getAnimationAlpha().getValue();
                  }

                  if (setting instanceof ModeSetting) {
                     ModeSetting mode = (ModeSetting)setting;
                     settingHeight += 22.0F * setting.getAnimationAlpha().getValue();
                     settingY = 87.5F;

                     ModeSetting.Value value;
                     for(var21 = mode.getValues().iterator(); var21.hasNext(); settingY += Fonts.REGULAR.getWidth(value.getName(), 6.15F) + 6.0F) {
                        value = (ModeSetting.Value)var21.next();
                        if (settingY >= 173.0F) {
                           settingY = 87.5F;
                           settingHeight += 10.0F;
                        }
                     }
                  }

                  if (setting instanceof MultiBooleanSetting) {
                     MultiBooleanSetting mode = (MultiBooleanSetting)setting;
                     settingHeight += 22.0F * setting.getAnimationAlpha().getValue();
                     settingY = 87.5F;

                     MultiBooleanSetting.Value value;
                     for(var21 = mode.getBooleanSettings().iterator(); var21.hasNext(); settingY += Fonts.REGULAR.getWidth(value.getName(), 6.15F) + 6.0F) {
                        value = (MultiBooleanSetting.Value)var21.next();
                        if (settingY >= 173.0F) {
                           settingY = 87.5F;
                           settingHeight += 10.0F * setting.getAnimationAlpha().getValue();
                        }
                     }
                  }

                  if (setting instanceof KeySetting) {
                     settingHeight += 13.0F * setting.getAnimationAlpha().getValue();
                  }

                  if (setting instanceof NumberSetting) {
                     settingHeight += 16.0F * setting.getAnimationAlpha().getValue();
                  }
               }
            }
         }
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.currentDrag = null;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.needToScroll = (float)((double)this.needToScroll + verticalAmount * 15.0D);
      return true;
   }

   @Native
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.needToClose = true;
         Menu.INSTANCE.setEnabled(false);
         this.currentModuleBinding = null;
         this.currentSettingBinding = null;
         this.currentDrag = null;
         this.search = false;
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         if (keyCode == 257) {
            this.search = false;
         }

         if (this.search) {
            if (Screen.hasControlDown()) {
               if (keyCode == 65) {
                  this.selectAllText();
                  return super.keyPressed(keyCode, scanCode, modifiers);
               }

               if (keyCode == 86) {
                  this.pasteFromClipboard();
                  return super.keyPressed(keyCode, scanCode, modifiers);
               }

               if (keyCode == 67) {
                  this.copyToClipboard();
                  return super.keyPressed(keyCode, scanCode, modifiers);
               }
            }

            if (keyCode == 259) {
               if (this.hasSelection()) {
                  this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
               } else if (this.cursorPosition > 0) {
                  this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
               }

               this.lastInputTime = System.currentTimeMillis();
            }

            if (keyCode == 263 && this.cursorPosition > 0) {
               --this.cursorPosition;
               this.lastInputTime = System.currentTimeMillis();
               this.updateSelectionAfterCursorMove();
            }

            if (keyCode == 262 && this.cursorPosition < this.searchText.length()) {
               ++this.cursorPosition;
               this.lastInputTime = System.currentTimeMillis();
               this.updateSelectionAfterCursorMove();
            }
         }

         if (this.currentSettingBinding != null) {
            if (keyCode == 261) {
               this.currentSettingBinding.setKeyCode(-1);
               this.currentSettingBinding = null;
               return super.keyPressed(keyCode, scanCode, modifiers);
            }

            this.currentSettingBinding.setKeyCode(keyCode);
            this.currentSettingBinding = null;
         }

         if (this.currentModuleBinding != null) {
            if (keyCode == 261) {
               this.currentModuleBinding.setKeyCode(-1);
               this.currentModuleBinding = null;
               return super.keyPressed(keyCode, scanCode, modifiers);
            }

            this.currentModuleBinding.setKeyCode(keyCode);
            this.currentModuleBinding = null;
         }

         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.search) {
         this.deleteSelectedText();
         this.searchText = this.searchText.substring(0, this.cursorPosition) + chr + this.searchText.substring(this.cursorPosition);
         ++this.cursorPosition;
         this.clearSelection();
         this.lastInputTime = System.currentTimeMillis();
      }

      return super.charTyped(chr, modifiers);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   private void updateScroll(int y) {
      if (this.columns != null) {
         float maxScroll = -(Math.max(this.columns.x, this.columns.y) - (float)(y + 199 - 2));
         this.needToScroll = MathHelper.clamp(this.needToScroll, Math.min(maxScroll, 0.0F), 0.0F);
         this.scroll.update(this.needToScroll);
      }
   }

   private void updateAnimations() {
      this.animation.update(1.0F);
      this.searchAnimation.update(!this.searchText.isEmpty() || this.search);
      this.openAnimation.update(!this.needToClose);
      this.openAnimationMetanoise.update(!this.needToClose);
   }

   private void pasteFromClipboard() {
      String clipboardText = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
      if (clipboardText != null) {
         this.replaceText(this.cursorPosition, this.cursorPosition, clipboardText);
         this.lastInputTime = System.currentTimeMillis();
      }

   }

   private void copyToClipboard() {
      if (this.hasSelection()) {
         GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), this.getSelectedText());
      }

   }

   private void selectAllText() {
      this.selectionStart = 0;
      this.selectionEnd = this.searchText.length();
      this.cursorPosition = this.searchText.length();
   }

   private void updateSelectionAfterCursorMove() {
      if (Screen.hasShiftDown()) {
         if (this.selectionStart == -1) {
            this.selectionStart = this.cursorPosition;
         }

         this.selectionEnd = this.cursorPosition;
      } else {
         this.clearSelection();
      }

   }

   private void replaceText(int start, int end, String replacement) {
      if (start < 0) {
         start = 0;
      }

      if (end > this.searchText.length()) {
         end = this.searchText.length();
      }

      if (start > end) {
         start = end;
      }

      this.searchText = this.searchText.substring(0, start) + replacement + this.searchText.substring(end);
      this.cursorPosition = start + replacement.length();
      this.clearSelection();
   }

   private boolean hasSelection() {
      return this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd;
   }

   private String getSelectedText() {
      return this.searchText.substring(this.getStartOfSelection(), this.getEndOfSelection());
   }

   private int getStartOfSelection() {
      return Math.min(this.selectionStart, this.selectionEnd);
   }

   private int getEndOfSelection() {
      return Math.max(this.selectionStart, this.selectionEnd);
   }

   private void clearSelection() {
      this.selectionStart = -1;
      this.selectionEnd = -1;
   }

   private void deleteSelectedText() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      }

   }
}
