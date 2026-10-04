package tech.javelin.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import org.joml.Vector4f;
import tech.javelin.Javelin;
import tech.javelin.base.events.impl.render.EventRender2D;
import tech.javelin.base.font.Fonts;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.impl.misc.NameProtect;
import tech.javelin.client.modules.impl.misc.ScoreboardHealth;
import tech.javelin.utility.game.other.ReplaceUtil;
import tech.javelin.utility.game.player.PlayerIntersectionUtil;
import tech.javelin.utility.math.ProjectionUtil;
import tech.javelin.utility.render.display.base.BorderRadius;
import tech.javelin.utility.render.display.base.color.ColorRGBA;
import tech.javelin.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(
   name = "NameTags",
   category = Category.RENDER,
   description = "Показывает информацию о игроке"
)
public final class EntityESP extends Module {
   public static final EntityESP INSTANCE = new EntityESP();
   private final HashMap<Entity, Vector4f> positions = new HashMap();

   @EventTarget
   private void onRender(EventRender2D e) {
      if (mc.world != null && mc.player != null) {
         float tickDelta = e.getTickDelta();
         this.renderPlayerTags(tickDelta, e);
         this.renderItemTags(tickDelta, e);
      }
   }

   private void renderPlayerTags(float tickDelta, EventRender2D e) {
      Iterator var3 = mc.world.getPlayers().iterator();

      while(true) {
         Vector4d position;
         float posY;
         ItemStack[] itemArray;
         int itemCount;
         do {
            PlayerEntity entity;
            Vec3d pos;
            do {
               do {
                  do {
                     do {
                        if (!var3.hasNext()) {
                           return;
                        }

                        entity = (PlayerEntity)var3.next();
                     } while(entity == mc.player && !mc.getEntityRenderDispatcher().camera.isThirdPerson());
                  } while(!ProjectionUtil.canSee(entity.getBoundingBox().getCenter()));

                  double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
                  double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.2D;
                  double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
                  pos = ProjectionUtil.worldSpaceToScreenSpace(new Vec3d(x, y, z));
               } while(pos.z <= 0.0D);
            } while(pos.z >= 1.0D);

            position = ProjectionUtil.getVector4D(entity);
            posY = (float)(position.y - 11.0D);
            float hp = ScoreboardHealth.INSTANCE.isEnabled() && entity != mc.player ? PlayerIntersectionUtil.getHealth(entity) : entity.getHealth();
            Text name = entity == mc.player && NameProtect.INSTANCE.isEnabled() ? Text.literal(NameProtect.getCustomName()) : ReplaceUtil.replaceSymbols(entity.getDisplayName());
            Text nameWithHp = ((Text)name).copy().append(Text.literal(" [").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(Text.literal(String.valueOf((int)hp)).setStyle(Style.EMPTY.withColor(Formatting.RED))).append(Text.literal("]").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            float textWidth = Fonts.REGULAR.getWidth(nameWithHp.getString(), 6.5F);
            DrawUtil.drawRoundedRect(e.getContext().getMatrices(), (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F) - 3.0D), posY - 2.5F, textWidth + 5.0F, 10.0F, BorderRadius.ZERO, Javelin.getInstance().getFriendManager().isFriend(entity.getNameForScoreboard()) ? new ColorRGBA(0, 166, 0, 123) : new ColorRGBA(0, 0, 0, 123));
            e.getContext().drawText(Fonts.REGULAR.getFont(6.5F), nameWithHp, (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F)), posY, 255.0F);
            itemArray = new ItemStack[6];
            itemCount = 0;
            EquipmentSlot[] var19 = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int var20 = var19.length;

            for(int var21 = 0; var21 < var20; ++var21) {
               EquipmentSlot slot = var19[var21];
               ItemStack stack = entity.getEquippedStack(slot);
               if (!stack.isEmpty()) {
                  itemArray[itemCount++] = stack;
               }
            }

            ItemStack mainHand = entity.getMainHandStack();
            if (!mainHand.isEmpty()) {
               itemArray[itemCount++] = mainHand;
            }

            ItemStack offHand = entity.getOffHandStack();
            if (!offHand.isEmpty()) {
               itemArray[itemCount++] = offHand;
            }
         } while(itemCount == 0);

         float iconSize = 16.0F;
         float spacing = 0.0F;
         float totalWidth = (float)itemCount * iconSize + (float)(itemCount - 1) * spacing;
         float startX = (float)(position.x + (position.z - position.x) / 2.0D - (double)(totalWidth / 2.0F) + 7.5D);
         float iconY = posY - 12.0F;
         MatrixStack matrices = e.getContext().getMatrices();

         for(int i = 0; i < itemCount; ++i) {
            ItemStack stack = itemArray[i];
            if (stack != null && !stack.isEmpty()) {
               float x2 = startX + (float)i * (iconSize + spacing);
               ItemEnchantmentsComponent enchComp = EnchantmentHelper.getEnchantments(stack);
               float enchantmentY;
               if (!enchComp.isEmpty()) {
                  Map<RegistryEntry<Enchantment>, Integer> enchMap = (Map)enchComp.getEnchantmentEntries().stream().collect(Collectors.toMap(Entry::getKey, it.unimi.dsi.fastutil.objects.Object2IntMap.Entry::getIntValue));
                  enchantmentY = iconY - 16.0F;
                  Iterator var33 = enchMap.entrySet().iterator();

                  label134:
                  while(true) {
                     Entry enchEntry;
                     int lvl;
                     do {
                        if (!var33.hasNext()) {
                           break label134;
                        }

                        enchEntry = (Entry)var33.next();
                        lvl = (Integer)enchEntry.getValue();
                     } while(lvl <= 0);

                     String fullName = Enchantment.getName((RegistryEntry)enchEntry.getKey(), lvl).getString();
                     String shortName = fullName.length() > 2 ? fullName.substring(0, 2) : fullName;
                     String enchantmentText = shortName + lvl;
                     float enchantmentTextWidth = Fonts.REGULAR.getWidth(enchantmentText, 6.0F);
                     int color = -1;
                     if (shortName.equalsIgnoreCase("Sh") && lvl > 5 || shortName.equalsIgnoreCase("Pr") && lvl > 4) {
                        color = (new ColorRGBA(212, 45, 43, 255)).getRGB();
                     }

                     e.getContext().drawText(Fonts.REGULAR.getFont(6.0F), enchantmentText, x2 - enchantmentTextWidth / 2.0F, enchantmentY, new ColorRGBA(color));
                     enchantmentY -= 8.0F;
                  }
               }

               DrawUtil.drawRoundedRect(matrices, x2 - 7.0F, iconY - 7.0F, 14.0F, 14.0F, BorderRadius.all(3.0F), new ColorRGBA(0, 0, 0, 123));
               float scale = 0.7F;
               enchantmentY = -18.0F;
               matrices.push();
               matrices.translate(x2 + enchantmentY, iconY + enchantmentY, 0.0F);
               matrices.scale(scale, scale, 1.0F);
               int drawX = (int)(-enchantmentY);
               int drawY = (int)(-enchantmentY);
               e.getContext().drawItem(stack, drawX, drawY);
               e.getContext().drawStackOverlay(mc.textRenderer, stack, drawX, drawY);
               matrices.pop();
            }
         }
      }
   }

   private void renderItemTags(float tickDelta, EventRender2D e) {
      Iterator var3 = mc.world.getEntities().iterator();

      while(var3.hasNext()) {
         Entity entity = (Entity)var3.next();
         if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (ProjectionUtil.canSee(itemEntity.getBoundingBox().getCenter())) {
               double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
               double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.1D;
               double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
               Vec3d pos = ProjectionUtil.worldSpaceToScreenSpace(new Vec3d(x, y, z));
               if (!(pos.z <= 0.0D) && !(pos.z >= 1.0D)) {
                  Vector4d position = ProjectionUtil.getVector4D(entity);
                  float posY = (float)(position.y - 11.0D);
                  ItemStack stack = itemEntity.getStack();
                  if (!stack.isEmpty()) {
                     int rarityOrdinal = stack.getRarity().ordinal();
                     Formatting var10000;
                     switch(rarityOrdinal) {
                     case 1:
                        var10000 = Formatting.YELLOW;
                        break;
                     case 2:
                        var10000 = Formatting.AQUA;
                        break;
                     case 3:
                        var10000 = Formatting.LIGHT_PURPLE;
                        break;
                     default:
                        var10000 = Formatting.WHITE;
                     }

                     Formatting rarityColor = var10000;
                     String itemName = stack.getName().getString();
                     Text nameText = Text.literal(itemName).setStyle(Style.EMPTY.withColor(rarityColor));
                     if (!stack.getName().getSiblings().isEmpty()) {
                        nameText = stack.getName();
                     }

                     Text countComponent = stack.getCount() > 1 ? Text.literal(" х" + stack.getCount()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)) : Text.empty();
                     Text textComponent = ((Text)nameText).copy().append(countComponent);
                     float textWidth = Fonts.REGULAR.getFont(6.5F).width((Text)textComponent);
                     DrawUtil.drawRoundedRect(e.getContext().getMatrices(), (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F) - 3.0D), (float)(position.y - 13.5D), textWidth + 4.0F, 10.0F, BorderRadius.ZERO, new ColorRGBA(0, 0, 0, 123));
                     e.getContext().drawText(Fonts.REGULAR.getFont(6.5F), textComponent, (float)(position.x + (position.z - position.x) / 2.0D - (double)(textWidth / 2.0F)), (float)position.y - 11.0F, 255.0F);
                  }
               }
            }
         }
      }

   }

   public static void drawBox(double x, double y, double width, double height, double size, int color, BufferBuilder bufferbuilder) {
      drawRectBuilding(x + size, y, width - size, y + size, color, bufferbuilder);
      drawRectBuilding(x, y, x + size, height, color, bufferbuilder);
      drawRectBuilding(width - size, y, width, height, color, bufferbuilder);
      drawRectBuilding(x + size, height - size, width - size, height, color, bufferbuilder);
   }

   public static void drawBoxTest(double x, double y, double width, double height, double size, Vector4f colors, BufferBuilder bufferbuilder) {
      drawMCHorizontalBuilding(x + size, y, width - size, y + size, (int)colors.x(), (int)colors.y(), bufferbuilder);
      drawMCVerticalBuilding(width - size, y + size, width, height - size, (int)colors.y(), (int)colors.z(), bufferbuilder);
      drawMCHorizontalBuilding(x + size, height - size, width - size, height, (int)colors.w(), (int)colors.z(), bufferbuilder);
      drawMCVerticalBuilding(x, y + size, x + size, height - size, (int)colors.x(), (int)colors.w(), bufferbuilder);
   }

   public static void drawRectBuilding(double left, double top, double right, double bottom, int color, BufferBuilder bufferbuilder) {
      double j;
      if (left < right) {
         j = left;
         left = right;
         right = j;
      }

      if (top < bottom) {
         j = top;
         top = bottom;
         bottom = j;
      }

      float f3 = (float)(color >> 24 & 255) / 255.0F;
      float f = (float)(color >> 16 & 255) / 255.0F;
      float f1 = (float)(color >> 8 & 255) / 255.0F;
      float f2 = (float)(color & 255) / 255.0F;
      bufferbuilder.vertex((float)left, (float)bottom, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)right, (float)bottom, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)right, (float)top, 0.0F).color(f, f1, f2, f3);
      bufferbuilder.vertex((float)left, (float)top, 0.0F).color(f, f1, f2, f3);
   }

   public static void drawMCHorizontalBuilding(double x1, double y1, double x2, double y2, int start, int end, BufferBuilder bufferbuilder) {
      float a1 = (float)(start >> 24 & 255) / 255.0F;
      float r1 = (float)(start >> 16 & 255) / 255.0F;
      float g1 = (float)(start >> 8 & 255) / 255.0F;
      float b1 = (float)(start & 255) / 255.0F;
      float a2 = (float)(end >> 24 & 255) / 255.0F;
      float r2 = (float)(end >> 16 & 255) / 255.0F;
      float g2 = (float)(end >> 8 & 255) / 255.0F;
      float b2 = (float)(end & 255) / 255.0F;
      bufferbuilder.vertex((float)x1, (float)y2, 0.0F).color(r1, g1, b1, a1);
      bufferbuilder.vertex((float)x2, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y1, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x1, (float)y1, 0.0F).color(r1, g1, b1, a1);
   }

   public static void drawMCVerticalBuilding(double x1, double y1, double x2, double y2, int start, int end, BufferBuilder bufferbuilder) {
      float a1 = (float)(start >> 24 & 255) / 255.0F;
      float r1 = (float)(start >> 16 & 255) / 255.0F;
      float g1 = (float)(start >> 8 & 255) / 255.0F;
      float b1 = (float)(start & 255) / 255.0F;
      float a2 = (float)(end >> 24 & 255) / 255.0F;
      float r2 = (float)(end >> 16 & 255) / 255.0F;
      float g2 = (float)(end >> 8 & 255) / 255.0F;
      float b2 = (float)(end & 255) / 255.0F;
      bufferbuilder.vertex((float)x1, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y2, 0.0F).color(r2, g2, b2, a2);
      bufferbuilder.vertex((float)x2, (float)y1, 0.0F).color(r1, g1, b1, a1);
      bufferbuilder.vertex((float)x1, (float)y1, 0.0F).color(r1, g1, b1, a1);
   }
}
