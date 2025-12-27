package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class ShulkerBulletEntityModel extends SinglePartEntityModel {
   private static final String MAIN = "main";
   private final ModelPart root;
   private final ModelPart bullet;

   public ShulkerBulletEntityModel(ModelPart root) {
      this.root = root;
      this.bullet = root.getChild("main");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, -1.0F, 8.0F, 8.0F, 2.0F).uv(0, 10).cuboid(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F).uv(20, 0).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F), ModelTransform.NONE);
      return TexturedModelData.of(modelData, 64, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.bullet.yaw = headYaw * ((float)Math.PI / 180F);
      this.bullet.pitch = headPitch * ((float)Math.PI / 180F);
   }
}
