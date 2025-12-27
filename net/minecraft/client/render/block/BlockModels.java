package net.minecraft.client.render.block;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BlockModels {
   private Map models = Map.of();
   private final BakedModelManager modelManager;

   public BlockModels(BakedModelManager modelManager) {
      this.modelManager = modelManager;
   }

   public Sprite getModelParticleSprite(BlockState state) {
      return this.getModel(state).getParticleSprite();
   }

   public BakedModel getModel(BlockState state) {
      BakedModel bakedModel = (BakedModel)this.models.get(state);
      if (bakedModel == null) {
         bakedModel = this.modelManager.getMissingModel();
      }

      return bakedModel;
   }

   public BakedModelManager getModelManager() {
      return this.modelManager;
   }

   public void setModels(Map models) {
      this.models = models;
   }

   public static ModelIdentifier getModelId(BlockState state) {
      return getModelId(Registries.BLOCK.getId(state.getBlock()), state);
   }

   public static ModelIdentifier getModelId(Identifier id, BlockState state) {
      return new ModelIdentifier(id, propertyMapToString(state.getEntries()));
   }

   public static String propertyMapToString(Map map) {
      StringBuilder stringBuilder = new StringBuilder();

      for(Map.Entry entry : map.entrySet()) {
         if (stringBuilder.length() != 0) {
            stringBuilder.append(',');
         }

         Property<?> property = (Property)entry.getKey();
         stringBuilder.append(property.getName());
         stringBuilder.append('=');
         stringBuilder.append(propertyValueToString(property, (Comparable)entry.getValue()));
      }

      return stringBuilder.toString();
   }

   private static String propertyValueToString(Property property, Comparable value) {
      return property.name(value);
   }
}
