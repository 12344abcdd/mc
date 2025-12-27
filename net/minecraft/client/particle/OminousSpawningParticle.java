package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class OminousSpawningParticle extends SpriteBillboardParticle {
   private final double startX;
   private final double startY;
   private final double startZ;
   private final int fromColor;
   private final int toColor;

   OminousSpawningParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, int fromColor, int toColor) {
      super(world, x, y, z);
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
      this.startX = x;
      this.startY = y;
      this.startZ = z;
      this.prevPosX = x + velocityX;
      this.prevPosY = y + velocityY;
      this.prevPosZ = z + velocityZ;
      this.x = this.prevPosX;
      this.y = this.prevPosY;
      this.z = this.prevPosZ;
      this.scale = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
      this.collidesWithWorld = false;
      this.maxAge = (int)(Math.random() * (double)5.0F) + 25;
      this.fromColor = fromColor;
      this.toColor = toColor;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double dx, double dy, double dz) {
   }

   public int getBrightness(float tint) {
      return 240;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         float f = (float)this.age / (float)this.maxAge;
         float g = 1.0F - f;
         this.x = this.startX + this.velocityX * (double)g;
         this.y = this.startY + this.velocityY * (double)g;
         this.z = this.startZ + this.velocityZ * (double)g;
         int i = Argb.lerp(f, this.fromColor, this.toColor);
         this.setColor((float)Argb.getRed(i) / 255.0F, (float)Argb.getGreen(i) / 255.0F, (float)Argb.getBlue(i) / 255.0F);
         this.setAlpha((float)Argb.getAlpha(i) / 255.0F);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         OminousSpawningParticle ominousSpawningParticle = new OminousSpawningParticle(clientWorld, d, e, f, g, h, i, -12210434, -1);
         ominousSpawningParticle.scale(MathHelper.nextBetween(clientWorld.getRandom(), 3.0F, 5.0F));
         ominousSpawningParticle.setSprite(this.spriteProvider);
         return ominousSpawningParticle;
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
