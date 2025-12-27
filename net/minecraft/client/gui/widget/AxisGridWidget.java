package net.minecraft.client.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import net.minecraft.util.math.Divider;

@Environment(EnvType.CLIENT)
public class AxisGridWidget extends WrapperWidget {
   private final DisplayAxis axis;
   private final List elements;
   private final Positioner mainPositioner;

   public AxisGridWidget(int width, int height, DisplayAxis axis) {
      this(0, 0, width, height, axis);
   }

   public AxisGridWidget(int x, int y, int width, int height, DisplayAxis axis) {
      super(x, y, width, height);
      this.elements = new ArrayList();
      this.mainPositioner = Positioner.create();
      this.axis = axis;
   }

   public void refreshPositions() {
      super.refreshPositions();
      if (!this.elements.isEmpty()) {
         int i = 0;
         int j = this.axis.getOtherAxisLength((Widget)this);

         for(Element element : this.elements) {
            i += this.axis.getSameAxisLength(element);
            j = Math.max(j, this.axis.getOtherAxisLength(element));
         }

         int k = this.axis.getSameAxisLength((Widget)this) - i;
         int l = this.axis.getSameAxisCoordinate(this);
         Iterator<Element> iterator = this.elements.iterator();
         Element element2 = (Element)iterator.next();
         this.axis.setSameAxisCoordinate(element2, l);
         l += this.axis.getSameAxisLength(element2);
         Element element3;
         if (this.elements.size() >= 2) {
            for(Divider divider = new Divider(k, this.elements.size() - 1); divider.hasNext(); l += this.axis.getSameAxisLength(element3)) {
               l += divider.nextInt();
               element3 = (Element)iterator.next();
               this.axis.setSameAxisCoordinate(element3, l);
            }
         }

         int m = this.axis.getOtherAxisCoordinate(this);

         for(Element element4 : this.elements) {
            this.axis.setOtherAxisCoordinate(element4, m, j);
         }

         switch (this.axis.ordinal()) {
            case 0 -> this.height = j;
            case 1 -> this.width = j;
         }

      }
   }

   public void forEachElement(Consumer consumer) {
      this.elements.forEach((element) -> consumer.accept(element.widget));
   }

   public Positioner copyPositioner() {
      return this.mainPositioner.copy();
   }

   public Positioner getMainPositioner() {
      return this.mainPositioner;
   }

   public Widget add(Widget widget) {
      return this.add(widget, this.copyPositioner());
   }

   public Widget add(Widget widget, Positioner positioner) {
      this.elements.add(new Element(widget, positioner));
      return widget;
   }

   public Widget add(Widget widget, Consumer callback) {
      return this.add(widget, (Positioner)Util.make(this.copyPositioner(), callback));
   }

   @Environment(EnvType.CLIENT)
   public static enum DisplayAxis {
      HORIZONTAL,
      VERTICAL;

      int getSameAxisLength(Widget widget) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = widget.getWidth();
            case 1 -> var10000 = widget.getHeight();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      int getSameAxisLength(Element element) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = element.getWidth();
            case 1 -> var10000 = element.getHeight();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      int getOtherAxisLength(Widget widget) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = widget.getHeight();
            case 1 -> var10000 = widget.getWidth();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      int getOtherAxisLength(Element element) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = element.getHeight();
            case 1 -> var10000 = element.getWidth();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      void setSameAxisCoordinate(Element element, int low) {
         switch (this.ordinal()) {
            case 0 -> element.setX(low, element.getWidth());
            case 1 -> element.setY(low, element.getHeight());
         }

      }

      void setOtherAxisCoordinate(Element element, int low, int high) {
         switch (this.ordinal()) {
            case 0 -> element.setY(low, high);
            case 1 -> element.setX(low, high);
         }

      }

      int getSameAxisCoordinate(Widget widget) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = widget.getX();
            case 1 -> var10000 = widget.getY();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      int getOtherAxisCoordinate(Widget widget) {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = widget.getY();
            case 1 -> var10000 = widget.getX();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static DisplayAxis[] method_46501() {
         return new DisplayAxis[]{HORIZONTAL, VERTICAL};
      }
   }

   @Environment(EnvType.CLIENT)
   static class Element extends WrapperWidget.WrappedElement {
      protected Element(Widget widget, Positioner positioner) {
         super(widget, positioner);
      }
   }
}
