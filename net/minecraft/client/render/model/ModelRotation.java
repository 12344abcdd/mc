package net.minecraft.client.render.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public enum ModelRotation implements ModelBakeSettings {
   X0_Y0(0, 0),
   X0_Y90(0, 90),
   X0_Y180(0, 180),
   X0_Y270(0, 270),
   X90_Y0(90, 0),
   X90_Y90(90, 90),
   X90_Y180(90, 180),
   X90_Y270(90, 270),
   X180_Y0(180, 0),
   X180_Y90(180, 90),
   X180_Y180(180, 180),
   X180_Y270(180, 270),
   X270_Y0(270, 0),
   X270_Y90(270, 90),
   X270_Y180(270, 180),
   X270_Y270(270, 270);

   private static final int MAX_ROTATION = 360;
   private static final Map BY_INDEX = (Map)Arrays.stream(values()).collect(Collectors.toMap((rotation) -> rotation.index, (rotation) -> rotation));
   private final AffineTransformation rotation;
   private final DirectionTransformation directionTransformation;
   private final int index;

   private static int getIndex(int x, int y) {
      return x * 360 + y;
   }

   private ModelRotation(final int x, final int y) {
      this.index = getIndex(x, y);
      Quaternionf quaternionf = (new Quaternionf()).rotateYXZ((float)(-y) * ((float)Math.PI / 180F), (float)(-x) * ((float)Math.PI / 180F), 0.0F);
      DirectionTransformation directionTransformation = DirectionTransformation.IDENTITY;

      for(int j = 0; j < y; j += 90) {
         directionTransformation = directionTransformation.prepend(DirectionTransformation.ROT_90_Y_NEG);
      }

      for(int j = 0; j < x; j += 90) {
         directionTransformation = directionTransformation.prepend(DirectionTransformation.ROT_90_X_NEG);
      }

      this.rotation = new AffineTransformation((Vector3f)null, quaternionf, (Vector3f)null, (Quaternionf)null);
      this.directionTransformation = directionTransformation;
   }

   public AffineTransformation getRotation() {
      return this.rotation;
   }

   public static ModelRotation get(int x, int y) {
      return (ModelRotation)BY_INDEX.get(getIndex(MathHelper.floorMod(x, 360), MathHelper.floorMod(y, 360)));
   }

   public DirectionTransformation getDirectionTransformation() {
      return this.directionTransformation;
   }

   // $FF: synthetic method
   private static ModelRotation[] method_36925() {
      return new ModelRotation[]{X0_Y0, X0_Y90, X0_Y180, X0_Y270, X90_Y0, X90_Y90, X90_Y180, X90_Y270, X180_Y0, X180_Y90, X180_Y180, X180_Y270, X270_Y0, X270_Y90, X270_Y180, X270_Y270};
   }
}
