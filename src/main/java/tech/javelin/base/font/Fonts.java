package tech.javelin.base.font;

import lombok.Generated;

public final class Fonts {
   public static final MsdfFont BOLD = MsdfFont.builder().atlas("bold").data("bold").build();
   public static final MsdfFont MEDIUM = MsdfFont.builder().atlas("medium").data("medium").build();
   public static final MsdfFont REGULAR = MsdfFont.builder().atlas("regular").data("regular").build();
   public static final MsdfFont SEMIBOLD = MsdfFont.builder().atlas("semibold").data("semibold").build();
   public static final MsdfFont ROUND_BOLD = MsdfFont.builder().atlas("roundbold").data("roundbold").build();
   public static final MsdfFont ICONS = MsdfFont.builder().atlas("icons").data("icons").build();
   public static final MsdfFont ICONS2 = MsdfFont.builder().atlas("hud_icons").data("hud_icons").build();
   public static final MsdfFont COMFORTA_LIGHT = MsdfFont.builder().atlas("comfortaa_light").data("comfortaa_light").build();
   public static final MsdfFont COMFORTA_REGULAR = MsdfFont.builder().atlas("comfortaa_regular").data("comfortaa_regular").build();
   public static final MsdfFont LUPA = MsdfFont.builder().atlas("lupa").data("lupa").build();

   @Generated
   private Fonts() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
