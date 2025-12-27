package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class ExplosionEmitterParticle extends NoRenderParticle {
   ExplosionEmitterParticle(ClientWorld clientWorld, double d, double e, double f) {
      super(clientWorld, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F);
      this.maxAge = 8;
   }

   public void tick() {
      for(int i = 0; i < 6; ++i) {
         double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double e = this.y + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         double f = this.z + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
         this.world.addParticle(ParticleTypes.EXPLOSION, d, e, f, (double)((float)this.age / (float)this.maxAge), (double)0.0F, (double)0.0F);
      }

      ++this.age;
      if (this.age == this.maxAge) {
         this.markDead();
      }

   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory {
      public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         return new ExplosionEmitterParticle(clientWorld, d, e, f);
      }

      // $FF: synthetic method
      public Particle createParticle(final ParticleEffect particleEffect, final ClientWorld clientWorld, final double d, final double e, final double f, final double g, final double h, final double i) {
         return this.createParticle((SimpleParticleType)particleEffect, clientWorld, d, e, f, g, h, i);
      }
   }
}
