package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class MultipartModelComponent {
   private final MultipartModelSelector selector;
   private final WeightedUnbakedModel model;

   public MultipartModelComponent(MultipartModelSelector selector, WeightedUnbakedModel model) {
      if (selector == null) {
         throw new IllegalArgumentException("Missing condition for selector");
      } else if (model == null) {
         throw new IllegalArgumentException("Missing variant for selector");
      } else {
         this.selector = selector;
         this.model = model;
      }
   }

   public WeightedUnbakedModel getModel() {
      return this.model;
   }

   public Predicate getPredicate(StateManager stateFactory) {
      return this.selector.getPredicate(stateFactory);
   }

   public boolean equals(Object o) {
      return this == o;
   }

   public int hashCode() {
      return System.identityHashCode(this);
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public MultipartModelComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         return new MultipartModelComponent(this.deserializeSelectorOrDefault(jsonObject), (WeightedUnbakedModel)jsonDeserializationContext.deserialize(jsonObject.get("apply"), WeightedUnbakedModel.class));
      }

      private MultipartModelSelector deserializeSelectorOrDefault(JsonObject object) {
         return object.has("when") ? deserializeSelector(JsonHelper.getObject(object, "when")) : MultipartModelSelector.TRUE;
      }

      @VisibleForTesting
      static MultipartModelSelector deserializeSelector(JsonObject object) {
         Set<Map.Entry<String, JsonElement>> set = object.entrySet();
         if (set.isEmpty()) {
            throw new JsonParseException("No elements found in selector");
         } else if (set.size() == 1) {
            if (object.has("OR")) {
               List<MultipartModelSelector> list = (List)Streams.stream(JsonHelper.getArray(object, "OR")).map((json) -> deserializeSelector(json.getAsJsonObject())).collect(Collectors.toList());
               return new OrMultipartModelSelector(list);
            } else if (object.has("AND")) {
               List<MultipartModelSelector> list = (List)Streams.stream(JsonHelper.getArray(object, "AND")).map((json) -> deserializeSelector(json.getAsJsonObject())).collect(Collectors.toList());
               return new AndMultipartModelSelector(list);
            } else {
               return createStatePropertySelector((Map.Entry)set.iterator().next());
            }
         } else {
            return new AndMultipartModelSelector((Iterable)set.stream().map(Deserializer::createStatePropertySelector).collect(Collectors.toList()));
         }
      }

      private static MultipartModelSelector createStatePropertySelector(Map.Entry entry) {
         return new SimpleMultipartModelSelector((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
      }

      // $FF: synthetic method
      public Object deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
