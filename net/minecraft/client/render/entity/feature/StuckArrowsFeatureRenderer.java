package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class StuckArrowsFeatureRenderer extends StuckObjectsFeatureRenderer {
   private final EntityRenderDispatcher dispatcher;

   public StuckArrowsFeatureRenderer(EntityRendererFactory.Context context, LivingEntityRenderer entityRenderer) {
      super(entityRenderer);
      this.dispatcher = context.getRenderDispatcher();
   }

   protected int getObjectCount(LivingEntity entity) {
      return entity.getStuckArrowCount();
   }

   protected void renderObject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float directionX, float directionY, float directionZ, float tickDelta) {
      float f = MathHelper.sqrt(directionX * directionX + directionZ * directionZ);
      ArrowEntity arrowEntity = new ArrowEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), ItemStack.EMPTY, (ItemStack)null);
      arrowEntity.setYaw((float)(Math.atan2((double)directionX, (double)directionZ) * (double)(180F / (float)Math.PI)));
      arrowEntity.setPitch((float)(Math.atan2((double)directionY, (double)f) * (double)(180F / (float)Math.PI)));
      arrowEntity.prevYaw = arrowEntity.getYaw();
      arrowEntity.prevPitch = arrowEntity.getPitch();
      this.dispatcher.render(arrowEntity, (double)0.0F, (double)0.0F, (double)0.0F, 0.0F, tickDelta, matrices, vertexConsumers, light);
   }
}
