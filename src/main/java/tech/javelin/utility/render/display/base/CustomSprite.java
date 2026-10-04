package tech.javelin.utility.render.display.base;

import lombok.Generated;
import net.minecraft.util.Identifier;
import tech.javelin.Javelin;

public class CustomSprite {
   private final Identifier texture;

   public CustomSprite(String path) {
      if (path.contains(":")) {
         this.texture = Identifier.of(path);
      } else if (path.contains("/")) {
         this.texture = Javelin.id(path);
      } else {
         this.texture = Javelin.id("icons/category/" + path);
      }

   }

   @Generated
   public Identifier getTexture() {
      return this.texture;
   }
}
