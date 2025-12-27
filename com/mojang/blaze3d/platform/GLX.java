package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@Environment(EnvType.CLIENT)
@DeobfuscateClass
public class GLX {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static String cpuInfo;

   public static String getOpenGLVersionString() {
      RenderSystem.assertOnRenderThread();
      if (GLFW.glfwGetCurrentContext() == 0L) {
         return "NO CONTEXT";
      } else {
         String var10000 = GlStateManager._getString(7937);
         return var10000 + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
      }
   }

   public static int _getRefreshRate(Window window) {
      RenderSystem.assertOnRenderThread();
      long l = GLFW.glfwGetWindowMonitor(window.getHandle());
      if (l == 0L) {
         l = GLFW.glfwGetPrimaryMonitor();
      }

      GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
      return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
   }

   public static String _getLWJGLVersion() {
      return Version.getVersion();
   }

   public static LongSupplier _initGlfw() {
      Window.acceptError((code, message) -> {
         throw new IllegalStateException(String.format(Locale.ROOT, "GLFW error before init: [0x%X]%s", code, message));
      });
      List<String> list = Lists.newArrayList();
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((code, pointer) -> {
         String string = pointer == 0L ? "" : MemoryUtil.memUTF8(pointer);
         list.add(String.format(Locale.ROOT, "GLFW error during init: [0x%X]%s", code, string));
      });
      if (!GLFW.glfwInit()) {
         throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
      } else {
         LongSupplier longSupplier = () -> (long)(GLFW.glfwGetTime() * (double)1.0E9F);

         for(String string : list) {
            LOGGER.error("GLFW error collected during initialization: {}", string);
         }

         RenderSystem.setErrorCallback(gLFWErrorCallback);
         return longSupplier;
      }
   }

   public static void _setGlfwErrorCallback(GLFWErrorCallbackI callback) {
      GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(callback);
      if (gLFWErrorCallback != null) {
         gLFWErrorCallback.free();
      }

   }

   public static boolean _shouldClose(Window window) {
      return GLFW.glfwWindowShouldClose(window.getHandle());
   }

   public static void _init(int debugVerbosity, boolean debugSync) {
      try {
         CentralProcessor centralProcessor = (new SystemInfo()).getHardware().getProcessor();
         cpuInfo = String.format(Locale.ROOT, "%dx %s", centralProcessor.getLogicalProcessorCount(), centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
      } catch (Throwable var3) {
      }

      GlDebug.enableDebug(debugVerbosity, debugSync);
   }

   public static String _getCpuInfo() {
      return cpuInfo == null ? "<unknown>" : cpuInfo;
   }

   public static void _renderCrosshair(int size, boolean drawX, boolean drawY, boolean drawZ) {
      if (drawX || drawY || drawZ) {
         RenderSystem.assertOnRenderThread();
         GlStateManager._depthMask(false);
         GlStateManager._disableCull();
         RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
         Tessellator tessellator = RenderSystem.renderThreadTesselator();
         BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
         RenderSystem.lineWidth(4.0F);
         if (drawX) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-16777216).normal(1.0F, 0.0F, 0.0F);
            bufferBuilder.vertex((float)size, 0.0F, 0.0F).color(-16777216).normal(1.0F, 0.0F, 0.0F);
         }

         if (drawY) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-16777216).normal(0.0F, 1.0F, 0.0F);
            bufferBuilder.vertex(0.0F, (float)size, 0.0F).color(-16777216).normal(0.0F, 1.0F, 0.0F);
         }

         if (drawZ) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-16777216).normal(0.0F, 0.0F, 1.0F);
            bufferBuilder.vertex(0.0F, 0.0F, (float)size).color(-16777216).normal(0.0F, 0.0F, 1.0F);
         }

         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         RenderSystem.lineWidth(2.0F);
         bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
         if (drawX) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-65536).normal(1.0F, 0.0F, 0.0F);
            bufferBuilder.vertex((float)size, 0.0F, 0.0F).color(-65536).normal(1.0F, 0.0F, 0.0F);
         }

         if (drawY) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-16711936).normal(0.0F, 1.0F, 0.0F);
            bufferBuilder.vertex(0.0F, (float)size, 0.0F).color(-16711936).normal(0.0F, 1.0F, 0.0F);
         }

         if (drawZ) {
            bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(-8421377).normal(0.0F, 0.0F, 1.0F);
            bufferBuilder.vertex(0.0F, 0.0F, (float)size).color(-8421377).normal(0.0F, 0.0F, 1.0F);
         }

         BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
         RenderSystem.lineWidth(1.0F);
         GlStateManager._enableCull();
         GlStateManager._depthMask(true);
      }
   }

   public static Object make(Supplier factory) {
      return factory.get();
   }

   public static Object make(Object object, Consumer initializer) {
      initializer.accept(object);
      return object;
   }
}
