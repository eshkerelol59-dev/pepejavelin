package tech.javelin.client.modules.api;

import lombok.Generated;

public enum Category {
   COMBAT("Combat", "0"),
   MOVEMENT("Movement", "1"),
   PLAYER("Player", "2"),
   RENDER("Render", "3"),
   MISC("Misc", "4"),
   THEMES("Themes", "G");

   private final String name;
   private final String icon;

   private Category(String name, String icon) {
      this.name = name;
      this.icon = icon;
   }

   @Generated
   public String getIcon() {
      return this.icon;
   }

   @Generated
   public String getName() {
      return this.name;
   }


   private static Category[] $values() {
      return new Category[]{COMBAT, MOVEMENT, PLAYER, RENDER, MISC, THEMES};
   }
}
