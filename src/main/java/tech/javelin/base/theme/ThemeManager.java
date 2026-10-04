package tech.javelin.base.theme;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.base.color.ColorUtil;

public class ThemeManager {
   private Theme currentTheme;
   private final List<Theme> themes = new ArrayList();
   private final Theme defaultTheme = new Theme("Cyber Blue", (new Color(0, 200, 255, 255)).getRGB(), (new Color(10, 10, 20, 255)).getRGB());

   public ThemeManager() {
      this.initThemes();
   }

   @Native
   private void initThemes() {
      if (this.currentTheme == null) {
         this.currentTheme = this.defaultTheme;
      }

      this.themes.addAll(List.of(new Theme[]{this.defaultTheme, new Theme("Violet Void", (new Color(180, 0, 255, 255)).getRGB(), (new Color(30, 0, 40, 255)).getRGB()), new Theme("Sunset", (new Color(255, 94, 0, 255)).getRGB(), (new Color(40, 20, 0, 255)).getRGB()), new Theme("Neon Green", (new Color(57, 255, 20, 255)).getRGB(), (new Color(10, 20, 10, 255)).getRGB()), new Theme("Abyss Blue", (new Color(0, 102, 204, 255)).getRGB(), (new Color(10, 10, 30, 255)).getRGB()), new Theme("Cotton Candy", (new Color(255, 182, 193, 255)).getRGB(), (new Color(240, 240, 255, 255)).getRGB()), new Theme("Sky Breeze", (new Color(135, 206, 250, 255)).getRGB(), (new Color(220, 240, 255, 255)).getRGB()), new Theme("Obsidian Glow", (new Color(200, 200, 255, 255)).getRGB(), (new Color(10, 10, 15, 230)).getRGB()), new Theme("Quantum Shift", (new Color(100, 255, 230, 255)).getRGB(), (new Color(0, 20, 25, 220)).getRGB()), new Theme("White-Black", (new Color(255, 255, 255, 255)).getRGB(), (new Color(0, 0, 0, 255)).getRGB()), new Theme("Peach Cream", (new Color(255, 218, 185, 255)).getRGB(), (new Color(255, 245, 235, 255)).getRGB()), new Theme("Mint Ice", (new Color(189, 255, 201, 255)).getRGB(), (new Color(230, 255, 240, 255)).getRGB()), new Theme("Lavender Fog", (new Color(200, 160, 255, 255)).getRGB(), (new Color(240, 230, 255, 255)).getRGB()), new Theme("Emerald", (new Color(80, 200, 120, 255)).getRGB(), (new Color(10, 30, 20, 255)).getRGB())}));
   }

   public ColorRGBA getClientColor(int index) {
      return this.currentTheme == null ? new ColorRGBA(255, 255, 255, 255) : ColorUtil.gradient(3, index, this.currentTheme.getColor(), this.currentTheme.getSecondColor());
   }

   @Generated
   public Theme getCurrentTheme() {
      return this.currentTheme;
   }

   @Generated
   public List<Theme> getThemes() {
      return this.themes;
   }

   @Generated
   public Theme getDefaultTheme() {
      return this.defaultTheme;
   }

   @Generated
   public void setCurrentTheme(Theme currentTheme) {
      this.currentTheme = currentTheme;
   }

   @Generated
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ThemeManager)) {
         return false;
      } else {
         ThemeManager other = (ThemeManager)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            label47: {
               Object this$currentTheme = this.getCurrentTheme();
               Object other$currentTheme = other.getCurrentTheme();
               if (this$currentTheme == null) {
                  if (other$currentTheme == null) {
                     break label47;
                  }
               } else if (this$currentTheme.equals(other$currentTheme)) {
                  break label47;
               }

               return false;
            }

            Object this$themes = this.getThemes();
            Object other$themes = other.getThemes();
            if (this$themes == null) {
               if (other$themes != null) {
                  return false;
               }
            } else if (!this$themes.equals(other$themes)) {
               return false;
            }

            Object this$defaultTheme = this.getDefaultTheme();
            Object other$defaultTheme = other.getDefaultTheme();
            if (this$defaultTheme == null) {
               if (other$defaultTheme != null) {
                  return false;
               }
            } else if (!this$defaultTheme.equals(other$defaultTheme)) {
               return false;
            }

            return true;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ThemeManager;
   }

   @Generated
   public int hashCode() {
      int PRIME = 1;
      int result = 1;
      Object $currentTheme = this.getCurrentTheme();
      result = result * 59 + ($currentTheme == null ? 43 : $currentTheme.hashCode());
      Object $themes = this.getThemes();
      result = result * 59 + ($themes == null ? 43 : $themes.hashCode());
      Object $defaultTheme = this.getDefaultTheme();
      result = result * 59 + ($defaultTheme == null ? 43 : $defaultTheme.hashCode());
      return result;
   }

   @Generated
   public String toString() {
      String var10000 = String.valueOf(this.getCurrentTheme());
      return "ThemeManager(currentTheme=" + var10000 + ", themes=" + String.valueOf(this.getThemes()) + ", defaultTheme=" + String.valueOf(this.getDefaultTheme()) + ")";
   }
}
