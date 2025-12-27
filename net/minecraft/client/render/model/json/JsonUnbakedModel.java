package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class JsonUnbakedModel implements UnbakedModel {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(JsonUnbakedModel.class, new Deserializer()).registerTypeAdapter(ModelElement.class, new ModelElement.Deserializer()).registerTypeAdapter(ModelElementFace.class, new ModelElementFace.Deserializer()).registerTypeAdapter(ModelElementTexture.class, new ModelElementTexture.Deserializer()).registerTypeAdapter(Transformation.class, new Transformation.Deserializer()).registerTypeAdapter(ModelTransformation.class, new ModelTransformation.Deserializer()).registerTypeAdapter(ModelOverride.class, new ModelOverride.Deserializer()).create();
   private static final char TEXTURE_REFERENCE_INITIAL = '#';
   public static final String PARTICLE_KEY = "particle";
   private static final boolean field_42912 = true;
   private final List elements;
   @Nullable
   private final GuiLight guiLight;
   @Nullable
   private final Boolean ambientOcclusion;
   private final ModelTransformation transformations;
   private final List overrides;
   public String id = "";
   @VisibleForTesting
   protected final Map textureMap;
   @Nullable
   protected JsonUnbakedModel parent;
   @Nullable
   protected Identifier parentId;

   public static JsonUnbakedModel deserialize(Reader input) {
      return (JsonUnbakedModel)JsonHelper.deserialize(GSON, input, JsonUnbakedModel.class);
   }

   public static JsonUnbakedModel deserialize(String json) {
      return deserialize((Reader)(new StringReader(json)));
   }

   public JsonUnbakedModel(@Nullable Identifier parentId, List elements, Map textureMap, @Nullable Boolean ambientOcclusion, @Nullable GuiLight guiLight, ModelTransformation transformations, List overrides) {
      this.elements = elements;
      this.ambientOcclusion = ambientOcclusion;
      this.guiLight = guiLight;
      this.textureMap = textureMap;
      this.parentId = parentId;
      this.transformations = transformations;
      this.overrides = overrides;
   }

   public List getElements() {
      return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
   }

   public boolean useAmbientOcclusion() {
      if (this.ambientOcclusion != null) {
         return this.ambientOcclusion;
      } else {
         return this.parent != null ? this.parent.useAmbientOcclusion() : true;
      }
   }

   public GuiLight getGuiLight() {
      if (this.guiLight != null) {
         return this.guiLight;
      } else {
         return this.parent != null ? this.parent.getGuiLight() : JsonUnbakedModel.GuiLight.BLOCK;
      }
   }

   public boolean needsResolution() {
      return this.parentId == null || this.parent != null && this.parent.needsResolution();
   }

   public List getOverrides() {
      return this.overrides;
   }

   private ModelOverrideList compileOverrides(Baker baker, JsonUnbakedModel parent) {
      return this.overrides.isEmpty() ? ModelOverrideList.EMPTY : new ModelOverrideList(baker, parent, this.overrides);
   }

   public Collection getModelDependencies() {
      Set<Identifier> set = Sets.newHashSet();

      for(ModelOverride modelOverride : this.overrides) {
         set.add(modelOverride.getModelId());
      }

      if (this.parentId != null) {
         set.add(this.parentId);
      }

      return set;
   }

   public void setParents(Function modelLoader) {
      Set<UnbakedModel> set = Sets.newLinkedHashSet();

      for(JsonUnbakedModel jsonUnbakedModel = this; jsonUnbakedModel.parentId != null && jsonUnbakedModel.parent == null; jsonUnbakedModel = jsonUnbakedModel.parent) {
         set.add(jsonUnbakedModel);
         UnbakedModel unbakedModel = (UnbakedModel)modelLoader.apply(jsonUnbakedModel.parentId);
         if (unbakedModel == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.parentId, jsonUnbakedModel);
         }

         if (set.contains(unbakedModel)) {
            LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", new Object[]{jsonUnbakedModel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentId});
            unbakedModel = null;
         }

         if (unbakedModel == null) {
            jsonUnbakedModel.parentId = ModelLoader.MISSING_ID;
            unbakedModel = (UnbakedModel)modelLoader.apply(jsonUnbakedModel.parentId);
         }

         if (!(unbakedModel instanceof JsonUnbakedModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
         }

         jsonUnbakedModel.parent = (JsonUnbakedModel)unbakedModel;
      }

      this.overrides.forEach((override) -> {
         UnbakedModel unbakedModel = (UnbakedModel)modelLoader.apply(override.getModelId());
         if (!Objects.equals(unbakedModel, this)) {
            unbakedModel.setParents(modelLoader);
         }
      });
   }

   public BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer) {
      return this.bake(baker, this, textureGetter, rotationContainer, true);
   }

   public BakedModel bake(Baker baker, JsonUnbakedModel parent, Function textureGetter, ModelBakeSettings settings, boolean bl) {
      Sprite sprite = (Sprite)textureGetter.apply(this.resolveSprite("particle"));
      if (this.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
         return new BuiltinBakedModel(this.getTransformations(), this.compileOverrides(baker, parent), sprite, this.getGuiLight().isSide());
      } else {
         BasicBakedModel.Builder builder = (new BasicBakedModel.Builder(this, this.compileOverrides(baker, parent), bl)).setParticle(sprite);

         for(ModelElement modelElement : this.getElements()) {
            for(Direction direction : modelElement.faces.keySet()) {
               ModelElementFace modelElementFace = (ModelElementFace)modelElement.faces.get(direction);
               Sprite sprite2 = (Sprite)textureGetter.apply(this.resolveSprite(modelElementFace.textureId()));
               if (modelElementFace.cullFace() == null) {
                  builder.addQuad(createQuad(modelElement, modelElementFace, sprite2, direction, settings));
               } else {
                  builder.addQuad(Direction.transform(settings.getRotation().getMatrix(), modelElementFace.cullFace()), createQuad(modelElement, modelElementFace, sprite2, direction, settings));
               }
            }
         }

         return builder.build();
      }
   }

   private static BakedQuad createQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings) {
      return QUAD_FACTORY.bake(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade);
   }

   public boolean textureExists(String name) {
      return !MissingSprite.getMissingSpriteId().equals(this.resolveSprite(name).getTextureId());
   }

   public SpriteIdentifier resolveSprite(String spriteName) {
      if (isTextureReference(spriteName)) {
         spriteName = spriteName.substring(1);
      }

      List<String> list = Lists.newArrayList();

      while(true) {
         Either<SpriteIdentifier, String> either = this.resolveTexture(spriteName);
         Optional<SpriteIdentifier> optional = either.left();
         if (optional.isPresent()) {
            return (SpriteIdentifier)optional.get();
         }

         spriteName = (String)either.right().get();
         if (list.contains(spriteName)) {
            LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", new Object[]{Joiner.on("->").join(list), spriteName, this.id});
            return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId());
         }

         list.add(spriteName);
      }
   }

   private Either resolveTexture(String name) {
      for(JsonUnbakedModel jsonUnbakedModel = this; jsonUnbakedModel != null; jsonUnbakedModel = jsonUnbakedModel.parent) {
         Either<SpriteIdentifier, String> either = (Either)jsonUnbakedModel.textureMap.get(name);
         if (either != null) {
            return either;
         }
      }

      return Either.left(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId()));
   }

   static boolean isTextureReference(String reference) {
      return reference.charAt(0) == '#';
   }

   public JsonUnbakedModel getRootModel() {
      return this.parent == null ? this : this.parent.getRootModel();
   }

   public ModelTransformation getTransformations() {
      Transformation transformation = this.getTransformation(ModelTransformationMode.THIRD_PERSON_LEFT_HAND);
      Transformation transformation2 = this.getTransformation(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
      Transformation transformation3 = this.getTransformation(ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
      Transformation transformation4 = this.getTransformation(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND);
      Transformation transformation5 = this.getTransformation(ModelTransformationMode.HEAD);
      Transformation transformation6 = this.getTransformation(ModelTransformationMode.GUI);
      Transformation transformation7 = this.getTransformation(ModelTransformationMode.GROUND);
      Transformation transformation8 = this.getTransformation(ModelTransformationMode.FIXED);
      return new ModelTransformation(transformation, transformation2, transformation3, transformation4, transformation5, transformation6, transformation7, transformation8);
   }

   private Transformation getTransformation(ModelTransformationMode renderMode) {
      return this.parent != null && !this.transformations.isTransformationDefined(renderMode) ? this.parent.getTransformation(renderMode) : this.transformations.getTransformation(renderMode);
   }

   public String toString() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public JsonUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         List<ModelElement> list = this.elementsFromJson(jsonDeserializationContext, jsonObject);
         String string = this.parentFromJson(jsonObject);
         Map<String, Either<SpriteIdentifier, String>> map = this.texturesFromJson(jsonObject);
         Boolean boolean_ = this.ambientOcclusionFromJson(jsonObject);
         ModelTransformation modelTransformation = ModelTransformation.NONE;
         if (jsonObject.has("display")) {
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "display");
            modelTransformation = (ModelTransformation)jsonDeserializationContext.deserialize(jsonObject2, ModelTransformation.class);
         }

         List<ModelOverride> list2 = this.overridesFromJson(jsonDeserializationContext, jsonObject);
         GuiLight guiLight = null;
         if (jsonObject.has("gui_light")) {
            guiLight = JsonUnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
         }

         Identifier identifier = string.isEmpty() ? null : Identifier.of(string);
         return new JsonUnbakedModel(identifier, list, map, boolean_, guiLight, modelTransformation, list2);
      }

      protected List overridesFromJson(JsonDeserializationContext context, JsonObject object) {
         List<ModelOverride> list = Lists.newArrayList();
         if (object.has("overrides")) {
            for(JsonElement jsonElement : JsonHelper.getArray(object, "overrides")) {
               list.add((ModelOverride)context.deserialize(jsonElement, ModelOverride.class));
            }
         }

         return list;
      }

      private Map texturesFromJson(JsonObject object) {
         Identifier identifier = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
         Map<String, Either<SpriteIdentifier, String>> map = Maps.newHashMap();
         if (object.has("textures")) {
            JsonObject jsonObject = JsonHelper.getObject(object, "textures");

            for(Map.Entry entry : jsonObject.entrySet()) {
               map.put((String)entry.getKey(), resolveReference(identifier, ((JsonElement)entry.getValue()).getAsString()));
            }
         }

         return map;
      }

      private static Either resolveReference(Identifier id, String name) {
         if (JsonUnbakedModel.isTextureReference(name)) {
            return Either.right(name.substring(1));
         } else {
            Identifier identifier = Identifier.tryParse(name);
            if (identifier == null) {
               throw new JsonParseException(name + " is not valid resource location");
            } else {
               return Either.left(new SpriteIdentifier(id, identifier));
            }
         }
      }

      private String parentFromJson(JsonObject json) {
         return JsonHelper.getString(json, "parent", "");
      }

      @Nullable
      protected Boolean ambientOcclusionFromJson(JsonObject json) {
         return json.has("ambientocclusion") ? JsonHelper.getBoolean(json, "ambientocclusion") : null;
      }

      protected List elementsFromJson(JsonDeserializationContext context, JsonObject json) {
         List<ModelElement> list = Lists.newArrayList();
         if (json.has("elements")) {
            for(JsonElement jsonElement : JsonHelper.getArray(json, "elements")) {
               list.add((ModelElement)context.deserialize(jsonElement, ModelElement.class));
            }
         }

         return list;
      }

      // $FF: synthetic method
      public Object deserialize(final JsonElement element, final Type unused, final JsonDeserializationContext ctx) throws JsonParseException {
         return this.deserialize(element, unused, ctx);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum GuiLight {
      ITEM("front"),
      BLOCK("side");

      private final String name;

      private GuiLight(final String name) {
         this.name = name;
      }

      public static GuiLight byName(String value) {
         for(GuiLight guiLight : values()) {
            if (guiLight.name.equals(value)) {
               return guiLight;
            }
         }

         throw new IllegalArgumentException("Invalid gui light: " + value);
      }

      public boolean isSide() {
         return this == BLOCK;
      }

      // $FF: synthetic method
      private static GuiLight[] method_36920() {
         return new GuiLight[]{ITEM, BLOCK};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class UncheckedModelException extends RuntimeException {
      public UncheckedModelException(String message) {
         super(message);
      }
   }
}
