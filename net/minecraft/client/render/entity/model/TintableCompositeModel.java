package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public abstract class TintableCompositeModel extends SinglePartEntityModel {
   private int field_52152 = -1;

   public void setColorMultiplier(int i) {
      this.field_52152 = i;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
      super.render(matrices, vertices, light, overlay, Argb.mixColor(color, this.field_52152));
   }
}
