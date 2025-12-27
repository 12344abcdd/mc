package net.minecraft.client.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.ResourceFactory;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientRegistries {
   @Nullable
   private DynamicRegistries dynamicRegistries;
   @Nullable
   private ClientTagLoader tagLoader;

   public void putDynamicRegistry(RegistryKey registryRef, List entries) {
      if (this.dynamicRegistries == null) {
         this.dynamicRegistries = new DynamicRegistries();
      }

      this.dynamicRegistries.put(registryRef, entries);
   }

   public void putTags(Map tags) {
      if (this.tagLoader == null) {
         this.tagLoader = new ClientTagLoader();
      }

      ClientTagLoader var10001 = this.tagLoader;
      Objects.requireNonNull(var10001);
      tags.forEach(var10001::put);
   }

   public DynamicRegistryManager.Immutable createRegistryManager(ResourceFactory factory, DynamicRegistryManager registryManager, boolean local) {
      CombinedDynamicRegistries<ClientDynamicRegistryType> combinedDynamicRegistries = ClientDynamicRegistryType.createCombinedDynamicRegistries();
      DynamicRegistryManager dynamicRegistryManager;
      if (this.dynamicRegistries != null) {
         DynamicRegistryManager.Immutable immutable = combinedDynamicRegistries.getPrecedingRegistryManagers(ClientDynamicRegistryType.REMOTE);
         DynamicRegistryManager.Immutable immutable2 = this.dynamicRegistries.load(factory, immutable).toImmutable();
         dynamicRegistryManager = combinedDynamicRegistries.with(ClientDynamicRegistryType.REMOTE, new DynamicRegistryManager.Immutable[]{immutable2}).getCombinedRegistryManager();
      } else {
         dynamicRegistryManager = registryManager;
      }

      if (this.tagLoader != null) {
         this.tagLoader.load(dynamicRegistryManager, local);
      }

      return dynamicRegistryManager.toImmutable();
   }

   @Environment(EnvType.CLIENT)
   static class DynamicRegistries {
      private final Map dynamicRegistries = new HashMap();

      public void put(RegistryKey registryRef, List entries) {
         ((List)this.dynamicRegistries.computeIfAbsent(registryRef, (registries) -> new ArrayList())).addAll(entries);
      }

      public DynamicRegistryManager load(ResourceFactory factory, DynamicRegistryManager registryManager) {
         return RegistryLoader.loadFromNetwork(this.dynamicRegistries, factory, registryManager, RegistryLoader.SYNCED_REGISTRIES);
      }
   }
}
