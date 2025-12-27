package net.minecraft.client.render.block.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.SkullBlock.Type;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PiglinHeadEntityModel;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SkullBlockEntityRenderer implements BlockEntityRenderer {
   private final Map MODELS;
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put(Type.SKELETON, Identifier.ofVanilla("textures/entity/skeleton/skeleton.png"));
      map.put(Type.WITHER_SKELETON, Identifier.ofVanilla("textures/entity/skeleton/wither_skeleton.png"));
      map.put(Type.ZOMBIE, Identifier.ofVanilla("textures/entity/zombie/zombie.png"));
      map.put(Type.CREEPER, Identifier.ofVanilla("textures/entity/creeper/creeper.png"));
      map.put(Type.DRAGON, Identifier.ofVanilla("textures/entity/enderdragon/dragon.png"));
      map.put(Type.PIGLIN, Identifier.ofVanilla("textures/entity/piglin/piglin.png"));
      map.put(Type.PLAYER, DefaultSkinHelper.getTexture());
   });

   public static Map getModels(EntityModelLoader modelLoader) {
      ImmutableMap.Builder<SkullBlock.SkullType, SkullBlockEntityModel> builder = ImmutableMap.builder();
      builder.put(Type.SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.SKELETON_SKULL)));
      builder.put(Type.WITHER_SKELETON, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.WITHER_SKELETON_SKULL)));
      builder.put(Type.PLAYER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.PLAYER_HEAD)));
      builder.put(Type.ZOMBIE, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.ZOMBIE_HEAD)));
      builder.put(Type.CREEPER, new SkullEntityModel(modelLoader.getModelPart(EntityModelLayers.CREEPER_HEAD)));
      builder.put(Type.DRAGON, new DragonHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.DRAGON_SKULL)));
      builder.put(Type.PIGLIN, new PiglinHeadEntityModel(modelLoader.getModelPart(EntityModelLayers.PIGLIN_HEAD)));
      return builder.build();
   }

   public SkullBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.MODELS = getModels(ctx.getLayerRenderDispatcher());
   }

   public void render(SkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      float g = skullBlockEntity.getPoweredTicks(f);
      BlockState blockState = skullBlockEntity.getCachedState();
      boolean bl = blockState.getBlock() instanceof WallSkullBlock;
      Direction direction = bl ? (Direction)blockState.get(WallSkullBlock.FACING) : null;
      int k = bl ? RotationPropertyHelper.fromDirection(direction.getOpposite()) : (Integer)blockState.get(SkullBlock.ROTATION);
      float h = RotationPropertyHelper.toDegrees(k);
      SkullBlock.SkullType skullType = ((AbstractSkullBlock)blockState.getBlock()).getSkullType();
      SkullBlockEntityModel skullBlockEntityModel = (SkullBlockEntityModel)this.MODELS.get(skullType);
      RenderLayer renderLayer = getRenderLayer(skullType, skullBlockEntity.getOwner());
      renderSkull(direction, h, g, matrixStack, vertexConsumerProvider, i, skullBlockEntityModel, renderLayer);
   }

   public static void renderSkull(@Nullable Direction direction, float yaw, float animationProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model, RenderLayer renderLayer) {
      matrices.push();
      if (direction == null) {
         matrices.translate(0.5F, 0.0F, 0.5F);
      } else {
         float f = 0.25F;
         matrices.translate(0.5F - (float)direction.getOffsetX() * 0.25F, 0.25F, 0.5F - (float)direction.getOffsetZ() * 0.25F);
      }

      matrices.scale(-1.0F, -1.0F, 1.0F);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
      model.setHeadRotation(animationProgress, yaw, 0.0F);
      model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
      matrices.pop();
   }

   public static RenderLayer getRenderLayer(SkullBlock.SkullType type, @Nullable ProfileComponent profile) {
      Identifier identifier = (Identifier)TEXTURES.get(type);
      if (type == Type.PLAYER && profile != null) {
         PlayerSkinProvider playerSkinProvider = MinecraftClient.getInstance().getSkinProvider();
         return RenderLayer.getEntityTranslucent(playerSkinProvider.getSkinTextures(profile.gameProfile()).texture());
      } else {
         return RenderLayer.getEntityCutoutNoCullZOffset(identifier);
      }
   }
}
