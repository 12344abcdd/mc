package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

@Environment(EnvType.CLIENT)
public class RainSplashParticle extends SpriteBillboardParticle {
   protected RainSplashParticle(ClientWorld clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F);
      this.velocityX *= (double)0.3F;
      this.velocityY = Math.random() * (double)0.2F + (double)0.1F;
      this.velocityZ *= (double)0.3F;
      this.setBoundingBoxSpacing(0.01F, 0.01F);
      this.gravityStrength = 0.06F;
      this.maxAge = (int)((double)8.0F / (Math.random() * 0.8 + 0.2));
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.maxAge-- <= 0) {
         this.markDead();
      } else {
         this.velocityY -= (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityX *= (double)0.98F;
         this.velocityY *= (double)0.98F;
         this.velocityZ *= (double)0.98F;
         if (this.onGround) {
            if (Math.random() < (double)0.5F) {
               this.markDead();
            }

            this.velocityX *= (double)0.7F;
            this.velocityZ *= (double)0.7F;
         }

         BlockPos blockPos = BlockPos.ofFloored(this.x, this.y, this.z);
         double d = Math.max(this.world.getBlockState(blockPos).getCollisionShape(this.world, blockPos).getEndingCoord(Axis.Y, this.x - (double)blockPos.getX(), this.z - (double)blockPos.getZ()), (double)this.world.getFluidState(blockPos).getHeight(this.world, blockPos));
         if (d > (double)0.0F && this.y < (double)blockPos.getY() + d) {
            this.markDead();
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         RainSplashParticle rainSplashParticle = new RainSplashParticle(clientWorld, d, e, f);
         rainSplashParticle.setSprite(this.spriteProvider);
         return rainSplashParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
