package net.minecraft.client.render.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.custom.DebugBreezeCustomPayload;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BreezeDebugRenderer {
   private static final int PINK = Argb.getArgb(255, 255, 100, 255);
   private static final int LIGHT_BLUE = Argb.getArgb(255, 100, 255, 255);
   private static final int GREEN = Argb.getArgb(255, 0, 255, 0);
   private static final int ORANGE = Argb.getArgb(255, 255, 165, 0);
   private static final int RED = Argb.getArgb(255, 255, 0, 0);
   private static final int field_47470 = 20;
   private static final float field_47471 = ((float)Math.PI / 10F);
   private final MinecraftClient client;
   private final Map breezes = new HashMap();

   public BreezeDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      ClientPlayerEntity clientPlayerEntity = this.client.player;
      clientPlayerEntity.getWorld().getEntitiesByType(EntityType.BREEZE, clientPlayerEntity.getBoundingBox().expand((double)100.0F), (entity) -> true).forEach((breeze) -> {
         Optional<DebugBreezeCustomPayload.BreezeInfo> optional = Optional.ofNullable((DebugBreezeCustomPayload.BreezeInfo)this.breezes.get(breeze.getId()));
         optional.map(DebugBreezeCustomPayload.BreezeInfo::attackTarget).map((attackTarget) -> clientPlayerEntity.getWorld().getEntityById(attackTarget)).map((attackTarget) -> attackTarget.getLerpedPos(this.client.getRenderTickCounter().getTickDelta(true))).ifPresent((targetPos) -> {
            drawLine(matrices, vertexConsumers, cameraX, cameraY, cameraZ, breeze.getPos(), targetPos, LIGHT_BLUE);
            Vec3d vec3d = targetPos.add((double)0.0F, (double)0.01F, (double)0.0F);
            drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip((double)2.0F)), vec3d, 4.0F, GREEN);
            drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip((double)2.0F)), vec3d, 8.0F, ORANGE);
            drawCurve(matrices.peek().getPositionMatrix(), cameraX, cameraY, cameraZ, vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip((double)2.0F)), vec3d, 20.0F, RED);
         });
         optional.map(DebugBreezeCustomPayload.BreezeInfo::jumpTarget).ifPresent((jumpTarget) -> {
            drawLine(matrices, vertexConsumers, cameraX, cameraY, cameraZ, breeze.getPos(), jumpTarget.toCenterPos(), PINK);
            DebugRenderer.drawBox(matrices, vertexConsumers, Box.from(Vec3d.of(jumpTarget)).offset(-cameraX, -cameraY, -cameraZ), 1.0F, 0.0F, 0.0F, 1.0F);
         });
      });
   }

   private static void drawLine(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, Vec3d entityPos, Vec3d targetPos, int color) {
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip((double)2.0F));
      vertexConsumer.vertex(matrices.peek(), (float)(entityPos.x - cameraX), (float)(entityPos.y - cameraY), (float)(entityPos.z - cameraZ)).color(color);
      vertexConsumer.vertex(matrices.peek(), (float)(targetPos.x - cameraX), (float)(targetPos.y - cameraY), (float)(targetPos.z - cameraZ)).color(color);
   }

   private static void drawCurve(Matrix4f matrix, double cameraX, double cameraY, double cameraZ, VertexConsumer vertexConsumer, Vec3d targetPos, float multiplier, int color) {
      for(int i = 0; i < 20; ++i) {
         drawCurvePart(i, matrix, cameraX, cameraY, cameraZ, vertexConsumer, targetPos, multiplier, color);
      }

      drawCurvePart(0, matrix, cameraX, cameraY, cameraZ, vertexConsumer, targetPos, multiplier, color);
   }

   private static void drawCurvePart(int index, Matrix4f matrix, double cameraX, double cameraY, double cameraZ, VertexConsumer vertexConsumer, Vec3d targetPos, float multiplier, int color) {
      float f = (float)index * ((float)Math.PI / 10F);
      Vec3d vec3d = targetPos.add((double)multiplier * Math.cos((double)f), (double)0.0F, (double)multiplier * Math.sin((double)f));
      vertexConsumer.vertex(matrix, (float)(vec3d.x - cameraX), (float)(vec3d.y - cameraY), (float)(vec3d.z - cameraZ)).color(color);
   }

   public void clear() {
      this.breezes.clear();
   }

   public void addBreezeDebugInfo(DebugBreezeCustomPayload.BreezeInfo breezeDebugInfo) {
      this.breezes.put(breezeDebugInfo.id(), breezeDebugInfo);
   }
}
