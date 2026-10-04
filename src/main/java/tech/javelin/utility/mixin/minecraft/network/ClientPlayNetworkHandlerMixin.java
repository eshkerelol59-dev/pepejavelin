package tech.javelin.utility.mixin.minecraft.network;

import com.darkmagician6.eventapi.EventManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.javelin.Javelin;
import tech.javelin.base.events.impl.player.EventPickupItem;

@Mixin({ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sendChatMessageHook(@NotNull String message, CallbackInfo ci) {
      if (message.startsWith(Javelin.getInstance().getCommandManager().getPrefix())) {
         try {
            Javelin.getInstance().getCommandManager().getDispatcher().execute(message.substring(Javelin.getInstance().getCommandManager().getPrefix().length()), Javelin.getInstance().getCommandManager().getSource());
         } catch (CommandSyntaxException var4) {
         }

         ci.cancel();
      }

   }

   @Inject(
      method = {"onEntitySpawn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
      if (packet.getEntityId() == 12345) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"onItemPickupAnimation"},
      at = {@At(
   value = "INVOKE_ASSIGN",
   target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;"
)}
   )
   private void onPickup(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
      ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler)(Object)this;
      if (handler.getWorld() != null) {
         Entity var6 = handler.getWorld().getEntityById(packet.getCollectorEntityId());
         if (var6 instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)var6;
            Entity var7 = handler.getWorld().getEntityById(packet.getEntityId());
               if (var7 instanceof ItemEntity) {
                  ItemEntity itemEntity = (ItemEntity)var7;
                  ItemStack stack = itemEntity.getStack();
                  EventManager.call(new EventPickupItem(living, stack));
               }
            }
         }
   }
}
