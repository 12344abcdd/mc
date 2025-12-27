package net.minecraft.client.render.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface BakedModel extends FabricBakedModel {
   List getQuads(@Nullable BlockState state, @Nullable Direction face, Random random);

   boolean useAmbientOcclusion();

   boolean hasDepth();

   boolean isSideLit();

   boolean isBuiltin();

   Sprite getParticleSprite();

   ModelTransformation getTransformation();

   ModelOverrideList getOverrides();
}
