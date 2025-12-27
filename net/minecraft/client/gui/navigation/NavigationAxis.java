package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum NavigationAxis {
   HORIZONTAL,
   VERTICAL;

   public NavigationAxis getOther() {
      NavigationAxis var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = VERTICAL;
         case 1 -> var10000 = HORIZONTAL;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public NavigationDirection getPositiveDirection() {
      NavigationDirection var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = NavigationDirection.RIGHT;
         case 1 -> var10000 = NavigationDirection.DOWN;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public NavigationDirection getNegativeDirection() {
      NavigationDirection var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = NavigationDirection.LEFT;
         case 1 -> var10000 = NavigationDirection.UP;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public NavigationDirection getDirection(boolean positive) {
      return positive ? this.getPositiveDirection() : this.getNegativeDirection();
   }

   // $FF: synthetic method
   private static NavigationAxis[] method_48236() {
      return new NavigationAxis[]{HORIZONTAL, VERTICAL};
   }
}
