package net.minecraft.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AsyncTexture extends ResourceTexture {
   @Nullable
   private CompletableFuture future;

   public AsyncTexture(ResourceManager resourceManager, Identifier id, Executor executor) {
      super(id);
      this.future = CompletableFuture.supplyAsync(() -> ResourceTexture.TextureData.load(resourceManager, id), executor);
   }

   protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
      if (this.future != null) {
         ResourceTexture.TextureData textureData = (ResourceTexture.TextureData)this.future.join();
         this.future = null;
         return textureData;
      } else {
         return ResourceTexture.TextureData.load(resourceManager, this.location);
      }
   }

   public CompletableFuture getLoadCompleteFuture() {
      return this.future == null ? CompletableFuture.completedFuture((Object)null) : this.future.thenApply((texture) -> null);
   }

   public void registerTexture(TextureManager textureManager, ResourceManager resourceManager, Identifier id, Executor executor) {
      this.future = CompletableFuture.supplyAsync(() -> ResourceTexture.TextureData.load(resourceManager, this.location), Util.getMainWorkerExecutor());
      this.future.thenRunAsync(() -> textureManager.registerTexture(this.location, this), createRenderThreadExecutor(executor));
   }

   private static Executor createRenderThreadExecutor(Executor executor) {
      return (runnable) -> executor.execute(() -> {
            Objects.requireNonNull(runnable);
            RenderSystem.recordRenderCall(runnable::run);
         });
   }
}
