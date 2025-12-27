package net.minecraft.client.render.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockStatesLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final int field_52259 = -1;
   private static final int field_52262 = 0;
   public static final ResourceFinder FINDER = ResourceFinder.json("blockstates");
   private static final Splitter COMMA_SPLITTER = Splitter.on(',');
   private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);
   private static final StateManager ITEM_FRAME_STATE_MANAGER;
   private static final Map STATIC_DEFINITIONS;
   private final Map blockStates;
   private final Profiler profiler;
   private final BlockColors blockColors;
   private final BiConsumer onLoad;
   private int lookupId = 1;
   private final Object2IntMap stateLookup = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), (map) -> map.defaultReturnValue(-1));
   private final BlockModel missingModel;
   private final ModelVariantMap.DeserializationContext context = new ModelVariantMap.DeserializationContext();

   public BlockStatesLoader(Map blockStates, Profiler profiler, UnbakedModel missingModel, BlockColors blockColors, BiConsumer onLoad) {
      this.blockStates = blockStates;
      this.profiler = profiler;
      this.blockColors = blockColors;
      this.onLoad = onLoad;
      ModelDefinition modelDefinition = new ModelDefinition(List.of(missingModel), List.of());
      this.missingModel = new BlockModel(missingModel, () -> modelDefinition);
   }

   public void load() {
      this.profiler.push("static_definitions");
      STATIC_DEFINITIONS.forEach(this::loadBlockStates);
      this.profiler.swap("blocks");

      for(Block block : Registries.BLOCK) {
         this.loadBlockStates(block.getRegistryEntry().registryKey().getValue(), block.getStateManager());
      }

      this.profiler.pop();
   }

   private void loadBlockStates(Identifier id, StateManager stateManager) {
      this.context.setStateFactory(stateManager);
      List<Property<?>> list = List.copyOf(this.blockColors.getProperties((Block)stateManager.getOwner()));
      List<BlockState> list2 = stateManager.getStates();
      Map<ModelIdentifier, BlockState> map = new HashMap();
      list2.forEach((state) -> map.put(BlockModels.getModelId(id, state), state));
      Map<BlockState, BlockModel> map2 = new HashMap();
      Identifier identifier = FINDER.toResourcePath(id);

      try {
         for(SourceTrackedData sourceTrackedData : (List)this.blockStates.getOrDefault(identifier, List.of())) {
            ModelVariantMap modelVariantMap = sourceTrackedData.readVariantMap(id, this.context);
            Map<BlockState, BlockModel> map3 = new IdentityHashMap();
            MultipartUnbakedModel multipartUnbakedModel;
            if (modelVariantMap.hasMultipartModel()) {
               multipartUnbakedModel = modelVariantMap.getMultipartModel();
               list2.forEach((state) -> map3.put(state, new BlockModel(multipartUnbakedModel, () -> BlockStatesLoader.ModelDefinition.create(state, (MultipartUnbakedModel)multipartUnbakedModel, list))));
            } else {
               multipartUnbakedModel = null;
            }

            modelVariantMap.getVariantMap().forEach((variant, model) -> {
               try {
                  list2.stream().filter(toStatePredicate(stateManager, variant)).forEach((state) -> {
                     BlockModel blockModel = (BlockModel)map3.put(state, new BlockModel(model, () -> BlockStatesLoader.ModelDefinition.create(state, (UnbakedModel)model, list)));
                     if (blockModel != null && blockModel.model != multipartUnbakedModel) {
                        map3.put(state, this.missingModel);
                        Optional var10002 = modelVariantMap.getVariantMap().entrySet().stream().filter((entry) -> entry.getValue() == blockModel.model).findFirst();
                        throw new RuntimeException("Overlapping definition with: " + (String)((Map.Entry)var10002.get()).getKey());
                     }
                  });
               } catch (Exception exception) {
                  LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", new Object[]{identifier, sourceTrackedData.source, variant, exception.getMessage()});
               }

            });
            map2.putAll(map3);
         }
      } catch (ModelLoaderException modelLoaderException) {
         LOGGER.warn("{}", modelLoaderException.getMessage());
      } catch (Exception exception) {
         LOGGER.warn("Exception loading blockstate definition: '{}'", identifier, exception);
      } finally {
         HashMap map5 = new HashMap();
         map.forEach((modelId, state) -> {
            BlockModel blockModel = (BlockModel)map2.get(state);
            if (blockModel == null) {
               LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", identifier, modelId);
               blockModel = this.missingModel;
            }

            this.onLoad.accept(modelId, blockModel.model);

            try {
               ModelDefinition modelDefinition = (ModelDefinition)blockModel.key().get();
               ((Set)map5.computeIfAbsent(modelDefinition, (definition) -> Sets.newIdentityHashSet())).add(state);
            } catch (Exception exception) {
               LOGGER.warn("Exception evaluating model definition: '{}'", modelId, exception);
            }

         });
         map5.forEach((definition, states) -> {
            Iterator<BlockState> iterator = states.iterator();

            while(iterator.hasNext()) {
               BlockState blockState = (BlockState)iterator.next();
               if (blockState.getRenderType() != BlockRenderType.MODEL) {
                  iterator.remove();
                  this.stateLookup.put(blockState, 0);
               }
            }

            if (states.size() > 1) {
               this.addStates(states);
            }

         });
      }

   }

   private static Predicate toStatePredicate(StateManager stateManager, String predicate) {
      Map<Property<?>, Comparable<?>> map = new HashMap();

      for(String string : COMMA_SPLITTER.split(predicate)) {
         Iterator<String> iterator = EQUAL_SIGN_SPLITTER.split(string).iterator();
         if (iterator.hasNext()) {
            String string2 = (String)iterator.next();
            Property<?> property = stateManager.getProperty(string2);
            if (property != null && iterator.hasNext()) {
               String string3 = (String)iterator.next();
               Comparable<?> comparable = parseProperty(property, string3);
               if (comparable == null) {
                  throw new RuntimeException("Unknown value: '" + string3 + "' for blockstate property: '" + string2 + "' " + String.valueOf(property.getValues()));
               }

               map.put(property, comparable);
            } else if (!string2.isEmpty()) {
               throw new RuntimeException("Unknown blockstate property: '" + string2 + "'");
            }
         }
      }

      Block block = (Block)stateManager.getOwner();
      return (state) -> {
         if (state != null && state.isOf(block)) {
            for(Map.Entry entry : map.entrySet()) {
               if (!Objects.equals(state.get((Property)entry.getKey()), entry.getValue())) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      };
   }

   @Nullable
   static Comparable parseProperty(Property property, String value) {
      return (Comparable)property.parse(value).orElse((Object)null);
   }

   private void addStates(Iterable states) {
      int i = this.lookupId++;
      states.forEach((state) -> this.stateLookup.put(state, i));
   }

   public Object2IntMap getStateLookup() {
      return this.stateLookup;
   }

   static {
      ITEM_FRAME_STATE_MANAGER = (new StateManager.Builder(Blocks.AIR)).add(new Property[]{BooleanProperty.of("map")}).build(Block::getDefaultState, BlockState::new);
      STATIC_DEFINITIONS = Map.of(Identifier.ofVanilla("item_frame"), ITEM_FRAME_STATE_MANAGER, Identifier.ofVanilla("glow_item_frame"), ITEM_FRAME_STATE_MANAGER);
   }

   @Environment(EnvType.CLIENT)
   static record ModelDefinition(List components, List values) {
      public static ModelDefinition create(BlockState state, MultipartUnbakedModel rawModel, Collection properties) {
         StateManager<Block, BlockState> stateManager = state.getBlock().getStateManager();
         List<UnbakedModel> list = (List)rawModel.getComponents().stream().filter((component) -> component.getPredicate(stateManager).test(state)).map(MultipartModelComponent::getModel).collect(Collectors.toUnmodifiableList());
         List<Object> list2 = getStateValues(state, properties);
         return new ModelDefinition(list, list2);
      }

      public static ModelDefinition create(BlockState state, UnbakedModel rawModel, Collection properties) {
         List<Object> list = getStateValues(state, properties);
         return new ModelDefinition(List.of(rawModel), list);
      }

      private static List getStateValues(BlockState state, Collection properties) {
         Stream var10000 = properties.stream();
         Objects.requireNonNull(state);
         return (List)var10000.map(state::get).collect(Collectors.toUnmodifiableList());
      }
   }

   @Environment(EnvType.CLIENT)
   static class ModelLoaderException extends RuntimeException {
      public ModelLoaderException(String message) {
         super(message);
      }
   }

   @Environment(EnvType.CLIENT)
   static record BlockModel(UnbakedModel model, Supplier key) {
      final UnbakedModel model;
   }

   @Environment(EnvType.CLIENT)
   public static record SourceTrackedData(String source, JsonElement data) {
      final String source;

      ModelVariantMap readVariantMap(Identifier id, ModelVariantMap.DeserializationContext context) {
         try {
            return ModelVariantMap.fromJson(context, this.data);
         } catch (Exception exception) {
            throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", id, this.source, exception.getMessage()));
         }
      }
   }
}
