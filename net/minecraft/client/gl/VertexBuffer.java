package net.minecraft.client.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
   private final Usage usage;
   private int vertexBufferId;
   private int indexBufferId;
   private int vertexArrayId;
   @Nullable
   private VertexFormat vertexFormat;
   @Nullable
   private RenderSystem.ShapeIndexBuffer sharedSequentialIndexBuffer;
   private VertexFormat.IndexType indexType;
   private int indexCount;
   private VertexFormat.DrawMode drawMode;

   public VertexBuffer(Usage usage) {
      this.usage = usage;
      RenderSystem.assertOnRenderThread();
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.vertexArrayId = GlStateManager._glGenVertexArrays();
   }

   public void upload(BuiltBuffer data) {
      BuiltBuffer var2 = data;

      label40: {
         try {
            if (this.isClosed()) {
               break label40;
            }

            RenderSystem.assertOnRenderThread();
            BuiltBuffer.DrawParameters drawParameters = data.getDrawParameters();
            this.vertexFormat = this.uploadVertexBuffer(drawParameters, data.getBuffer());
            this.sharedSequentialIndexBuffer = this.uploadIndexBuffer(drawParameters, data.getSortedBuffer());
            this.indexCount = drawParameters.indexCount();
            this.indexType = drawParameters.indexType();
            this.drawMode = drawParameters.mode();
         } catch (Throwable var6) {
            if (data != null) {
               try {
                  var2.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (data != null) {
            data.close();
         }

         return;
      }

      if (data != null) {
         data.close();
      }

   }

   public void uploadIndexBuffer(BufferAllocator.CloseableBuffer indexBuffer) {
      BufferAllocator.CloseableBuffer var2 = indexBuffer;

      label40: {
         try {
            if (this.isClosed()) {
               break label40;
            }

            RenderSystem.assertOnRenderThread();
            GlStateManager._glBindBuffer(34963, this.indexBufferId);
            RenderSystem.glBufferData(34963, indexBuffer.getBuffer(), this.usage.id);
            this.sharedSequentialIndexBuffer = null;
         } catch (Throwable var6) {
            if (indexBuffer != null) {
               try {
                  var2.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (indexBuffer != null) {
            indexBuffer.close();
         }

         return;
      }

      if (indexBuffer != null) {
         indexBuffer.close();
      }

   }

   private VertexFormat uploadVertexBuffer(BuiltBuffer.DrawParameters parameters, @Nullable ByteBuffer vertexBuffer) {
      boolean bl = false;
      if (!parameters.format().equals(this.vertexFormat)) {
         if (this.vertexFormat != null) {
            this.vertexFormat.clearState();
         }

         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         parameters.format().setupState();
         bl = true;
      }

      if (vertexBuffer != null) {
         if (!bl) {
            GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         }

         RenderSystem.glBufferData(34962, vertexBuffer, this.usage.id);
      }

      return parameters.format();
   }

   @Nullable
   private RenderSystem.ShapeIndexBuffer uploadIndexBuffer(BuiltBuffer.DrawParameters parameters, @Nullable ByteBuffer indexBuffer) {
      if (indexBuffer != null) {
         GlStateManager._glBindBuffer(34963, this.indexBufferId);
         RenderSystem.glBufferData(34963, indexBuffer, this.usage.id);
         return null;
      } else {
         RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(parameters.mode());
         if (shapeIndexBuffer != this.sharedSequentialIndexBuffer || !shapeIndexBuffer.isLargeEnough(parameters.indexCount())) {
            shapeIndexBuffer.bindAndGrow(parameters.indexCount());
         }

         return shapeIndexBuffer;
      }
   }

   public void bind() {
      BufferRenderer.resetCurrentVertexBuffer();
      GlStateManager._glBindVertexArray(this.vertexArrayId);
   }

   public static void unbind() {
      BufferRenderer.resetCurrentVertexBuffer();
      GlStateManager._glBindVertexArray(0);
   }

   public void draw() {
      RenderSystem.drawElements(this.drawMode.glMode, this.indexCount, this.getIndexType().glType);
   }

   private VertexFormat.IndexType getIndexType() {
      RenderSystem.ShapeIndexBuffer shapeIndexBuffer = this.sharedSequentialIndexBuffer;
      return shapeIndexBuffer != null ? shapeIndexBuffer.getIndexType() : this.indexType;
   }

   public void draw(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram program) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> this.drawInternal(new Matrix4f(viewMatrix), new Matrix4f(projectionMatrix), program));
      } else {
         this.drawInternal(viewMatrix, projectionMatrix, program);
      }

   }

   private void drawInternal(Matrix4f viewMatrix, Matrix4f projectionMatrix, ShaderProgram shader) {
      shader.initializeUniforms(this.drawMode, viewMatrix, projectionMatrix, MinecraftClient.getInstance().getWindow());
      shader.bind();
      this.draw();
      shader.unbind();
   }

   public void close() {
      if (this.vertexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.vertexBufferId);
         this.vertexBufferId = -1;
      }

      if (this.indexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.indexBufferId);
         this.indexBufferId = -1;
      }

      if (this.vertexArrayId >= 0) {
         RenderSystem.glDeleteVertexArrays(this.vertexArrayId);
         this.vertexArrayId = -1;
      }

   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public boolean isClosed() {
      return this.vertexArrayId == -1;
   }

   @Environment(EnvType.CLIENT)
   public static enum Usage {
      STATIC(35044),
      DYNAMIC(35048);

      final int id;

      private Usage(final int id) {
         this.id = id;
      }

      // $FF: synthetic method
      private static Usage[] method_51735() {
         return new Usage[]{STATIC, DYNAMIC};
      }
   }
}
