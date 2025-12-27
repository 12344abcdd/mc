package net.minecraft.client.render.model.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class ModelOverride {
   private final Identifier modelId;
   private final List conditions;

   public ModelOverride(Identifier modelId, List conditions) {
      this.modelId = modelId;
      this.conditions = ImmutableList.copyOf(conditions);
   }

   public Identifier getModelId() {
      return this.modelId;
   }

   public Stream streamConditions() {
      return this.conditions.stream();
   }

   @Environment(EnvType.CLIENT)
   public static class Condition {
      private final Identifier type;
      private final float threshold;

      public Condition(Identifier type, float threshold) {
         this.type = type;
         this.threshold = threshold;
      }

      public Identifier getType() {
         return this.type;
      }

      public float getThreshold() {
         return this.threshold;
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Deserializer implements JsonDeserializer {
      public ModelOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Identifier identifier = Identifier.of(JsonHelper.getString(jsonObject, "model"));
         List<Condition> list = this.deserializeMinPropertyValues(jsonObject);
         return new ModelOverride(identifier, list);
      }

      protected List deserializeMinPropertyValues(JsonObject object) {
         Map<Identifier, Float> map = Maps.newLinkedHashMap();
         JsonObject jsonObject = JsonHelper.getObject(object, "predicate");

         for(Map.Entry entry : jsonObject.entrySet()) {
            map.put(Identifier.of((String)entry.getKey()), JsonHelper.asFloat((JsonElement)entry.getValue(), (String)entry.getKey()));
         }

         return (List)map.entrySet().stream().map((entryx) -> new Condition((Identifier)entryx.getKey(), (Float)entryx.getValue())).collect(ImmutableList.toImmutableList());
      }

      // $FF: synthetic method
      public Object deserialize(final JsonElement functionJson, final Type unused, final JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }
   }
}
