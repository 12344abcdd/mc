package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PiglinEntityModel extends PlayerEntityModel {
   public final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelTransform bodyRotation;
   private final ModelTransform headRotation;
   private final ModelTransform leftArmRotation;
   private final ModelTransform rightArmRotation;

   public PiglinEntityModel(ModelPart modelPart) {
      super(modelPart, false);
      this.rightEar = this.head.getChild("right_ear");
      this.leftEar = this.head.getChild("left_ear");
      this.bodyRotation = this.body.getTransform();
      this.headRotation = this.head.getTransform();
      this.leftArmRotation = this.leftArm.getTransform();
      this.rightArmRotation = this.rightArm.getTransform();
   }

   public static ModelData getModelData(Dilation dilation) {
      ModelData modelData = PlayerEntityModel.getTexturedModelData(dilation, false);
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.NONE);
      addHead(dilation, modelData);
      modelPartData.addChild("hat", ModelPartBuilder.create(), ModelTransform.NONE);
      return modelData;
   }

   public static void addHead(Dilation dilation, ModelData baseModelData) {
      ModelPartData modelPartData = baseModelData.getRoot();
      ModelPartData modelPartData2 = modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, dilation).uv(31, 1).cuboid(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, dilation).uv(2, 4).cuboid(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, dilation).uv(2, 0).cuboid(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, dilation), ModelTransform.NONE);
      modelPartData2.addChild("left_ear", ModelPartBuilder.create().uv(51, 6).cuboid(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, dilation), ModelTransform.of(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (-(float)Math.PI / 6F)));
      modelPartData2.addChild("right_ear", ModelPartBuilder.create().uv(39, 6).cuboid(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, dilation), ModelTransform.of(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, ((float)Math.PI / 6F)));
   }

   public void setAngles(MobEntity mobEntity, float f, float g, float h, float i, float j) {
      this.body.setTransform(this.bodyRotation);
      this.head.setTransform(this.headRotation);
      this.leftArm.setTransform(this.leftArmRotation);
      this.rightArm.setTransform(this.rightArmRotation);
      super.setAngles((LivingEntity)mobEntity, f, g, h, i, j);
      float k = ((float)Math.PI / 6F);
      float l = h * 0.1F + f * 0.5F;
      float m = 0.08F + g * 0.4F;
      this.leftEar.roll = (-(float)Math.PI / 6F) - MathHelper.cos(l * 1.2F) * m;
      this.rightEar.roll = ((float)Math.PI / 6F) + MathHelper.cos(l) * m;
      if (mobEntity instanceof AbstractPiglinEntity abstractPiglinEntity) {
         PiglinActivity piglinActivity = abstractPiglinEntity.getActivity();
         if (piglinActivity == PiglinActivity.DANCING) {
            float n = h / 60.0F;
            this.rightEar.roll = ((float)Math.PI / 6F) + ((float)Math.PI / 180F) * MathHelper.sin(n * 30.0F) * 10.0F;
            this.leftEar.roll = (-(float)Math.PI / 6F) - ((float)Math.PI / 180F) * MathHelper.cos(n * 30.0F) * 10.0F;
            this.head.pivotX = MathHelper.sin(n * 10.0F);
            this.head.pivotY = MathHelper.sin(n * 40.0F) + 0.4F;
            this.rightArm.roll = ((float)Math.PI / 180F) * (70.0F + MathHelper.cos(n * 40.0F) * 10.0F);
            this.leftArm.roll = this.rightArm.roll * -1.0F;
            this.rightArm.pivotY = MathHelper.sin(n * 40.0F) * 0.5F + 1.5F;
            this.leftArm.pivotY = MathHelper.sin(n * 40.0F) * 0.5F + 1.5F;
            this.body.pivotY = MathHelper.sin(n * 40.0F) * 0.35F;
         } else if (piglinActivity == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON && this.handSwingProgress == 0.0F) {
            this.rotateMainArm(mobEntity);
         } else if (piglinActivity == PiglinActivity.CROSSBOW_HOLD) {
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, !mobEntity.isLeftHanded());
         } else if (piglinActivity == PiglinActivity.CROSSBOW_CHARGE) {
            CrossbowPosing.charge(this.rightArm, this.leftArm, mobEntity, !mobEntity.isLeftHanded());
         } else if (piglinActivity == PiglinActivity.ADMIRING_ITEM) {
            this.head.pitch = 0.5F;
            this.head.yaw = 0.0F;
            if (mobEntity.isLeftHanded()) {
               this.rightArm.yaw = -0.5F;
               this.rightArm.pitch = -0.9F;
            } else {
               this.leftArm.yaw = 0.5F;
               this.leftArm.pitch = -0.9F;
            }
         }
      } else if (mobEntity.getType() == EntityType.ZOMBIFIED_PIGLIN) {
         CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, mobEntity.isAttacking(), this.handSwingProgress, h);
      }

      this.leftPants.copyTransform(this.leftLeg);
      this.rightPants.copyTransform(this.rightLeg);
      this.leftSleeve.copyTransform(this.leftArm);
      this.rightSleeve.copyTransform(this.rightArm);
      this.jacket.copyTransform(this.body);
      this.hat.copyTransform(this.head);
   }

   protected void animateArms(MobEntity mobEntity, float f) {
      if (this.handSwingProgress > 0.0F && mobEntity instanceof PiglinEntity && ((PiglinEntity)mobEntity).getActivity() == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON) {
         CrossbowPosing.meleeAttack(this.rightArm, this.leftArm, mobEntity, this.handSwingProgress, f);
      } else {
         super.animateArms(mobEntity, f);
      }
   }

   private void rotateMainArm(MobEntity entity) {
      if (entity.isLeftHanded()) {
         this.leftArm.pitch = -1.8F;
      } else {
         this.rightArm.pitch = -1.8F;
      }

   }
}
