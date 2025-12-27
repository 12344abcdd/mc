package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ElderGuardianEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GuardianEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class ElderGuardianAppearanceParticle extends Particle {
   private final Model model;
   private final RenderLayer layer;

   ElderGuardianAppearanceParticle(ClientWorld clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f);
      this.layer = RenderLayer.getEntityTranslucent(ElderGuardianEntityRenderer.TEXTURE);
      this.model = new GuardianEntityModel(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.ELDER_GUARDIAN));
      this.gravityStrength = 0.0F;
      this.maxAge = 30;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.CUSTOM;
   }

   public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
      float f = ((float)this.age + tickDelta) / (float)this.maxAge;
      float g = 0.05F + 0.5F * MathHelper.sin(f * (float)Math.PI);
      int i = Argb.fromFloats(g, 1.0F, 1.0F, 1.0F);
      MatrixStack matrixStack = new MatrixStack();
      matrixStack.multiply(camera.getRotation());
      matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(150.0F * f - 60.0F));
      matrixStack.scale(1.0F, -1.0F, -1.0F);
      matrixStack.translate(0.0F, -1.101F, 1.5F);
      VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
      VertexConsumer vertexConsumer2 = immediate.getBuffer(this.layer);
      this.model.render(matrixStack, vertexConsumer2, 15728880, OverlayTexture.DEFAULT_UV, i);
      immediate.draw();
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new ElderGuardianAppearanceParticle(clientWorld, d, e, f);
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
