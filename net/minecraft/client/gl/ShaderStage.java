package net.minecraft.client.gl;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class ShaderStage {
   private static final int MAX_INFO_LOG_LENGTH = 32768;
   private final Type type;
   private final String name;
   private int glRef;

   protected ShaderStage(Type type, int glRef, String name) {
      this.type = type;
      this.glRef = glRef;
      this.name = name;
   }

   public void attachTo(ShaderProgramSetupView program) {
      RenderSystem.assertOnRenderThread();
      GlStateManager.glAttachShader(program.getGlRef(), this.getGlRef());
   }

   public void release() {
      if (this.glRef != -1) {
         RenderSystem.assertOnRenderThread();
         GlStateManager.glDeleteShader(this.glRef);
         this.glRef = -1;
         this.type.getLoadedShaders().remove(this.name);
      }
   }

   public String getName() {
      return this.name;
   }

   public static ShaderStage createFromResource(Type type, String name, InputStream stream, String domain, GlImportProcessor loader) throws IOException {
      RenderSystem.assertOnRenderThread();
      int i = load(type, name, stream, domain, loader);
      ShaderStage shaderStage = new ShaderStage(type, i, name);
      type.getLoadedShaders().put(name, shaderStage);
      return shaderStage;
   }

   protected static int load(Type type, String name, InputStream stream, String domain, GlImportProcessor loader) throws IOException {
      String string = IOUtils.toString(stream, StandardCharsets.UTF_8);
      if (string == null) {
         throw new IOException("Could not load program " + type.getName());
      } else {
         int i = GlStateManager.glCreateShader(type.getGlType());
         GlStateManager.glShaderSource(i, loader.readSource(string));
         GlStateManager.glCompileShader(i);
         if (GlStateManager.glGetShaderi(i, 35713) == 0) {
            String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
            throw new IOException("Couldn't compile " + type.getName() + " program (" + domain + ", " + name + ") : " + string2);
         } else {
            return i;
         }
      }
   }

   protected int getGlRef() {
      return this.glRef;
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      VERTEX("vertex", ".vsh", 35633),
      FRAGMENT("fragment", ".fsh", 35632);

      private final String name;
      private final String fileExtension;
      private final int glType;
      private final Map loadedShaders = Maps.newHashMap();

      private Type(final String name, final String extension, final int glType) {
         this.name = name;
         this.fileExtension = extension;
         this.glType = glType;
      }

      public String getName() {
         return this.name;
      }

      public String getFileExtension() {
         return this.fileExtension;
      }

      int getGlType() {
         return this.glType;
      }

      public Map getLoadedShaders() {
         return this.loadedShaders;
      }

      // $FF: synthetic method
      private static Type[] method_36815() {
         return new Type[]{VERTEX, FRAGMENT};
      }
   }
}
