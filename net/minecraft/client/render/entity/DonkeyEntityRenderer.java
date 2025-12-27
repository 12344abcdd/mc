package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.DonkeyEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DonkeyEntityRenderer extends AbstractHorseEntityRenderer {
   private static final Map TEXTURES;

   public DonkeyEntityRenderer(EntityRendererFactory.Context ctx, float scale, EntityModelLayer layer) {
      super(ctx, new DonkeyEntityModel(ctx.getPart(layer)), scale);
   }

   public Identifier getTexture(AbstractDonkeyEntity abstractDonkeyEntity) {
      return (Identifier)TEXTURES.get(abstractDonkeyEntity.getType());
   }

   static {
      TEXTURES = Maps.newHashMap(ImmutableMap.of(EntityType.DONKEY, Identifier.ofVanilla("textures/entity/horse/donkey.png"), EntityType.MULE, Identifier.ofVanilla("textures/entity/horse/mule.png")));
   }
}
