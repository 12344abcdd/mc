package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class VertexFormat {
   public static final int field_52099 = -1;
   private final List elements;
   private final List names;
   private final int vertexSizeByte;
   private final int requiredMask;
   private final int[] offsetsByElementId = new int[32];
   @Nullable
   private VertexBuffer buffer;

   VertexFormat(List elements, List names, IntList offsets, int vertexSizeByte) {
      this.elements = elements;
      this.names = names;
      this.vertexSizeByte = vertexSizeByte;
      this.requiredMask = elements.stream().mapToInt(VertexFormatElement::getBit).reduce(0, (a, b) -> a | b);

      for(int i = 0; i < this.offsetsByElementId.length; ++i) {
         VertexFormatElement vertexFormatElement = VertexFormatElement.get(i);
         int j = vertexFormatElement != null ? elements.indexOf(vertexFormatElement) : -1;
         this.offsetsByElementId[i] = j != -1 ? offsets.getInt(j) : -1;
      }

   }

   public static Builder builder() {
      return new Builder();
   }

   public String toString() {
      StringBuilder stringBuilder = (new StringBuilder("Vertex format (")).append(this.vertexSizeByte).append(" bytes):\n");

      for(int i = 0; i < this.elements.size(); ++i) {
         VertexFormatElement vertexFormatElement = (VertexFormatElement)this.elements.get(i);
         stringBuilder.append(i).append(". ").append((String)this.names.get(i)).append(": ").append(vertexFormatElement).append(" @ ").append(this.getOffset(vertexFormatElement)).append('\n');
      }

      return stringBuilder.toString();
   }

   public int getVertexSizeByte() {
      return this.vertexSizeByte;
   }

   public List getElements() {
      return this.elements;
   }

   public List getAttributeNames() {
      return this.names;
   }

   public int[] getOffsetsByElementId() {
      return this.offsetsByElementId;
   }

   public int getOffset(VertexFormatElement element) {
      return this.offsetsByElementId[element.id()];
   }

   public boolean has(VertexFormatElement element) {
      return (this.requiredMask & element.getBit()) != 0;
   }

   public int getRequiredMask() {
      return this.requiredMask;
   }

   public String getName(VertexFormatElement element) {
      int i = this.elements.indexOf(element);
      if (i == -1) {
         throw new IllegalArgumentException(String.valueOf(element) + " is not contained in format");
      } else {
         return (String)this.names.get(i);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         boolean var10000;
         if (o instanceof VertexFormat) {
            VertexFormat vertexFormat = (VertexFormat)o;
            if (this.requiredMask == vertexFormat.requiredMask && this.vertexSizeByte == vertexFormat.vertexSizeByte && this.names.equals(vertexFormat.names) && Arrays.equals(this.offsetsByElementId, vertexFormat.offsetsByElementId)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   public int hashCode() {
      return this.requiredMask * 31 + Arrays.hashCode(this.offsetsByElementId);
   }

   public void setupState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::setupStateInternal);
      } else {
         this.setupStateInternal();
      }
   }

   private void setupStateInternal() {
      int i = this.getVertexSizeByte();

      for(int j = 0; j < this.elements.size(); ++j) {
         GlStateManager._enableVertexAttribArray(j);
         VertexFormatElement vertexFormatElement = (VertexFormatElement)this.elements.get(j);
         vertexFormatElement.setupState(j, (long)this.getOffset(vertexFormatElement), i);
      }

   }

   public void clearState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::clearStateInternal);
      } else {
         this.clearStateInternal();
      }
   }

   private void clearStateInternal() {
      for(int i = 0; i < this.elements.size(); ++i) {
         GlStateManager._disableVertexAttribArray(i);
      }

   }

   public VertexBuffer getBuffer() {
      VertexBuffer vertexBuffer = this.buffer;
      if (vertexBuffer == null) {
         this.buffer = vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
      }

      return vertexBuffer;
   }

   @Environment(EnvType.CLIENT)
   public static class Builder {
      private final ImmutableMap.Builder elements = ImmutableMap.builder();
      private final IntList offsets = new IntArrayList();
      private int currentOffset;

      Builder() {
      }

      public Builder add(String name, VertexFormatElement element) {
         this.elements.put(name, element);
         this.offsets.add(this.currentOffset);
         this.currentOffset += element.getSizeInBytes();
         return this;
      }

      public Builder skip(int offset) {
         this.currentOffset += offset;
         return this;
      }

      public VertexFormat build() {
         ImmutableMap<String, VertexFormatElement> immutableMap = this.elements.buildOrThrow();
         ImmutableList<VertexFormatElement> immutableList = immutableMap.values().asList();
         ImmutableList<String> immutableList2 = immutableMap.keySet().asList();
         return new VertexFormat(immutableList, immutableList2, this.offsets, this.currentOffset);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum DrawMode {
      LINES(4, 2, 2, false),
      LINE_STRIP(5, 2, 1, true),
      DEBUG_LINES(1, 2, 2, false),
      DEBUG_LINE_STRIP(3, 2, 1, true),
      TRIANGLES(4, 3, 3, false),
      TRIANGLE_STRIP(5, 3, 1, true),
      TRIANGLE_FAN(6, 3, 1, true),
      QUADS(4, 4, 4, false);

      public final int glMode;
      public final int firstVertexCount;
      public final int additionalVertexCount;
      public final boolean shareVertices;

      private DrawMode(final int glMode, final int firstVertexCount, final int additionalVertexCount, final boolean shareVertices) {
         this.glMode = glMode;
         this.firstVertexCount = firstVertexCount;
         this.additionalVertexCount = additionalVertexCount;
         this.shareVertices = shareVertices;
      }

      public int getIndexCount(int vertexCount) {
         int i;
         switch (this.ordinal()) {
            case 0:
            case 7:
               i = vertexCount / 4 * 6;
               break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
               i = vertexCount;
               break;
            default:
               i = 0;
         }

         return i;
      }

      // $FF: synthetic method
      private static DrawMode[] method_36817() {
         return new DrawMode[]{LINES, LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUADS};
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum IndexType {
      SHORT(5123, 2),
      INT(5125, 4);

      public final int glType;
      public final int size;

      private IndexType(final int glType, final int size) {
         this.glType = glType;
         this.size = size;
      }

      public static IndexType smallestFor(int indexCount) {
         return (indexCount & -65536) != 0 ? INT : SHORT;
      }

      // $FF: synthetic method
      private static IndexType[] method_36816() {
         return new IndexType[]{SHORT, INT};
      }
   }
}
