package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class FishingParticle extends SpriteBillboardParticle {
   private final SpriteProvider spriteProvider;

   FishingParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.spriteProvider = spriteProvider;
      this.velocityX *= (double)0.3F;
      this.velocityY = Math.random() * (double)0.2F + (double)0.1F;
      this.velocityZ *= (double)0.3F;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.maxAge = (int)((double)8.0F / (Math.random() * 0.8 + 0.2));
      this.setSpriteForAge(spriteProvider);
      this.gravityStrength = 0.0F;
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      int i = 60 - this.maxAge;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= (double)0.98F;
         this.velocityY *= (double)0.98F;
         this.velocityZ *= (double)0.98F;
         float f = (float)i * 0.001F;
         this.setBoundingBoxSpacing(f, f);
         this.setSprite(this.spriteProvider.getSprite(i % 4, 4));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new FishingParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
