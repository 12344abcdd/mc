package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RabbitEntityModel extends EntityModel {
   private static final float HAUNCH_JUMP_PITCH_MULTIPLIER = 50.0F;
   private static final float FRONT_LEGS_JUMP_PITCH_MULTIPLIER = -40.0F;
   private static final String LEFT_HAUNCH = "left_haunch";
   private static final String RIGHT_HAUNCH = "right_haunch";
   private final ModelPart leftHindLeg;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHaunch;
   private final ModelPart rightHaunch;
   private final ModelPart body;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart head;
   private final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelPart tail;
   private final ModelPart nose;
   private float jumpProgress;
   private static final float SCALE = 0.6F;

   public RabbitEntityModel(ModelPart root) {
      this.leftHindLeg = root.getChild("left_hind_foot");
      this.rightHindLeg = root.getChild("right_hind_foot");
      this.leftHaunch = root.getChild("left_haunch");
      this.rightHaunch = root.getChild("right_haunch");
      this.body = root.getChild("body");
      this.leftFrontLeg = root.getChild("left_front_leg");
      this.rightFrontLeg = root.getChild("right_front_leg");
      this.head = root.getChild("head");
      this.rightEar = root.getChild("right_ear");
      this.leftEar = root.getChild("left_ear");
      this.tail = root.getChild("tail");
      this.nose = root.getChild("nose");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("left_hind_foot", ModelPartBuilder.create().uv(26, 24).cuboid(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), ModelTransform.pivot(3.0F, 17.5F, 3.7F));
      modelPartData.addChild("right_hind_foot", ModelPartBuilder.create().uv(8, 24).cuboid(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), ModelTransform.pivot(-3.0F, 17.5F, 3.7F));
      modelPartData.addChild("left_haunch", ModelPartBuilder.create().uv(30, 15).cuboid(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F), ModelTransform.of(3.0F, 17.5F, 3.7F, -0.34906584F, 0.0F, 0.0F));
      modelPartData.addChild("right_haunch", ModelPartBuilder.create().uv(16, 15).cuboid(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F), ModelTransform.of(-3.0F, 17.5F, 3.7F, -0.34906584F, 0.0F, 0.0F));
      modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F), ModelTransform.of(0.0F, 19.0F, 8.0F, -0.34906584F, 0.0F, 0.0F));
      modelPartData.addChild("left_front_leg", ModelPartBuilder.create().uv(8, 15).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), ModelTransform.of(3.0F, 17.0F, -1.0F, -0.17453292F, 0.0F, 0.0F));
      modelPartData.addChild("right_front_leg", ModelPartBuilder.create().uv(0, 15).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), ModelTransform.of(-3.0F, 17.0F, -1.0F, -0.17453292F, 0.0F, 0.0F));
      modelPartData.addChild("head", ModelPartBuilder.create().uv(32, 0).cuboid(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F), ModelTransform.pivot(0.0F, 16.0F, -1.0F));
      modelPartData.addChild("right_ear", ModelPartBuilder.create().uv(52, 0).cuboid(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F), ModelTransform.of(0.0F, 16.0F, -1.0F, 0.0F, -0.2617994F, 0.0F));
      modelPartData.addChild("left_ear", ModelPartBuilder.create().uv(58, 0).cuboid(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F), ModelTransform.of(0.0F, 16.0F, -1.0F, 0.0F, 0.2617994F, 0.0F));
      modelPartData.addChild("tail", ModelPartBuilder.create().uv(52, 6).cuboid(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F), ModelTransform.of(0.0F, 20.0F, 7.0F, -0.3490659F, 0.0F, 0.0F));
      modelPartData.addChild("nose", ModelPartBuilder.create().uv(32, 9).cuboid(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F), ModelTransform.pivot(0.0F, 16.0F, -1.0F));
      return TexturedModelData.of(modelData, 64, 32);
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
      if (this.child) {
         float f = 1.5F;
         matrices.push();
         matrices.scale(0.56666666F, 0.56666666F, 0.56666666F);
         matrices.translate(0.0F, 1.375F, 0.125F);
         ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach((modelPart) -> modelPart.render(matrices, vertices, light, overlay, color));
         matrices.pop();
         matrices.push();
         matrices.scale(0.4F, 0.4F, 0.4F);
         matrices.translate(0.0F, 2.25F, 0.0F);
         ImmutableList.of(this.leftHindLeg, this.rightHindLeg, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail).forEach((modelPart) -> modelPart.render(matrices, vertices, light, overlay, color));
         matrices.pop();
      } else {
         matrices.push();
         matrices.scale(0.6F, 0.6F, 0.6F);
         matrices.translate(0.0F, 1.0F, 0.0F);
         ImmutableList.of(this.leftHindLeg, this.rightHindLeg, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.head, this.rightEar, this.leftEar, this.tail, this.nose, new ModelPart[0]).forEach((modelPart) -> modelPart.render(matrices, vertices, light, overlay, color));
         matrices.pop();
      }

   }

   public void setAngles(RabbitEntity rabbitEntity, float f, float g, float h, float i, float j) {
      float k = h - (float)rabbitEntity.age;
      this.nose.pitch = j * ((float)Math.PI / 180F);
      this.head.pitch = j * ((float)Math.PI / 180F);
      this.rightEar.pitch = j * ((float)Math.PI / 180F);
      this.leftEar.pitch = j * ((float)Math.PI / 180F);
      this.nose.yaw = i * ((float)Math.PI / 180F);
      this.head.yaw = i * ((float)Math.PI / 180F);
      this.rightEar.yaw = this.nose.yaw - 0.2617994F;
      this.leftEar.yaw = this.nose.yaw + 0.2617994F;
      this.jumpProgress = MathHelper.sin(rabbitEntity.getJumpProgress(k) * (float)Math.PI);
      this.leftHaunch.pitch = (this.jumpProgress * 50.0F - 21.0F) * ((float)Math.PI / 180F);
      this.rightHaunch.pitch = (this.jumpProgress * 50.0F - 21.0F) * ((float)Math.PI / 180F);
      this.leftHindLeg.pitch = this.jumpProgress * 50.0F * ((float)Math.PI / 180F);
      this.rightHindLeg.pitch = this.jumpProgress * 50.0F * ((float)Math.PI / 180F);
      this.leftFrontLeg.pitch = (this.jumpProgress * -40.0F - 11.0F) * ((float)Math.PI / 180F);
      this.rightFrontLeg.pitch = (this.jumpProgress * -40.0F - 11.0F) * ((float)Math.PI / 180F);
   }

   public void animateModel(RabbitEntity rabbitEntity, float f, float g, float h) {
      super.animateModel(rabbitEntity, f, g, h);
      this.jumpProgress = MathHelper.sin(rabbitEntity.getJumpProgress(h) * (float)Math.PI);
   }
}
