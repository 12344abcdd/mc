package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class CrackParticle extends SpriteBillboardParticle {
   private final float sampleU;
   private final float sampleV;

   CrackParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, ItemStack stack) {
      this(world, x, y, z, stack);
      this.velocityX *= (double)0.1F;
      this.velocityY *= (double)0.1F;
      this.velocityZ *= (double)0.1F;
      this.velocityX += velocityX;
      this.velocityY += velocityY;
      this.velocityZ += velocityZ;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.TERRAIN_SHEET;
   }

   protected CrackParticle(ClientWorld world, double x, double y, double z, ItemStack stack) {
      super(world, x, y, z, (double)0.0F, (double)0.0F, (double)0.0F);
      this.setSprite(MinecraftClient.getInstance().getItemRenderer().getModel(stack, world, (LivingEntity)null, 0).getParticleSprite());
      this.gravityStrength = 1.0F;
      this.scale /= 2.0F;
      this.sampleU = this.random.nextFloat() * 3.0F;
      this.sampleV = this.random.nextFloat() * 3.0F;
   }

   protected float getMinU() {
      return this.sprite.getFrameU((this.sampleU + 1.0F) / 4.0F);
   }

   protected float getMaxU() {
      return this.sprite.getFrameU(this.sampleU / 4.0F);
   }

   protected float getMinV() {
      return this.sprite.getFrameV(this.sampleV / 4.0F);
   }

   protected float getMaxV() {
      return this.sprite.getFrameV((this.sampleV + 1.0F) / 4.0F);
   }

   @Environment(EnvType.CLIENT)
   public static class CobwebFactory implements ParticleFactory {
      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(clientWorld, d, e, f, new ItemStack(Items.COBWEB));
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ItemFactory implements ParticleFactory {
      public Particle createParticle(ItemStackParticleEffect itemStackParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(clientWorld, d, e, f, g, h, i, itemStackParticleEffect.getItemStack());
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((ItemStackParticleEffect)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SlimeballFactory implements ParticleFactory {
      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(clientWorld, d, e, f, new ItemStack(Items.SLIME_BALL));
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class SnowballFactory implements ParticleFactory {
      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new CrackParticle(clientWorld, d, e, f, new ItemStack(Items.SNOWBALL));
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
