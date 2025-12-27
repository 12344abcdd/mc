package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.MooshroomMushroomFeatureRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MooshroomEntity.Type;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class MooshroomEntityRenderer extends MobEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (map) -> {
      map.put(Type.BROWN, Identifier.ofVanilla("textures/entity/cow/brown_mooshroom.png"));
      map.put(Type.RED, Identifier.ofVanilla("textures/entity/cow/red_mooshroom.png"));
   });

   public MooshroomEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new CowEntityModel(context.getPart(EntityModelLayers.MOOSHROOM)), 0.7F);
      this.addFeature(new MooshroomMushroomFeatureRenderer(this, context.getBlockRenderManager()));
   }

   public Identifier getTexture(MooshroomEntity mooshroomEntity) {
      return (Identifier)TEXTURES.get(mooshroomEntity.getVariant());
   }
}
