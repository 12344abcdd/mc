package net.minecraft.client.texture;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

@Environment(EnvType.CLIENT)
public interface Scaling {
   Codec CODEC = Scaling.Type.CODEC.dispatch(Scaling::getType, Type::getCodec);
   Scaling STRETCH = new Stretch();

   Type getType();

   @Environment(EnvType.CLIENT)
   public static record NineSlice(int width, int height, Border border) implements Scaling {
      public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codecs.POSITIVE_INT.fieldOf("width").forGetter(NineSlice::width), Codecs.POSITIVE_INT.fieldOf("height").forGetter(NineSlice::height), Scaling.NineSlice.Border.CODEC.fieldOf("border").forGetter(NineSlice::border)).apply(instance, NineSlice::new)).validate(NineSlice::validate);

      private static DataResult validate(NineSlice nineSlice) {
         Border border = nineSlice.border();
         if (border.left() + border.right() >= nineSlice.width()) {
            return DataResult.error(() -> {
               int var10000 = border.left();
               return "Nine-sliced texture has no horizontal center slice: " + var10000 + " + " + border.right() + " >= " + nineSlice.width();
            });
         } else {
            return border.top() + border.bottom() >= nineSlice.height() ? DataResult.error(() -> {
               int var10000 = border.top();
               return "Nine-sliced texture has no vertical center slice: " + var10000 + " + " + border.bottom() + " >= " + nineSlice.height();
            }) : DataResult.success(nineSlice);
         }
      }

      public Type getType() {
         return Scaling.Type.NINE_SLICE;
      }

      @Environment(EnvType.CLIENT)
      public static record Border(int left, int top, int right, int bottom) {
         private static final Codec UNIFORM_SIDE_SIZES_CODEC;
         private static final Codec DIFFERENT_SIDE_SIZES_CODEC;
         static final Codec CODEC;

         private OptionalInt getUniformSideSize() {
            return this.left() == this.top() && this.top() == this.right() && this.right() == this.bottom() ? OptionalInt.of(this.left()) : OptionalInt.empty();
         }

         static {
            UNIFORM_SIDE_SIZES_CODEC = Codecs.POSITIVE_INT.flatComapMap((size) -> new Border(size, size, size, size), (border) -> {
               OptionalInt optionalInt = border.getUniformSideSize();
               return optionalInt.isPresent() ? DataResult.success(optionalInt.getAsInt()) : DataResult.error(() -> "Border has different side sizes");
            });
            DIFFERENT_SIDE_SIZES_CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codecs.NONNEGATIVE_INT.fieldOf("left").forGetter(Border::left), Codecs.NONNEGATIVE_INT.fieldOf("top").forGetter(Border::top), Codecs.NONNEGATIVE_INT.fieldOf("right").forGetter(Border::right), Codecs.NONNEGATIVE_INT.fieldOf("bottom").forGetter(Border::bottom)).apply(instance, Border::new));
            CODEC = Codec.either(UNIFORM_SIDE_SIZES_CODEC, DIFFERENT_SIDE_SIZES_CODEC).xmap(Either::unwrap, (border) -> border.getUniformSideSize().isPresent() ? Either.left(border) : Either.right(border));
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static record Stretch() implements Scaling {
      public static final MapCodec CODEC = MapCodec.unit(Stretch::new);

      public Type getType() {
         return Scaling.Type.STRETCH;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record Tile(int width, int height) implements Scaling {
      public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codecs.POSITIVE_INT.fieldOf("width").forGetter(Tile::width), Codecs.POSITIVE_INT.fieldOf("height").forGetter(Tile::height)).apply(instance, Tile::new));

      public Type getType() {
         return Scaling.Type.TILE;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Type implements StringIdentifiable {
      STRETCH("stretch", Scaling.Stretch.CODEC),
      TILE("tile", Scaling.Tile.CODEC),
      NINE_SLICE("nine_slice", Scaling.NineSlice.CODEC);

      public static final Codec CODEC = StringIdentifiable.createCodec(Type::values);
      private final String name;
      private final MapCodec codec;

      private Type(final String name, final MapCodec codec) {
         this.name = name;
         this.codec = codec;
      }

      public String asString() {
         return this.name;
      }

      public MapCodec getCodec() {
         return this.codec;
      }

      // $FF: synthetic method
      private static Type[] method_52887() {
         return new Type[]{STRETCH, TILE, NINE_SLICE};
      }
   }
}
