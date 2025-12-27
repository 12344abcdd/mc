package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ElytraEntityModel extends AnimalModel {
   private final ModelPart rightWing;
   private final ModelPart leftWing;

   public ElytraEntityModel(ModelPart root) {
      this.leftWing = root.getChild("left_wing");
      this.rightWing = root.getChild("right_wing");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      Dilation dilation = new Dilation(1.0F);
      modelPartData.addChild("left_wing", ModelPartBuilder.create().uv(22, 0).cuboid(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, dilation), ModelTransform.of(5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, -0.2617994F));
      modelPartData.addChild("right_wing", ModelPartBuilder.create().uv(22, 0).mirrored().cuboid(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, dilation), ModelTransform.of(-5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, 0.2617994F));
      return TexturedModelData.of(modelData, 64, 32);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of();
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.leftWing, this.rightWing);
   }

   public void setAngles(LivingEntity livingEntity, float f, float g, float h, float i, float j) {
      float k = 0.2617994F;
      float l = -0.2617994F;
      float m = 0.0F;
      float n = 0.0F;
      if (livingEntity.isFallFlying()) {
         float o = 1.0F;
         Vec3d vec3d = livingEntity.getVelocity();
         if (vec3d.y < (double)0.0F) {
            Vec3d vec3d2 = vec3d.normalize();
            o = 1.0F - (float)Math.pow(-vec3d2.y, (double)1.5F);
         }

         k = o * 0.34906584F + (1.0F - o) * k;
         l = o * (-(float)Math.PI / 2F) + (1.0F - o) * l;
      } else if (livingEntity.isInSneakingPose()) {
         k = 0.6981317F;
         l = (-(float)Math.PI / 4F);
         m = 3.0F;
         n = 0.08726646F;
      }

      this.leftWing.pivotY = m;
      if (livingEntity instanceof AbstractClientPlayerEntity abstractClientPlayerEntity) {
         abstractClientPlayerEntity.elytraPitch += (k - abstractClientPlayerEntity.elytraPitch) * 0.1F;
         abstractClientPlayerEntity.elytraYaw += (n - abstractClientPlayerEntity.elytraYaw) * 0.1F;
         abstractClientPlayerEntity.elytraRoll += (l - abstractClientPlayerEntity.elytraRoll) * 0.1F;
         this.leftWing.pitch = abstractClientPlayerEntity.elytraPitch;
         this.leftWing.yaw = abstractClientPlayerEntity.elytraYaw;
         this.leftWing.roll = abstractClientPlayerEntity.elytraRoll;
      } else {
         this.leftWing.pitch = k;
         this.leftWing.roll = l;
         this.leftWing.yaw = n;
      }

      this.rightWing.yaw = -this.leftWing.yaw;
      this.rightWing.pivotY = this.leftWing.pivotY;
      this.rightWing.pitch = this.leftWing.pitch;
      this.rightWing.roll = -this.leftWing.roll;
   }
}
