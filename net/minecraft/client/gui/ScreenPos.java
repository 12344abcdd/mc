package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;

@Environment(EnvType.CLIENT)
public record ScreenPos(int x, int y) {
   public static ScreenPos of(NavigationAxis axis, int sameAxis, int otherAxis) {
      ScreenPos var10000;
      switch (axis) {
         case HORIZONTAL -> var10000 = new ScreenPos(sameAxis, otherAxis);
         case VERTICAL -> var10000 = new ScreenPos(otherAxis, sameAxis);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public ScreenPos add(NavigationDirection direction) {
      ScreenPos var10000;
      switch (direction) {
         case DOWN -> var10000 = new ScreenPos(this.x, this.y + 1);
         case UP -> var10000 = new ScreenPos(this.x, this.y - 1);
         case LEFT -> var10000 = new ScreenPos(this.x - 1, this.y);
         case RIGHT -> var10000 = new ScreenPos(this.x + 1, this.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public int getComponent(NavigationAxis axis) {
      int var10000;
      switch (axis) {
         case HORIZONTAL -> var10000 = this.x;
         case VERTICAL -> var10000 = this.y;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
