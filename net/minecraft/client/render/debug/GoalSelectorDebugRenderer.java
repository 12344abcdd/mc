package net.minecraft.client.render.debug;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.custom.DebugGoalSelectorCustomPayload;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.Renderer {
   private static final int RANGE = 160;
   private final MinecraftClient client;
   private final Int2ObjectMap goalSelectors = new Int2ObjectOpenHashMap();

   public void clear() {
      this.goalSelectors.clear();
   }

   public void setGoalSelectorList(int index, BlockPos pos, List goals) {
      this.goalSelectors.put(index, new Entity(pos, goals));
   }

   public void removeGoalSelectorList(int index) {
      this.goalSelectors.remove(index);
   }

   public GoalSelectorDebugRenderer(MinecraftClient client) {
      this.client = client;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      Camera camera = this.client.gameRenderer.getCamera();
      BlockPos blockPos = BlockPos.ofFloored(camera.getPos().x, (double)0.0F, camera.getPos().z);
      ObjectIterator var11 = this.goalSelectors.values().iterator();

      while(var11.hasNext()) {
         Entity entity = (Entity)var11.next();
         BlockPos blockPos2 = entity.entityPos;
         if (blockPos.isWithinDistance(blockPos2, (double)160.0F)) {
            for(int i = 0; i < entity.goals.size(); ++i) {
               DebugGoalSelectorCustomPayload.Goal goal = (DebugGoalSelectorCustomPayload.Goal)entity.goals.get(i);
               double d = (double)blockPos2.getX() + (double)0.5F;
               double e = (double)blockPos2.getY() + (double)2.0F + (double)i * (double)0.25F;
               double f = (double)blockPos2.getZ() + (double)0.5F;
               int j = goal.isRunning() ? -16711936 : -3355444;
               DebugRenderer.drawString(matrices, vertexConsumers, goal.name(), d, e, f, j);
            }
         }
      }

   }

   @Environment(EnvType.CLIENT)
   static record Entity(BlockPos entityPos, List goals) {
      final BlockPos entityPos;
      final List goals;
   }
}
