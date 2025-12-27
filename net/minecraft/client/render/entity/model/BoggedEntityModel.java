package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.mob.BoggedEntity;
import net.minecraft.entity.mob.MobEntity;

@Environment(EnvType.CLIENT)
public class BoggedEntityModel extends SkeletonEntityModel {
   private final ModelPart mushrooms;

   public BoggedEntityModel(ModelPart modelPart) {
      super(modelPart);
      this.mushrooms = modelPart.getChild("head").getChild("mushrooms");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0.0F);
      ModelPartData modelPartData = modelData.getRoot();
      SkeletonEntityModel.addLimbs(modelPartData);
      ModelPartData modelPartData2 = modelPartData.getChild("head").addChild("mushrooms", ModelPartBuilder.create(), ModelTransform.NONE);
      modelPartData2.addChild("red_mushroom_1", ModelPartBuilder.create().uv(50, 16).cuboid(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(3.0F, -8.0F, 3.0F, 0.0F, ((float)Math.PI / 4F), 0.0F));
      modelPartData2.addChild("red_mushroom_2", ModelPartBuilder.create().uv(50, 16).cuboid(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(3.0F, -8.0F, 3.0F, 0.0F, 2.3561945F, 0.0F));
      modelPartData2.addChild("brown_mushroom_1", ModelPartBuilder.create().uv(50, 22).cuboid(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(-3.0F, -8.0F, -3.0F, 0.0F, ((float)Math.PI / 4F), 0.0F));
      modelPartData2.addChild("brown_mushroom_2", ModelPartBuilder.create().uv(50, 22).cuboid(-3.0F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(-3.0F, -8.0F, -3.0F, 0.0F, 2.3561945F, 0.0F));
      modelPartData2.addChild("brown_mushroom_3", ModelPartBuilder.create().uv(50, 28).cuboid(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(-2.0F, -1.0F, 4.0F, (-(float)Math.PI / 2F), 0.0F, ((float)Math.PI / 4F)));
      modelPartData2.addChild("brown_mushroom_4", ModelPartBuilder.create().uv(50, 28).cuboid(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F), ModelTransform.of(-2.0F, -1.0F, 4.0F, (-(float)Math.PI / 2F), 0.0F, 2.3561945F));
      return TexturedModelData.of(modelData, 64, 32);
   }

   public void animateModel(BoggedEntity boggedEntity, float f, float g, float h) {
      this.mushrooms.visible = !boggedEntity.isSheared();
      super.animateModel((MobEntity)boggedEntity, f, g, h);
   }
}
