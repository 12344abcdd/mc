package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractHorseEntity;

@Environment(EnvType.CLIENT)
public abstract class AbstractHorseEntityRenderer extends MobEntityRenderer {
   private final float scale;

   public AbstractHorseEntityRenderer(EntityRendererFactory.Context ctx, HorseEntityModel model, float scale) {
      super(ctx, model, 0.75F);
      this.scale = scale;
   }

   protected void scale(AbstractHorseEntity abstractHorseEntity, MatrixStack matrixStack, float f) {
      matrixStack.scale(this.scale, this.scale, this.scale);
      super.scale(abstractHorseEntity, matrixStack, f);
   }
}
