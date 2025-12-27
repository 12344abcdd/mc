package net.minecraft.client.render.entity.animation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class AnimationHelper {
   public static void animate(SinglePartEntityModel model, Animation animation, long runningTime, float scale, Vector3f tempVec) {
      float f = getRunningSeconds(animation, runningTime);

      for(Map.Entry entry : animation.boneAnimations().entrySet()) {
         Optional<ModelPart> optional = model.getChild((String)entry.getKey());
         List<Transformation> list = (List)entry.getValue();
         optional.ifPresent((part) -> list.forEach((transformation) -> {
               Keyframe[] keyframes = transformation.keyframes();
               int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, (index) -> f <= keyframes[index].timestamp()) - 1);
               int j = Math.min(keyframes.length - 1, i + 1);
               Keyframe keyframe = keyframes[i];
               Keyframe keyframe2 = keyframes[j];
               float h = f - keyframe.timestamp();
               float k;
               if (j != i) {
                  k = MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
               } else {
                  k = 0.0F;
               }

               keyframe2.interpolation().apply(tempVec, k, keyframes, i, j, scale);
               transformation.target().apply(part, tempVec);
            }));
      }

   }

   private static float getRunningSeconds(Animation animation, long runningTime) {
      float f = (float)runningTime / 1000.0F;
      return animation.looping() ? f % animation.lengthInSeconds() : f;
   }

   public static Vector3f createTranslationalVector(float x, float y, float z) {
      return new Vector3f(x, -y, z);
   }

   public static Vector3f createRotationalVector(float x, float y, float z) {
      return new Vector3f(x * ((float)Math.PI / 180F), y * ((float)Math.PI / 180F), z * ((float)Math.PI / 180F));
   }

   public static Vector3f createScalingVector(double x, double y, double z) {
      return new Vector3f((float)(x - (double)1.0F), (float)(y - (double)1.0F), (float)(z - (double)1.0F));
   }
}
