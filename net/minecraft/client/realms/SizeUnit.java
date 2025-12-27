package net.minecraft.client.realms;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum SizeUnit {
   B,
   KB,
   MB,
   GB;

   private static final int BASE = 1024;

   public static SizeUnit getLargestUnit(long bytes) {
      if (bytes < 1024L) {
         return B;
      } else {
         try {
            int i = (int)(Math.log((double)bytes) / Math.log((double)1024.0F));
            String string = String.valueOf("KMGTPE".charAt(i - 1));
            return valueOf(string + "B");
         } catch (Exception var4) {
            return GB;
         }
      }
   }

   public static double convertToUnit(long bytes, SizeUnit unit) {
      return unit == B ? (double)bytes : (double)bytes / Math.pow((double)1024.0F, (double)unit.ordinal());
   }

   public static String getUserFriendlyString(long bytes) {
      int i = 1024;
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int j = (int)(Math.log((double)bytes) / Math.log((double)1024.0F));
         String string = "" + "KMGTPE".charAt(j - 1);
         return String.format(Locale.ROOT, "%.1f %sB", (double)bytes / Math.pow((double)1024.0F, (double)j), string);
      }
   }

   public static String humanReadableSize(long bytes, SizeUnit unit) {
      return String.format(Locale.ROOT, "%." + (unit == GB ? "1" : "0") + "f %s", convertToUnit(bytes, unit), unit.name());
   }

   // $FF: synthetic method
   private static SizeUnit[] method_36844() {
      return new SizeUnit[]{B, KB, MB, GB};
   }
}
