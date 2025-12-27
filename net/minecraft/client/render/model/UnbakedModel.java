package net.minecraft.client.render.model;

import java.util.Collection;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface UnbakedModel {
   Collection getModelDependencies();

   void setParents(Function modelLoader);

   @Nullable
   BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer);
}
