package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public record SoundListenerTransform(Vec3d position, Vec3d forward, Vec3d up) {
   public static final SoundListenerTransform DEFAULT;

   public Vec3d right() {
      return this.forward.crossProduct(this.up);
   }

   static {
      DEFAULT = new SoundListenerTransform(Vec3d.ZERO, new Vec3d((double)0.0F, (double)0.0F, (double)-1.0F), new Vec3d((double)0.0F, (double)1.0F, (double)0.0F));
   }
}
