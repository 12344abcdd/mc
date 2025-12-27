package net.minecraft.client.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.tag.TagPacketSerializer;

@Environment(EnvType.CLIENT)
public class ClientTagLoader {
   private final Map tagsByRegistry = new HashMap();

   public void put(RegistryKey registryRef, TagPacketSerializer.Serialized serialized) {
      this.tagsByRegistry.put(registryRef, serialized);
   }

   private static void onStaticTagsLoaded() {
      AbstractFurnaceBlockEntity.clearFuelTimes();
      Blocks.refreshShapeCache();
   }

   private void load(DynamicRegistryManager registryManager, Predicate predicate) {
      this.tagsByRegistry.forEach((registryRef, serialized) -> {
         if (predicate.test(registryRef)) {
            serialized.loadTo(registryManager.get(registryRef));
         }

      });
   }

   public void load(DynamicRegistryManager registryManager, boolean local) {
      if (local) {
         Set var10002 = SerializableRegistries.SYNCED_REGISTRIES;
         Objects.requireNonNull(var10002);
         this.load(registryManager, var10002::contains);
      } else {
         registryManager.streamAllRegistries().filter((registries) -> !SerializableRegistries.SYNCED_REGISTRIES.contains(registries.key())).forEach((entry) -> entry.value().clearTags());
         this.load(registryManager, (registryRef) -> true);
         onStaticTagsLoaded();
      }

   }
}
