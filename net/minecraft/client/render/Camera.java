package net.minecraft.client.render;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class Camera {
   private static final float BASE_CAMERA_DISTANCE = 4.0F;
   private static final Vector3f HORIZONTAL = new Vector3f(0.0F, 0.0F, -1.0F);
   private static final Vector3f VERTICAL = new Vector3f(0.0F, 1.0F, 0.0F);
   private static final Vector3f DIAGONAL = new Vector3f(-1.0F, 0.0F, 0.0F);
   private boolean ready;
   private BlockView area;
   private Entity focusedEntity;
   private Vec3d pos;
   private final BlockPos.Mutable blockPos;
   private final Vector3f horizontalPlane;
   private final Vector3f verticalPlane;
   private final Vector3f diagonalPlane;
   private float pitch;
   private float yaw;
   private final Quaternionf rotation;
   private boolean thirdPerson;
   private float cameraY;
   private float lastCameraY;
   private float lastTickDelta;
   public static final float field_32133 = 0.083333336F;

   public Camera() {
      this.pos = Vec3d.ZERO;
      this.blockPos = new BlockPos.Mutable();
      this.horizontalPlane = new Vector3f(HORIZONTAL);
      this.verticalPlane = new Vector3f(VERTICAL);
      this.diagonalPlane = new Vector3f(DIAGONAL);
      this.rotation = new Quaternionf();
   }

   public void update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
      this.ready = true;
      this.area = area;
      this.focusedEntity = focusedEntity;
      this.thirdPerson = thirdPerson;
      this.lastTickDelta = tickDelta;
      this.setRotation(focusedEntity.getYaw(tickDelta), focusedEntity.getPitch(tickDelta));
      this.setPos(MathHelper.lerp((double)tickDelta, focusedEntity.prevX, focusedEntity.getX()), MathHelper.lerp((double)tickDelta, focusedEntity.prevY, focusedEntity.getY()) + (double)MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY), MathHelper.lerp((double)tickDelta, focusedEntity.prevZ, focusedEntity.getZ()));
      if (thirdPerson) {
         if (inverseView) {
            this.setRotation(this.yaw + 180.0F, -this.pitch);
         }

         float var10000;
         if (focusedEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)focusedEntity;
            var10000 = livingEntity.getScale();
         } else {
            var10000 = 1.0F;
         }

         float f = var10000;
         this.moveBy(-this.clipToSpace(4.0F * f), 0.0F, 0.0F);
      } else if (focusedEntity instanceof LivingEntity && ((LivingEntity)focusedEntity).isSleeping()) {
         Direction direction = ((LivingEntity)focusedEntity).getSleepingDirection();
         this.setRotation(direction != null ? direction.asRotation() - 180.0F : 0.0F, 0.0F);
         this.moveBy(0.0F, 0.3F, 0.0F);
      }

   }

   public void updateEyeHeight() {
      if (this.focusedEntity != null) {
         this.lastCameraY = this.cameraY;
         this.cameraY += (this.focusedEntity.getStandingEyeHeight() - this.cameraY) * 0.5F;
      }

   }

   private float clipToSpace(float f) {
      float g = 0.1F;

      for(int i = 0; i < 8; ++i) {
         float h = (float)((i & 1) * 2 - 1);
         float j = (float)((i >> 1 & 1) * 2 - 1);
         float k = (float)((i >> 2 & 1) * 2 - 1);
         Vec3d vec3d = this.pos.add((double)(h * 0.1F), (double)(j * 0.1F), (double)(k * 0.1F));
         Vec3d vec3d2 = vec3d.add((new Vec3d(this.horizontalPlane)).multiply((double)(-f)));
         HitResult hitResult = this.area.raycast(new RaycastContext(vec3d, vec3d2, ShapeType.VISUAL, FluidHandling.NONE, this.focusedEntity));
         if (hitResult.getType() != Type.MISS) {
            float l = (float)hitResult.getPos().squaredDistanceTo(this.pos);
            if (l < MathHelper.square(f)) {
               f = MathHelper.sqrt(l);
            }
         }
      }

      return f;
   }

   protected void moveBy(float f, float g, float h) {
      Vector3f vector3f = (new Vector3f(h, g, -f)).rotate(this.rotation);
      this.setPos(new Vec3d(this.pos.x + (double)vector3f.x, this.pos.y + (double)vector3f.y, this.pos.z + (double)vector3f.z));
   }

   protected void setRotation(float yaw, float pitch) {
      this.pitch = pitch;
      this.yaw = yaw;
      this.rotation.rotationYXZ((float)Math.PI - yaw * ((float)Math.PI / 180F), -pitch * ((float)Math.PI / 180F), 0.0F);
      HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
      VERTICAL.rotate(this.rotation, this.verticalPlane);
      DIAGONAL.rotate(this.rotation, this.diagonalPlane);
   }

   protected void setPos(double x, double y, double z) {
      this.setPos(new Vec3d(x, y, z));
   }

   protected void setPos(Vec3d pos) {
      this.pos = pos;
      this.blockPos.set(pos.x, pos.y, pos.z);
   }

   public Vec3d getPos() {
      return this.pos;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public Entity getFocusedEntity() {
      return this.focusedEntity;
   }

   public boolean isReady() {
      return this.ready;
   }

   public boolean isThirdPerson() {
      return this.thirdPerson;
   }

   public Projection getProjection() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      double d = (double)minecraftClient.getWindow().getFramebufferWidth() / (double)minecraftClient.getWindow().getFramebufferHeight();
      double e = Math.tan((double)((float)(Integer)minecraftClient.options.getFov().getValue() * ((float)Math.PI / 180F)) / (double)2.0F) * (double)0.05F;
      double f = e * d;
      Vec3d vec3d = (new Vec3d(this.horizontalPlane)).multiply((double)0.05F);
      Vec3d vec3d2 = (new Vec3d(this.diagonalPlane)).multiply(f);
      Vec3d vec3d3 = (new Vec3d(this.verticalPlane)).multiply(e);
      return new Projection(vec3d, vec3d2, vec3d3);
   }

   public CameraSubmersionType getSubmersionType() {
      if (!this.ready) {
         return CameraSubmersionType.NONE;
      } else {
         FluidState fluidState = this.area.getFluidState(this.blockPos);
         if (fluidState.isIn(FluidTags.WATER) && this.pos.y < (double)((float)this.blockPos.getY() + fluidState.getHeight(this.area, this.blockPos))) {
            return CameraSubmersionType.WATER;
         } else {
            Projection projection = this.getProjection();

            for(Vec3d vec3d : Arrays.asList(projection.center, projection.getBottomRight(), projection.getTopRight(), projection.getBottomLeft(), projection.getTopLeft())) {
               Vec3d vec3d2 = this.pos.add(vec3d);
               BlockPos blockPos = BlockPos.ofFloored(vec3d2);
               FluidState fluidState2 = this.area.getFluidState(blockPos);
               if (fluidState2.isIn(FluidTags.LAVA)) {
                  if (vec3d2.y <= (double)(fluidState2.getHeight(this.area, blockPos) + (float)blockPos.getY())) {
                     return CameraSubmersionType.LAVA;
                  }
               } else {
                  BlockState blockState = this.area.getBlockState(blockPos);
                  if (blockState.isOf(Blocks.POWDER_SNOW)) {
                     return CameraSubmersionType.POWDER_SNOW;
                  }
               }
            }

            return CameraSubmersionType.NONE;
         }
      }
   }

   public final Vector3f getHorizontalPlane() {
      return this.horizontalPlane;
   }

   public final Vector3f getVerticalPlane() {
      return this.verticalPlane;
   }

   public final Vector3f getDiagonalPlane() {
      return this.diagonalPlane;
   }

   public void reset() {
      this.area = null;
      this.focusedEntity = null;
      this.ready = false;
   }

   public float getLastTickDelta() {
      return this.lastTickDelta;
   }

   @Environment(EnvType.CLIENT)
   public static class Projection {
      final Vec3d center;
      private final Vec3d x;
      private final Vec3d y;

      Projection(Vec3d center, Vec3d x, Vec3d y) {
         this.center = center;
         this.x = x;
         this.y = y;
      }

      public Vec3d getBottomRight() {
         return this.center.add(this.y).add(this.x);
      }

      public Vec3d getTopRight() {
         return this.center.add(this.y).subtract(this.x);
      }

      public Vec3d getBottomLeft() {
         return this.center.subtract(this.y).add(this.x);
      }

      public Vec3d getTopLeft() {
         return this.center.subtract(this.y).subtract(this.x);
      }

      public Vec3d getPosition(float factorX, float factorY) {
         return this.center.add(this.y.multiply((double)factorY)).subtract(this.x.multiply((double)factorX));
      }
   }
}
