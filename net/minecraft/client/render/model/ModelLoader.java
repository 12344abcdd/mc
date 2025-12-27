package net.minecraft.client.render.model;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelLoader {
   public static final SpriteIdentifier FIRE_0;
   public static final SpriteIdentifier FIRE_1;
   public static final SpriteIdentifier LAVA_FLOW;
   public static final SpriteIdentifier WATER_FLOW;
   public static final SpriteIdentifier WATER_OVERLAY;
   public static final SpriteIdentifier BANNER_BASE;
   public static final SpriteIdentifier SHIELD_BASE;
   public static final SpriteIdentifier SHIELD_BASE_NO_PATTERN;
   public static final int field_32983 = 10;
   public static final List BLOCK_DESTRUCTION_STAGES;
   public static final List BLOCK_DESTRUCTION_STAGE_TEXTURES;
   public static final List BLOCK_DESTRUCTION_RENDER_LAYERS;
   private static final Logger LOGGER;
   private static final String BUILTIN = "builtin/";
   private static final String BUILTIN_GENERATED = "builtin/generated";
   private static final String BUILTIN_ENTITY = "builtin/entity";
   private static final String MISSING = "missing";
   public static final Identifier MISSING_ID;
   public static final ModelIdentifier MISSING_MODEL_ID;
   public static final ResourceFinder MODELS_FINDER;
   @VisibleForTesting
   public static final String MISSING_DEFINITION;
   private static final Map BUILTIN_MODEL_DEFINITIONS;
   public static final JsonUnbakedModel GENERATION_MARKER;
   public static final JsonUnbakedModel BLOCK_ENTITY_MARKER;
   static final ItemModelGenerator ITEM_MODEL_GENERATOR;
   private final Map jsonUnbakedModels;
   private final Set modelsToLoad = new HashSet();
   private final Map unbakedModels = new HashMap();
   final Map bakedModelCache = new HashMap();
   private final Map modelsToBake = new HashMap();
   private final Map bakedModels = new HashMap();
   private final UnbakedModel missingModel;
   private final Object2IntMap stateLookup;

   public ModelLoader(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates) {
      this.jsonUnbakedModels = jsonUnbakedModels;
      profiler.push("missing_model");

      try {
         this.missingModel = this.loadModelFromJson(MISSING_ID);
         this.addModelToBake(MISSING_MODEL_ID, this.missingModel);
      } catch (IOException iOException) {
         LOGGER.error("Error loading missing model, should never happen :(", iOException);
         throw new RuntimeException(iOException);
      }

      BlockStatesLoader blockStatesLoader = new BlockStatesLoader(blockStates, profiler, this.missingModel, blockColors, this::add);
      blockStatesLoader.load();
      this.stateLookup = blockStatesLoader.getStateLookup();
      profiler.swap("items");

      for(Identifier identifier : Registries.ITEM.getIds()) {
         this.loadInventoryVariantItemModel(identifier);
      }

      profiler.swap("special");
      this.loadItemModel(ItemRenderer.TRIDENT_IN_HAND);
      this.loadItemModel(ItemRenderer.SPYGLASS_IN_HAND);
      this.modelsToBake.values().forEach((model) -> model.setParents(this::getOrLoadModel));
      profiler.pop();
   }

   public void bake(SpriteGetter spliteGetter) {
      this.modelsToBake.forEach((id, model) -> {
         BakedModel bakedModel = null;

         try {
            bakedModel = (new BakerImpl(spliteGetter, id)).bake((UnbakedModel)model, ModelRotation.X0_Y0);
         } catch (Exception exception) {
            LOGGER.warn("Unable to bake model: '{}': {}", id, exception);
         }

         if (bakedModel != null) {
            this.bakedModels.put(id, bakedModel);
         }

      });
   }

   UnbakedModel getOrLoadModel(Identifier id) {
      if (this.unbakedModels.containsKey(id)) {
         return (UnbakedModel)this.unbakedModels.get(id);
      } else if (this.modelsToLoad.contains(id)) {
         throw new IllegalStateException("Circular reference while loading " + String.valueOf(id));
      } else {
         this.modelsToLoad.add(id);

         while(!this.modelsToLoad.isEmpty()) {
            Identifier identifier = (Identifier)this.modelsToLoad.iterator().next();

            try {
               if (!this.unbakedModels.containsKey(identifier)) {
                  UnbakedModel unbakedModel = this.loadModelFromJson(identifier);
                  this.unbakedModels.put(identifier, unbakedModel);
                  this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
               }
            } catch (Exception exception) {
               LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", new Object[]{identifier, id, exception});
               this.unbakedModels.put(identifier, this.missingModel);
            } finally {
               this.modelsToLoad.remove(identifier);
            }
         }

         return (UnbakedModel)this.unbakedModels.getOrDefault(id, this.missingModel);
      }
   }

   private void loadInventoryVariantItemModel(Identifier id) {
      ModelIdentifier modelIdentifier = ModelIdentifier.ofInventoryVariant(id);
      Identifier identifier = id.withPrefixedPath("item/");
      UnbakedModel unbakedModel = this.getOrLoadModel(identifier);
      this.add(modelIdentifier, unbakedModel);
   }

   private void loadItemModel(ModelIdentifier id) {
      Identifier identifier = id.id().withPrefixedPath("item/");
      UnbakedModel unbakedModel = this.getOrLoadModel(identifier);
      this.add(id, unbakedModel);
   }

   private void add(ModelIdentifier id, UnbakedModel model) {
      for(Identifier identifier : model.getModelDependencies()) {
         this.getOrLoadModel(identifier);
      }

      this.addModelToBake(id, model);
   }

   private void addModelToBake(ModelIdentifier id, UnbakedModel model) {
      this.modelsToBake.put(id, model);
   }

   private JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException {
      String string = id.getPath();
      if ("builtin/generated".equals(string)) {
         return GENERATION_MARKER;
      } else if ("builtin/entity".equals(string)) {
         return BLOCK_ENTITY_MARKER;
      } else if (string.startsWith("builtin/")) {
         String string2 = string.substring("builtin/".length());
         String string3 = (String)BUILTIN_MODEL_DEFINITIONS.get(string2);
         if (string3 == null) {
            throw new FileNotFoundException(id.toString());
         } else {
            Reader reader = new StringReader(string3);
            JsonUnbakedModel jsonUnbakedModel = JsonUnbakedModel.deserialize(reader);
            jsonUnbakedModel.id = id.toString();
            return jsonUnbakedModel;
         }
      } else {
         Identifier identifier = MODELS_FINDER.toResourcePath(id);
         JsonUnbakedModel jsonUnbakedModel2 = (JsonUnbakedModel)this.jsonUnbakedModels.get(identifier);
         if (jsonUnbakedModel2 == null) {
            throw new FileNotFoundException(identifier.toString());
         } else {
            jsonUnbakedModel2.id = id.toString();
            return jsonUnbakedModel2;
         }
      }
   }

   public Map getBakedModelMap() {
      return this.bakedModels;
   }

   public Object2IntMap getStateLookup() {
      return this.stateLookup;
   }

   static {
      FIRE_0 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/fire_0"));
      FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/fire_1"));
      LAVA_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/lava_flow"));
      WATER_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/water_flow"));
      WATER_OVERLAY = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.ofVanilla("block/water_overlay"));
      BANNER_BASE = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/banner_base"));
      SHIELD_BASE = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base"));
      SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.ofVanilla("entity/shield_base_nopattern"));
      BLOCK_DESTRUCTION_STAGES = (List)IntStream.range(0, 10).mapToObj((stage) -> Identifier.ofVanilla("block/destroy_stage_" + stage)).collect(Collectors.toList());
      BLOCK_DESTRUCTION_STAGE_TEXTURES = (List)BLOCK_DESTRUCTION_STAGES.stream().map((id) -> id.withPath((path) -> "textures/" + path + ".png")).collect(Collectors.toList());
      BLOCK_DESTRUCTION_RENDER_LAYERS = (List)BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(RenderLayer::getBlockBreaking).collect(Collectors.toList());
      LOGGER = LogUtils.getLogger();
      MISSING_ID = Identifier.ofVanilla("builtin/missing");
      MISSING_MODEL_ID = new ModelIdentifier(MISSING_ID, "missing");
      MODELS_FINDER = ResourceFinder.json("models");
      MISSING_DEFINITION = ("{    'textures': {       'particle': '" + MissingSprite.getMissingSpriteId().getPath() + "',       'missingno': '" + MissingSprite.getMissingSpriteId().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '"');
      BUILTIN_MODEL_DEFINITIONS = Map.of("missing", MISSING_DEFINITION);
      GENERATION_MARKER = (JsonUnbakedModel)Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"front\"}"), (model) -> model.id = "generation marker");
      BLOCK_ENTITY_MARKER = (JsonUnbakedModel)Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"side\"}"), (model) -> model.id = "block entity marker");
      ITEM_MODEL_GENERATOR = new ItemModelGenerator();
   }

   @Environment(EnvType.CLIENT)
   class BakerImpl implements Baker {
      private final Function textureGetter;

      BakerImpl(final SpriteGetter spriteGetter, final ModelIdentifier modelIdentifier) {
         this.textureGetter = (spriteId) -> spriteGetter.get(modelIdentifier, spriteId);
      }

      public UnbakedModel getOrLoadModel(Identifier id) {
         return ModelLoader.this.getOrLoadModel(id);
      }

      public BakedModel bake(Identifier id, ModelBakeSettings settings) {
         BakedModelCacheKey bakedModelCacheKey = new BakedModelCacheKey(id, settings.getRotation(), settings.isUvLocked());
         BakedModel bakedModel = (BakedModel)ModelLoader.this.bakedModelCache.get(bakedModelCacheKey);
         if (bakedModel != null) {
            return bakedModel;
         } else {
            UnbakedModel unbakedModel = this.getOrLoadModel(id);
            BakedModel bakedModel2 = this.bake(unbakedModel, settings);
            ModelLoader.this.bakedModelCache.put(bakedModelCacheKey, bakedModel2);
            return bakedModel2;
         }
      }

      @Nullable
      BakedModel bake(UnbakedModel model, ModelBakeSettings settings) {
         if (model instanceof JsonUnbakedModel jsonUnbakedModel) {
            if (jsonUnbakedModel.getRootModel() == ModelLoader.GENERATION_MARKER) {
               return ModelLoader.ITEM_MODEL_GENERATOR.create(this.textureGetter, jsonUnbakedModel).bake(this, jsonUnbakedModel, this.textureGetter, settings, false);
            }
         }

         return model.bake(this, this.textureGetter, settings);
      }
   }

   @Environment(EnvType.CLIENT)
   static record BakedModelCacheKey(Identifier id, AffineTransformation transformation, boolean isUvLocked) {
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface SpriteGetter {
      Sprite get(ModelIdentifier modelId, SpriteIdentifier spriteId);
   }
}
