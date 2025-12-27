package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BipedEntityModel extends AnimalModel implements ModelWithArms, ModelWithHead {
   public static final float field_32505 = 0.25F;
   public static final float field_32506 = 0.5F;
   public static final float field_42513 = -0.1F;
   private static final float field_42512 = 0.005F;
   private static final float SPYGLASS_ARM_YAW_OFFSET = 0.2617994F;
   private static final float SPYGLASS_ARM_PITCH_OFFSET = 1.9198622F;
   private static final float SPYGLASS_SNEAKING_ARM_PITCH_OFFSET = 0.2617994F;
   private static final float field_46576 = -1.3962634F;
   private static final float field_46577 = 0.43633232F;
   private static final float field_46724 = ((float)Math.PI / 6F);
   public static final float field_39069 = 1.4835298F;
   public static final float field_39070 = ((float)Math.PI / 6F);
   public final ModelPart head;
   public final ModelPart hat;
   public final ModelPart body;
   public final ModelPart rightArm;
   public final ModelPart leftArm;
   public final ModelPart rightLeg;
   public final ModelPart leftLeg;
   public ArmPose leftArmPose;
   public ArmPose rightArmPose;
   public boolean sneaking;
   public float leaningPitch;

   public BipedEntityModel(ModelPart root) {
      this(root, RenderLayer::getEntityCutoutNoCull);
   }

   public BipedEntityModel(ModelPart root, Function renderLayerFactory) {
      super(renderLayerFactory, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.head = root.getChild("head");
      this.hat = root.getChild("hat");
      this.body = root.getChild("body");
      this.rightArm = root.getChild("right_arm");
      this.leftArm = root.getChild("left_arm");
      this.rightLeg = root.getChild("right_leg");
      this.leftLeg = root.getChild("left_leg");
   }

   public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("hat", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.add(0.5F)), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("body", ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(0.0F, 0.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-5.0F, 2.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 2.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(-1.9F, 12.0F + pivotOffsetY, 0.0F));
      modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(1.9F, 12.0F + pivotOffsetY, 0.0F));
      return modelData;
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void animateModel(LivingEntity livingEntity, float f, float g, float h) {
      this.leaningPitch = livingEntity.getLeaningPitch(h);
      super.animateModel(livingEntity, f, g, h);
   }

   public void setAngles(LivingEntity livingEntity, float f, float g, float h, float i, float j) {
      boolean bl = livingEntity.getFallFlyingTicks() > 4;
      boolean bl2 = livingEntity.isInSwimmingPose();
      this.head.yaw = i * ((float)Math.PI / 180F);
      if (bl) {
         this.head.pitch = (-(float)Math.PI / 4F);
      } else if (this.leaningPitch > 0.0F) {
         if (bl2) {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, (-(float)Math.PI / 4F));
         } else {
            this.head.pitch = this.lerpAngle(this.leaningPitch, this.head.pitch, j * ((float)Math.PI / 180F));
         }
      } else {
         this.head.pitch = j * ((float)Math.PI / 180F);
      }

      this.body.yaw = 0.0F;
      this.rightArm.pivotZ = 0.0F;
      this.rightArm.pivotX = -5.0F;
      this.leftArm.pivotZ = 0.0F;
      this.leftArm.pivotX = 5.0F;
      float k = 1.0F;
      if (bl) {
         k = (float)livingEntity.getVelocity().lengthSquared();
         k /= 0.2F;
         k *= k * k;
      }

      if (k < 1.0F) {
         k = 1.0F;
      }

      this.rightArm.pitch = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 2.0F * g * 0.5F / k;
      this.leftArm.pitch = MathHelper.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
      this.rightArm.roll = 0.0F;
      this.leftArm.roll = 0.0F;
      this.rightLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g / k;
      this.leftLeg.pitch = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * g / k;
      this.rightLeg.yaw = 0.005F;
      this.leftLeg.yaw = -0.005F;
      this.rightLeg.roll = 0.005F;
      this.leftLeg.roll = -0.005F;
      if (this.riding) {
         ModelPart var10000 = this.rightArm;
         var10000.pitch += (-(float)Math.PI / 5F);
         var10000 = this.leftArm;
         var10000.pitch += (-(float)Math.PI / 5F);
         this.rightLeg.pitch = -1.4137167F;
         this.rightLeg.yaw = ((float)Math.PI / 10F);
         this.rightLeg.roll = 0.07853982F;
         this.leftLeg.pitch = -1.4137167F;
         this.leftLeg.yaw = (-(float)Math.PI / 10F);
         this.leftLeg.roll = -0.07853982F;
      }

      this.rightArm.yaw = 0.0F;
      this.leftArm.yaw = 0.0F;
      boolean bl3 = livingEntity.getMainArm() == Arm.RIGHT;
      if (livingEntity.isUsingItem()) {
         boolean bl4 = livingEntity.getActiveHand() == Hand.MAIN_HAND;
         if (bl4 == bl3) {
            this.positionRightArm(livingEntity);
         } else {
            this.positionLeftArm(livingEntity);
         }
      } else {
         boolean bl4 = bl3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
         if (bl3 != bl4) {
            this.positionLeftArm(livingEntity);
            this.positionRightArm(livingEntity);
         } else {
            this.positionRightArm(livingEntity);
            this.positionLeftArm(livingEntity);
         }
      }

      this.animateArms(livingEntity, h);
      if (this.sneaking) {
         this.body.pitch = 0.5F;
         ModelPart var24 = this.rightArm;
         var24.pitch += 0.4F;
         var24 = this.leftArm;
         var24.pitch += 0.4F;
         this.rightLeg.pivotZ = 4.0F;
         this.leftLeg.pivotZ = 4.0F;
         this.rightLeg.pivotY = 12.2F;
         this.leftLeg.pivotY = 12.2F;
         this.head.pivotY = 4.2F;
         this.body.pivotY = 3.2F;
         this.leftArm.pivotY = 5.2F;
         this.rightArm.pivotY = 5.2F;
      } else {
         this.body.pitch = 0.0F;
         this.rightLeg.pivotZ = 0.0F;
         this.leftLeg.pivotZ = 0.0F;
         this.rightLeg.pivotY = 12.0F;
         this.leftLeg.pivotY = 12.0F;
         this.head.pivotY = 0.0F;
         this.body.pivotY = 0.0F;
         this.leftArm.pivotY = 2.0F;
         this.rightArm.pivotY = 2.0F;
      }

      if (this.rightArmPose != BipedEntityModel.ArmPose.SPYGLASS) {
         CrossbowPosing.swingArm(this.rightArm, h, 1.0F);
      }

      if (this.leftArmPose != BipedEntityModel.ArmPose.SPYGLASS) {
         CrossbowPosing.swingArm(this.leftArm, h, -1.0F);
      }

      if (this.leaningPitch > 0.0F) {
         float l = f % 26.0F;
         Arm arm = this.getPreferredArm(livingEntity);
         float m = arm == Arm.RIGHT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         float n = arm == Arm.LEFT && this.handSwingProgress > 0.0F ? 0.0F : this.leaningPitch;
         if (!livingEntity.isUsingItem()) {
            if (l < 14.0F) {
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 0.0F);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 0.0F);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, (float)Math.PI + 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, (float)Math.PI - 1.8707964F * this.method_2807(l) / this.method_2807(14.0F));
            } else if (l >= 14.0F && l < 22.0F) {
               float o = (l - 14.0F) / 8.0F;
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, ((float)Math.PI / 2F) * o);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, ((float)Math.PI / 2F) * o);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 5.012389F - 1.8707964F * o);
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 1.2707963F + 1.8707964F * o);
            } else if (l >= 22.0F && l < 26.0F) {
               float o = (l - 22.0F) / 4.0F;
               this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * o);
               this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * o);
               this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
               this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
               this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, (float)Math.PI);
               this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, (float)Math.PI);
            }
         }

         float o = 0.3F;
         float p = 0.33333334F;
         this.leftLeg.pitch = MathHelper.lerp(this.leaningPitch, this.leftLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F + (float)Math.PI));
         this.rightLeg.pitch = MathHelper.lerp(this.leaningPitch, this.rightLeg.pitch, 0.3F * MathHelper.cos(f * 0.33333334F));
      }

      this.hat.copyTransform(this.head);
   }

   private void positionRightArm(LivingEntity entity) {
      switch (this.rightArmPose.ordinal()) {
         case 0:
            this.rightArm.yaw = 0.0F;
            break;
         case 1:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - ((float)Math.PI / 10F);
            this.rightArm.yaw = 0.0F;
            break;
         case 2:
            this.positionBlockingArm(this.rightArm, true);
            break;
         case 3:
            this.rightArm.yaw = -0.1F + this.head.yaw;
            this.leftArm.yaw = 0.1F + this.head.yaw + 0.4F;
            this.rightArm.pitch = (-(float)Math.PI / 2F) + this.head.pitch;
            this.leftArm.pitch = (-(float)Math.PI / 2F) + this.head.pitch;
            break;
         case 4:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - (float)Math.PI;
            this.rightArm.yaw = 0.0F;
            break;
         case 5:
            CrossbowPosing.charge(this.rightArm, this.leftArm, entity, true);
            break;
         case 6:
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
            break;
         case 7:
            this.rightArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622F - (entity.isInSneakingPose() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.rightArm.yaw = this.head.yaw - 0.2617994F;
            break;
         case 8:
            this.rightArm.pitch = MathHelper.clamp(this.head.pitch, -1.2F, 1.2F) - 1.4835298F;
            this.rightArm.yaw = this.head.yaw - ((float)Math.PI / 6F);
            break;
         case 9:
            this.rightArm.pitch = this.rightArm.pitch * 0.5F - ((float)Math.PI / 5F);
            this.rightArm.yaw = 0.0F;
      }

   }

   private void positionLeftArm(LivingEntity entity) {
      switch (this.leftArmPose.ordinal()) {
         case 0:
            this.leftArm.yaw = 0.0F;
            break;
         case 1:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - ((float)Math.PI / 10F);
            this.leftArm.yaw = 0.0F;
            break;
         case 2:
            this.positionBlockingArm(this.leftArm, false);
            break;
         case 3:
            this.rightArm.yaw = -0.1F + this.head.yaw - 0.4F;
            this.leftArm.yaw = 0.1F + this.head.yaw;
            this.rightArm.pitch = (-(float)Math.PI / 2F) + this.head.pitch;
            this.leftArm.pitch = (-(float)Math.PI / 2F) + this.head.pitch;
            break;
         case 4:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - (float)Math.PI;
            this.leftArm.yaw = 0.0F;
            break;
         case 5:
            CrossbowPosing.charge(this.rightArm, this.leftArm, entity, false);
            break;
         case 6:
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, false);
            break;
         case 7:
            this.leftArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622F - (entity.isInSneakingPose() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.leftArm.yaw = this.head.yaw + 0.2617994F;
            break;
         case 8:
            this.leftArm.pitch = MathHelper.clamp(this.head.pitch, -1.2F, 1.2F) - 1.4835298F;
            this.leftArm.yaw = this.head.yaw + ((float)Math.PI / 6F);
            break;
         case 9:
            this.leftArm.pitch = this.leftArm.pitch * 0.5F - ((float)Math.PI / 5F);
            this.leftArm.yaw = 0.0F;
      }

   }

   private void positionBlockingArm(ModelPart arm, boolean rightArm) {
      arm.pitch = arm.pitch * 0.5F - 0.9424779F + MathHelper.clamp(this.head.pitch, -1.3962634F, 0.43633232F);
      arm.yaw = (rightArm ? -30.0F : 30.0F) * ((float)Math.PI / 180F) + MathHelper.clamp(this.head.yaw, (-(float)Math.PI / 6F), ((float)Math.PI / 6F));
   }

   protected void animateArms(LivingEntity entity, float animationProgress) {
      if (!(this.handSwingProgress <= 0.0F)) {
         Arm arm = this.getPreferredArm(entity);
         ModelPart modelPart = this.getArm(arm);
         float f = this.handSwingProgress;
         this.body.yaw = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
         if (arm == Arm.LEFT) {
            ModelPart var10000 = this.body;
            var10000.yaw *= -1.0F;
         }

         this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 5.0F;
         this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 5.0F;
         this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 5.0F;
         this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 5.0F;
         ModelPart var12 = this.rightArm;
         var12.yaw += this.body.yaw;
         var12 = this.leftArm;
         var12.yaw += this.body.yaw;
         var12 = this.leftArm;
         var12.pitch += this.body.yaw;
         f = 1.0F - this.handSwingProgress;
         f *= f;
         f *= f;
         f = 1.0F - f;
         float g = MathHelper.sin(f * (float)Math.PI);
         float h = MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -(this.head.pitch - 0.7F) * 0.75F;
         modelPart.pitch -= g * 1.2F + h;
         modelPart.yaw += this.body.yaw * 2.0F;
         modelPart.roll += MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -0.4F;
      }
   }

   protected float lerpAngle(float angleOne, float angleTwo, float magnitude) {
      float f = (magnitude - angleTwo) % ((float)Math.PI * 2F);
      if (f < -(float)Math.PI) {
         f += ((float)Math.PI * 2F);
      }

      if (f >= (float)Math.PI) {
         f -= ((float)Math.PI * 2F);
      }

      return angleTwo + angleOne * f;
   }

   private float method_2807(float f) {
      return -65.0F * f + f * f;
   }

   public void copyBipedStateTo(BipedEntityModel model) {
      super.copyStateTo(model);
      model.leftArmPose = this.leftArmPose;
      model.rightArmPose = this.rightArmPose;
      model.sneaking = this.sneaking;
      model.head.copyTransform(this.head);
      model.hat.copyTransform(this.hat);
      model.body.copyTransform(this.body);
      model.rightArm.copyTransform(this.rightArm);
      model.leftArm.copyTransform(this.leftArm);
      model.rightLeg.copyTransform(this.rightLeg);
      model.leftLeg.copyTransform(this.leftLeg);
   }

   public void setVisible(boolean visible) {
      this.head.visible = visible;
      this.hat.visible = visible;
      this.body.visible = visible;
      this.rightArm.visible = visible;
      this.leftArm.visible = visible;
      this.rightLeg.visible = visible;
      this.leftLeg.visible = visible;
   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      this.getArm(arm).rotate(matrices);
   }

   protected ModelPart getArm(Arm arm) {
      return arm == Arm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   private Arm getPreferredArm(LivingEntity entity) {
      Arm arm = entity.getMainArm();
      return entity.preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
   }

   @Environment(EnvType.CLIENT)
   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true),
      SPYGLASS(false),
      TOOT_HORN(false),
      BRUSH(false);

      private final boolean twoHanded;

      private ArmPose(final boolean twoHanded) {
         this.twoHanded = twoHanded;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }

      // $FF: synthetic method
      private static ArmPose[] method_36892() {
         return new ArmPose[]{EMPTY, ITEM, BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH};
      }
   }
}
