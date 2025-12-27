package net.minecraft.client.render.debug;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class SupportingBlockDebugRenderer implements DebugRenderer.Renderer {
   private final MinecraftClient client;
   private double lastEntityCheckTime = Double.MIN_VALUE;
   private List entities = Collections.emptyList();

   public SupportingBlockDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      double d = (double)Util.getMeasuringTimeNano();
      if (d - this.lastEntityCheckTime > (double)1.0E8F) {
         this.lastEntityCheckTime = d;
         Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();
         this.entities = ImmutableList.copyOf(entity.getWorld().getOtherEntities(entity, entity.getBoundingBox().expand((double)16.0F)));
      }

      PlayerEntity playerEntity = this.client.player;
      if (playerEntity != null && playerEntity.supportingBlockPos.isPresent()) {
         this.renderBlockHighlights(matrices, vertexConsumers, cameraX, cameraY, cameraZ, playerEntity, () -> (double)0.0F, 1.0F, 0.0F, 0.0F);
      }

      for(Entity entity2 : this.entities) {
         if (entity2 != playerEntity) {
            this.renderBlockHighlights(matrices, vertexConsumers, cameraX, cameraY, cameraZ, entity2, () -> this.getAdditionalDilation(entity2), 0.0F, 1.0F, 0.0F);
         }
      }

   }

   private void renderBlockHighlights(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Entity entity, DoubleSupplier extraDilation, float red, float green, float blue) {
      entity.supportingBlockPos.ifPresent((pos) -> {
         double j = extraDilation.getAsDouble();
         BlockPos blockPos = entity.getSteppingPos();
         this.renderBlockHighlight(blockPos, matrices, cameraX, cameraY, cameraZ, vertexConsumers, 0.02 + j, red, green, blue);
         BlockPos blockPos2 = entity.getLandingPos();
         if (!blockPos2.equals(blockPos)) {
            this.renderBlockHighlight(blockPos2, matrices, cameraX, cameraY, cameraZ, vertexConsumers, 0.04 + j, 0.0F, 1.0F, 1.0F);
         }

      });
   }

   private double getAdditionalDilation(Entity entity) {
      return 0.02 * (double)(String.valueOf((double)entity.getId() + 0.132453657).hashCode() % 1000) / (double)1000.0F;
   }

   private void renderBlockHighlight(BlockPos pos, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, VertexConsumerProvider vertexConsumers, double dilation, float red, float green, float blue) {
      double d = (double)pos.getX() - cameraX - (double)2.0F * dilation;
      double e = (double)pos.getY() - cameraY - (double)2.0F * dilation;
      double f = (double)pos.getZ() - cameraZ - (double)2.0F * dilation;
      double g = d + (double)1.0F + (double)4.0F * dilation;
      double h = e + (double)1.0F + (double)4.0F * dilation;
      double i = f + (double)1.0F + (double)4.0F * dilation;
      WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), d, e, f, g, h, i, red, green, blue, 0.4F);
      WorldRenderer.drawShapeOutline(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), this.client.world.getBlockState(pos).getCollisionShape(this.client.world, pos, ShapeContext.absent()).offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), -cameraX, -cameraY, -cameraZ, red, green, blue, 1.0F, false);
   }
}
