package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class BubbleColumnUpParticle extends SpriteBillboardParticle {
   BubbleColumnUpParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
      super(clientWorld, d, e, f);
      this.gravityStrength = -0.125F;
      this.velocityMultiplier = 0.85F;
      this.setBoundingBoxSpacing(0.02F, 0.02F);
      this.scale *= this.random.nextFloat() * 0.6F + 0.2F;
      this.velocityX = g * (double)0.2F + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.02F;
      this.velocityY = h * (double)0.2F + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.02F;
      this.velocityZ = i * (double)0.2F + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.02F;
      this.maxAge = (int)((double)40.0F / (Math.random() * 0.8 + 0.2));
   }

   public void tick() {
      super.tick();
      if (!this.dead && !this.world.getFluidState(BlockPos.ofFloored(this.x, this.y, this.z)).isIn(FluidTags.WATER)) {
         this.markDead();
      }

   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(clientWorld, d, e, f, g, h, i);
         bubbleColumnUpParticle.setSprite(this.spriteProvider);
         return bubbleColumnUpParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
