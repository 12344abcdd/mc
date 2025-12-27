package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum FogShape {
   SPHERE(0),
   CYLINDER(1);

   private final int id;

   private FogShape(final int id) {
      this.id = id;
   }

   public int getId() {
      return this.id;
   }

   // $FF: synthetic method
   private static FogShape[] method_40037() {
      return new FogShape[]{SPHERE, CYLINDER};
   }
}
