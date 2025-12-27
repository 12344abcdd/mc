package net.minecraft.client.render.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class MultipartBakedModel implements BakedModel {
   private final List components;
   protected final boolean ambientOcclusion;
   protected final boolean depthGui;
   protected final boolean sideLit;
   protected final Sprite sprite;
   protected final ModelTransformation transformations;
   protected final ModelOverrideList itemPropertyOverrides;
   private final Map stateCache = new Reference2ObjectOpenHashMap();

   public MultipartBakedModel(List components) {
      this.components = components;
      BakedModel bakedModel = (BakedModel)((Pair)components.iterator().next()).getRight();
      this.ambientOcclusion = bakedModel.useAmbientOcclusion();
      this.depthGui = bakedModel.hasDepth();
      this.sideLit = bakedModel.isSideLit();
      this.sprite = bakedModel.getParticleSprite();
      this.transformations = bakedModel.getTransformation();
      this.itemPropertyOverrides = bakedModel.getOverrides();
   }

   public List getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
      if (state == null) {
         return Collections.emptyList();
      } else {
         BitSet bitSet = (BitSet)this.stateCache.get(state);
         if (bitSet == null) {
            bitSet = new BitSet();

            for(int i = 0; i < this.components.size(); ++i) {
               Pair<Predicate<BlockState>, BakedModel> pair = (Pair)this.components.get(i);
               if (((Predicate)pair.getLeft()).test(state)) {
                  bitSet.set(i);
               }
            }

            this.stateCache.put(state, bitSet);
         }

         List<BakedQuad> list = Lists.newArrayList();
         long l = random.nextLong();

         for(int j = 0; j < bitSet.length(); ++j) {
            if (bitSet.get(j)) {
               list.addAll(((BakedModel)((Pair)this.components.get(j)).getRight()).getQuads(state, face, Random.create(l)));
            }
         }

         return list;
      }
   }

   public boolean useAmbientOcclusion() {
      return this.ambientOcclusion;
   }

   public boolean hasDepth() {
      return this.depthGui;
   }

   public boolean isSideLit() {
      return this.sideLit;
   }

   public boolean isBuiltin() {
      return false;
   }

   public Sprite getParticleSprite() {
      return this.sprite;
   }

   public ModelTransformation getTransformation() {
      return this.transformations;
   }

   public ModelOverrideList getOverrides() {
      return this.itemPropertyOverrides;
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final List components = Lists.newArrayList();

      public void addComponent(Predicate predicate, BakedModel model) {
         this.components.add(Pair.of(predicate, model));
      }

      public BakedModel build() {
         return new MultipartBakedModel(this.components);
      }
   }
}
