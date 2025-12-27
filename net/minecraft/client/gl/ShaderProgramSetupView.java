package net.minecraft.client.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ShaderProgramSetupView {
   int getGlRef();

   void markUniformsDirty();

   ShaderStage getVertexShader();

   ShaderStage getFragmentShader();

   void attachReferencedShaders();
}
