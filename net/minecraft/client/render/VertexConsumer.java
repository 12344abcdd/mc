package net.minecraft.client.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.ColorHelper.Argb;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public interface VertexConsumer {
   VertexConsumer vertex(float x, float y, float z);

   VertexConsumer color(int red, int green, int blue, int alpha);

   VertexConsumer texture(float u, float v);

   VertexConsumer overlay(int u, int v);

   VertexConsumer light(int u, int v);

   VertexConsumer normal(float x, float y, float z);

   default void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
      this.vertex(x, y, z);
      this.color(color);
      this.texture(u, v);
      this.overlay(overlay);
      this.light(light);
      this.normal(normalX, normalY, normalZ);
   }

   default VertexConsumer color(float red, float green, float blue, float alpha) {
      return this.color((int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F), (int)(alpha * 255.0F));
   }

   default VertexConsumer color(int argb) {
      return this.color(Argb.getRed(argb), Argb.getGreen(argb), Argb.getBlue(argb), Argb.getAlpha(argb));
   }

   default VertexConsumer colorRgb(int rgb) {
      return this.color(Argb.withAlpha(rgb, -1));
   }

   default VertexConsumer light(int uv) {
      return this.light(uv & '\uffff', uv >> 16 & '\uffff');
   }

   default VertexConsumer overlay(int uv) {
      return this.overlay(uv & '\uffff', uv >> 16 & '\uffff');
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float f, int i, int j) {
      this.quad(matrixEntry, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, f, new int[]{i, i, i, i}, j, false);
   }

   default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float f, int[] is, int i, boolean bl) {
      int[] js = quad.getVertexData();
      Vec3i vec3i = quad.getFace().getVector();
      Matrix4f matrix4f = matrixEntry.getPositionMatrix();
      Vector3f vector3f = matrixEntry.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
      int j = 8;
      int k = js.length / 8;
      int l = (int)(f * 255.0F);
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
         IntBuffer intBuffer = byteBuffer.asIntBuffer();

         for(int m = 0; m < k; ++m) {
            intBuffer.clear();
            intBuffer.put(js, m * 8, 8);
            float g = byteBuffer.getFloat(0);
            float h = byteBuffer.getFloat(4);
            float n = byteBuffer.getFloat(8);
            float r;
            float s;
            float t;
            if (bl) {
               float o = (float)(byteBuffer.get(12) & 255);
               float p = (float)(byteBuffer.get(13) & 255);
               float q = (float)(byteBuffer.get(14) & 255);
               r = o * brightnesses[m] * red;
               s = p * brightnesses[m] * green;
               t = q * brightnesses[m] * blue;
            } else {
               r = brightnesses[m] * red * 255.0F;
               s = brightnesses[m] * green * 255.0F;
               t = brightnesses[m] * blue * 255.0F;
            }

            int u = Argb.getArgb(l, (int)r, (int)s, (int)t);
            int v = is[m];
            float q = byteBuffer.getFloat(16);
            float w = byteBuffer.getFloat(20);
            Vector3f vector3f2 = matrix4f.transformPosition(g, h, n, new Vector3f());
            this.vertex(vector3f2.x(), vector3f2.y(), vector3f2.z(), u, q, w, i, v, vector3f.x(), vector3f.y(), vector3f.z());
         }
      } catch (Throwable var34) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var33) {
               var34.addSuppressed(var33);
            }
         }

         throw var34;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

   }

   default VertexConsumer vertex(Vector3f vec) {
      return this.vertex(vec.x(), vec.y(), vec.z());
   }

   default VertexConsumer vertex(MatrixStack.Entry matrix, Vector3f vec) {
      return this.vertex(matrix, vec.x(), vec.y(), vec.z());
   }

   default VertexConsumer vertex(MatrixStack.Entry matrix, float x, float y, float z) {
      return this.vertex(matrix.getPositionMatrix(), x, y, z);
   }

   default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
      Vector3f vector3f = matrix.transformPosition(x, y, z, new Vector3f());
      return this.vertex(vector3f.x(), vector3f.y(), vector3f.z());
   }

   default VertexConsumer normal(MatrixStack.Entry matrix, float x, float y, float z) {
      Vector3f vector3f = matrix.transformNormal(x, y, z, new Vector3f());
      return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
   }
}
