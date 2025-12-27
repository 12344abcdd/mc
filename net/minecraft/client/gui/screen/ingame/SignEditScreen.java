package net.minecraft.client.gui.screen.ingame;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SignEditScreen extends AbstractSignEditScreen {
   public static final float BACKGROUND_SCALE = 62.500004F;
   public static final float TEXT_SCALE_MULTIPLIER = 0.9765628F;
   private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
   @Nullable
   private SignBlockEntityRenderer.SignModel model;

   public SignEditScreen(SignBlockEntity sign, boolean filtered, boolean bl) {
      super(sign, filtered, bl);
   }

   protected void init() {
      super.init();
      this.model = SignBlockEntityRenderer.createSignModel(this.client.getEntityModelLoader(), this.signType);
   }

   protected void translateForRender(DrawContext context, BlockState state) {
      super.translateForRender(context, state);
      boolean bl = state.getBlock() instanceof SignBlock;
      if (!bl) {
         context.getMatrices().translate(0.0F, 35.0F, 0.0F);
      }

   }

   protected void renderSignBackground(DrawContext context, BlockState state) {
      if (this.model != null) {
         boolean bl = state.getBlock() instanceof SignBlock;
         context.getMatrices().translate(0.0F, 31.0F, 0.0F);
         context.getMatrices().scale(62.500004F, 62.500004F, -62.500004F);
         SpriteIdentifier spriteIdentifier = TexturedRenderLayers.getSignTextureId(this.signType);
         VertexConsumerProvider.Immediate var10001 = context.getVertexConsumers();
         SignBlockEntityRenderer.SignModel var10002 = this.model;
         Objects.requireNonNull(var10002);
         VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(var10001, var10002::getLayer);
         this.model.stick.visible = bl;
         this.model.root.render(context.getMatrices(), vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV);
      }
   }

   protected Vector3f getTextScale() {
      return TEXT_SCALE;
   }
}
