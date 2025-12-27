package net.minecraft.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class DisplayEntityRenderer extends EntityRenderer {
   private final EntityRenderDispatcher renderDispatcher;

   protected DisplayEntityRenderer(EntityRendererFactory.Context context) {
      super(context);
      this.renderDispatcher = context.getRenderDispatcher();
   }

   public Identifier getTexture(DisplayEntity displayEntity) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }

   public void render(DisplayEntity displayEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      DisplayEntity.RenderState renderState = displayEntity.getRenderState();
      if (renderState != null) {
         S object = (S)this.getData(displayEntity);
         if (object != null) {
            float h = displayEntity.getLerpProgress(g);
            this.shadowRadius = renderState.shadowRadius().lerp(h);
            this.shadowOpacity = renderState.shadowStrength().lerp(h);
            int j = renderState.brightnessOverride();
            int k = j != -1 ? j : i;
            super.render(displayEntity, f, g, matrixStack, vertexConsumerProvider, k);
            matrixStack.push();
            matrixStack.multiply(this.getBillboardRotation(renderState, displayEntity, g, new Quaternionf()));
            AffineTransformation affineTransformation = (AffineTransformation)renderState.transformation().interpolate(h);
            matrixStack.multiplyPositionMatrix(affineTransformation.getMatrix());
            this.render(displayEntity, object, matrixStack, vertexConsumerProvider, k, h);
            matrixStack.pop();
         }
      }
   }

   private Quaternionf getBillboardRotation(DisplayEntity.RenderState renderState, DisplayEntity entity, float yaw, Quaternionf rotation) {
      Camera camera = this.renderDispatcher.camera;
      Quaternionf var10000;
      switch (renderState.billboardConstraints()) {
         case FIXED -> var10000 = rotation.rotationYXZ(-0.017453292F * lerpYaw(entity, yaw), ((float)Math.PI / 180F) * lerpPitch(entity, yaw), 0.0F);
         case HORIZONTAL -> var10000 = rotation.rotationYXZ(-0.017453292F * lerpYaw(entity, yaw), ((float)Math.PI / 180F) * getNegatedPitch(camera), 0.0F);
         case VERTICAL -> var10000 = rotation.rotationYXZ(-0.017453292F * getBackwardsYaw(camera), ((float)Math.PI / 180F) * lerpPitch(entity, yaw), 0.0F);
         case CENTER -> var10000 = rotation.rotationYXZ(-0.017453292F * getBackwardsYaw(camera), ((float)Math.PI / 180F) * getNegatedPitch(camera), 0.0F);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private static float getBackwardsYaw(Camera camera) {
      return camera.getYaw() - 180.0F;
   }

   private static float getNegatedPitch(Camera camera) {
      return -camera.getPitch();
   }

   private static float lerpYaw(DisplayEntity entity, float delta) {
      return MathHelper.lerpAngleDegrees(delta, entity.prevYaw, entity.getYaw());
   }

   private static float lerpPitch(DisplayEntity entity, float delta) {
      return MathHelper.lerp(delta, entity.prevPitch, entity.getPitch());
   }

   @Nullable
   protected abstract Object getData(DisplayEntity entity);

   protected abstract void render(DisplayEntity entity, Object data, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int brightness, float lerpProgress);

   @Environment(EnvType.CLIENT)
   public static class BlockDisplayEntityRenderer extends DisplayEntityRenderer {
      private final BlockRenderManager blockRenderManager;

      protected BlockDisplayEntityRenderer(EntityRendererFactory.Context context) {
         super(context);
         this.blockRenderManager = context.getBlockRenderManager();
      }

      @Nullable
      protected DisplayEntity.BlockDisplayEntity.Data getData(DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
         return blockDisplayEntity.getData();
      }

      public void render(DisplayEntity.BlockDisplayEntity blockDisplayEntity, DisplayEntity.BlockDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
         this.blockRenderManager.renderBlockAsEntity(data.blockState(), matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(final DisplayEntity entity) {
         return this.getData((DisplayEntity.BlockDisplayEntity)entity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ItemDisplayEntityRenderer extends DisplayEntityRenderer {
      private final ItemRenderer itemRenderer;

      protected ItemDisplayEntityRenderer(EntityRendererFactory.Context context) {
         super(context);
         this.itemRenderer = context.getItemRenderer();
      }

      @Nullable
      protected DisplayEntity.ItemDisplayEntity.Data getData(DisplayEntity.ItemDisplayEntity itemDisplayEntity) {
         return itemDisplayEntity.getData();
      }

      public void render(DisplayEntity.ItemDisplayEntity itemDisplayEntity, DisplayEntity.ItemDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)Math.PI));
         this.itemRenderer.renderItem(data.itemStack(), data.itemTransform(), i, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, itemDisplayEntity.getWorld(), itemDisplayEntity.getId());
      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(final DisplayEntity entity) {
         return this.getData((DisplayEntity.ItemDisplayEntity)entity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class TextDisplayEntityRenderer extends DisplayEntityRenderer {
      private final TextRenderer displayTextRenderer;

      protected TextDisplayEntityRenderer(EntityRendererFactory.Context context) {
         super(context);
         this.displayTextRenderer = context.getTextRenderer();
      }

      private DisplayEntity.TextDisplayEntity.TextLines getLines(Text text, int width) {
         List<OrderedText> list = this.displayTextRenderer.wrapLines(text, width);
         List<DisplayEntity.TextDisplayEntity.TextLine> list2 = new ArrayList(list.size());
         int i = 0;

         for(OrderedText orderedText : list) {
            int j = this.displayTextRenderer.getWidth(orderedText);
            i = Math.max(i, j);
            list2.add(new DisplayEntity.TextDisplayEntity.TextLine(orderedText, j));
         }

         return new DisplayEntity.TextDisplayEntity.TextLines(list2, i);
      }

      @Nullable
      protected DisplayEntity.TextDisplayEntity.Data getData(DisplayEntity.TextDisplayEntity textDisplayEntity) {
         return textDisplayEntity.getData();
      }

      public void render(DisplayEntity.TextDisplayEntity textDisplayEntity, DisplayEntity.TextDisplayEntity.Data data, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, float f) {
         byte b = data.flags();
         boolean bl = (b & 2) != 0;
         boolean bl2 = (b & 4) != 0;
         boolean bl3 = (b & 1) != 0;
         DisplayEntity.TextDisplayEntity.TextAlignment textAlignment = TextDisplayEntity.getAlignment(b);
         byte c = (byte)data.textOpacity().lerp(f);
         int j;
         if (bl2) {
            float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
            j = (int)(g * 255.0F) << 24;
         } else {
            j = data.backgroundColor().lerp(f);
         }

         float g = 0.0F;
         Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
         matrix4f.rotate((float)Math.PI, 0.0F, 1.0F, 0.0F);
         matrix4f.scale(-0.025F, -0.025F, -0.025F);
         DisplayEntity.TextDisplayEntity.TextLines textLines = textDisplayEntity.splitLines(this::getLines);
         Objects.requireNonNull(this.displayTextRenderer);
         int k = 9 + 1;
         int l = textLines.width();
         int m = textLines.lines().size() * k;
         matrix4f.translate(1.0F - (float)l / 2.0F, (float)(-m), 0.0F);
         if (j != 0) {
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(bl ? RenderLayer.getTextBackgroundSeeThrough() : RenderLayer.getTextBackground());
            vertexConsumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(j).light(i);
            vertexConsumer.vertex(matrix4f, -1.0F, (float)m, 0.0F).color(j).light(i);
            vertexConsumer.vertex(matrix4f, (float)l, (float)m, 0.0F).color(j).light(i);
            vertexConsumer.vertex(matrix4f, (float)l, -1.0F, 0.0F).color(j).light(i);
         }

         for(DisplayEntity.TextDisplayEntity.TextLine textLine : textLines.lines()) {
            float var10000;
            switch (textAlignment) {
               case LEFT -> var10000 = 0.0F;
               case RIGHT -> var10000 = (float)(l - textLine.width());
               case CENTER -> var10000 = (float)l / 2.0F - (float)textLine.width() / 2.0F;
               default -> throw new MatchException((String)null, (Throwable)null);
            }

            float h = var10000;
            this.displayTextRenderer.draw((OrderedText)textLine.contents(), h, g, c << 24 | 16777215, bl3, matrix4f, vertexConsumerProvider, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.POLYGON_OFFSET, 0, i);
            g += (float)k;
         }

      }

      // $FF: synthetic method
      @Nullable
      protected Object getData(final DisplayEntity entity) {
         return this.getData((DisplayEntity.TextDisplayEntity)entity);
      }
   }
}
