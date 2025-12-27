package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MultipartUnbakedModel implements UnbakedModel {
   private final StateManager stateFactory;
   private final List components;

   public MultipartUnbakedModel(StateManager stateFactory, List components) {
      this.stateFactory = stateFactory;
      this.components = components;
   }

   public List getComponents() {
      return this.components;
   }

   public Set getModels() {
      Set<WeightedUnbakedModel> set = Sets.newHashSet();

      for(MultipartModelComponent multipartModelComponent : this.components) {
         set.add(multipartModelComponent.getModel());
      }

      return set;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof MultipartUnbakedModel)) {
         return false;
      } else {
         MultipartUnbakedModel multipartUnbakedModel = (MultipartUnbakedModel)o;
         return Objects.equals(this.stateFactory, multipartUnbakedModel.stateFactory) && Objects.equals(this.components, multipartUnbakedModel.components);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.stateFactory, this.components});
   }

   public Collection getModelDependencies() {
      return (Collection)this.getComponents().stream().flatMap((component) -> component.getModel().getModelDependencies().stream()).collect(Collectors.toSet());
   }

   public void setParents(Function modelLoader) {
      this.getComponents().forEach((component) -> component.getModel().setParents(modelLoader));
   }

   @Nullable
   public BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer) {
      MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();

      for(MultipartModelComponent multipartModelComponent : this.getComponents()) {
         BakedModel bakedModel = multipartModelComponent.getModel().bake(baker, textureGetter, rotationContainer);
         if (bakedModel != null) {
            builder.addComponent(multipartModelComponent.getPredicate(this.stateFactory), bakedModel);
         }
      }

      return builder.build();
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      private final ModelVariantMap.DeserializationContext context;

      public Deserializer(ModelVariantMap.DeserializationContext context) {
         this.context = context;
      }

      public MultipartUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         return new MultipartUnbakedModel(this.context.getStateFactory(), this.deserializeComponents(jsonDeserializationContext, jsonElement.getAsJsonArray()));
      }

      private List deserializeComponents(JsonDeserializationContext context, JsonArray array) {
         List<MultipartModelComponent> list = Lists.newArrayList();

         for(JsonElement jsonElement : array) {
            list.add((MultipartModelComponent)context.deserialize(jsonElement, MultipartModelComponent.class));
         }

         return list;
      }

      // $FF: synthetic method
      public Object deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
