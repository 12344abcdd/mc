package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class GlowParticle extends SpriteBillboardParticle {
   static final Random RANDOM = Random.create();
   private final SpriteProvider spriteProvider;

   GlowParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
      super(world, x, y, z, velocityX, velocityY, velocityZ);
      this.velocityMultiplier = 0.96F;
      this.ascending = true;
      this.spriteProvider = spriteProvider;
      this.scale *= 0.75F;
      this.collidesWithWorld = false;
      this.setSpriteForAge(spriteProvider);
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getBrightness(float tint) {
      float f = ((float)this.age + tint) / (float)this.maxAge;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      int i = super.getBrightness(tint);
      int j = i & 255;
      int k = i >> 16 & 255;
      j += (int)(f * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      super.tick();
      this.setSpriteForAge(this.spriteProvider);
   }

   @Environment(EnvType.CLIENT)
   public static class ElectricSparkFactory implements ParticleFactory {
      private final double velocityMultiplier = (double)0.25F;
      private final SpriteProvider spriteProvider;

      public ElectricSparkFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F, this.spriteProvider);
         glowParticle.setColor(1.0F, 0.9F, 1.0F);
         glowParticle.setVelocity(g * (double)0.25F, h * (double)0.25F, i * (double)0.25F);
         int j = 2;
         int k = 4;
         glowParticle.setMaxAge(clientWorld.random.nextInt(2) + 2);
         return glowParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class GlowFactory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public GlowFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, (double)0.5F - GlowParticle.RANDOM.nextDouble(), h, (double)0.5F - GlowParticle.RANDOM.nextDouble(), this.spriteProvider);
         if (clientWorld.random.nextBoolean()) {
            glowParticle.setColor(0.6F, 1.0F, 0.8F);
         } else {
            glowParticle.setColor(0.08F, 0.4F, 0.4F);
         }

         glowParticle.velocityY *= (double)0.2F;
         if (g == (double)0.0F && i == (double)0.0F) {
            glowParticle.velocityX *= (double)0.1F;
            glowParticle.velocityZ *= (double)0.1F;
         }

         glowParticle.setMaxAge((int)((double)8.0F / (clientWorld.random.nextDouble() * 0.8 + 0.2)));
         return glowParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ScrapeFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public ScrapeFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F, this.spriteProvider);
         if (clientWorld.random.nextBoolean()) {
            glowParticle.setColor(0.29F, 0.58F, 0.51F);
         } else {
            glowParticle.setColor(0.43F, 0.77F, 0.62F);
         }

         glowParticle.setVelocity(g * 0.01, h * 0.01, i * 0.01);
         int j = 10;
         int k = 40;
         glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
         return glowParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WaxOffFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public WaxOffFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F, this.spriteProvider);
         glowParticle.setColor(1.0F, 0.9F, 1.0F);
         glowParticle.setVelocity(g * 0.01 / (double)2.0F, h * 0.01, i * 0.01 / (double)2.0F);
         int j = 10;
         int k = 40;
         glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
         return glowParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WaxOnFactory implements ParticleFactory {
      private final double velocityMultiplier = 0.01;
      private final SpriteProvider spriteProvider;

      public WaxOnFactory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F, this.spriteProvider);
         glowParticle.setColor(0.91F, 0.55F, 0.08F);
         glowParticle.setVelocity(g * 0.01 / (double)2.0F, h * 0.01, i * 0.01 / (double)2.0F);
         int j = 10;
         int k = 40;
         glowParticle.setMaxAge(clientWorld.random.nextInt(30) + 10);
         return glowParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
