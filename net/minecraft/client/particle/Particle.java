package net.minecraft.client.particle;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public abstract class Particle {
   private static final Box EMPTY_BOUNDING_BOX = new Box((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
   private static final double MAX_SQUARED_COLLISION_CHECK_DISTANCE = MathHelper.square((double)100.0F);
   protected final ClientWorld world;
   protected double prevPosX;
   protected double prevPosY;
   protected double prevPosZ;
   protected double x;
   protected double y;
   protected double z;
   protected double velocityX;
   protected double velocityY;
   protected double velocityZ;
   private Box boundingBox;
   protected boolean onGround;
   protected boolean collidesWithWorld;
   private boolean stopped;
   protected boolean dead;
   protected float spacingXZ;
   protected float spacingY;
   protected final Random random;
   protected int age;
   protected int maxAge;
   protected float gravityStrength;
   protected float red;
   protected float green;
   protected float blue;
   protected float alpha;
   protected float angle;
   protected float prevAngle;
   protected float velocityMultiplier;
   protected boolean ascending;

   protected Particle(ClientWorld world, double x, double y, double z) {
      this.boundingBox = EMPTY_BOUNDING_BOX;
      this.collidesWithWorld = true;
      this.spacingXZ = 0.6F;
      this.spacingY = 1.8F;
      this.random = Random.create();
      this.red = 1.0F;
      this.green = 1.0F;
      this.blue = 1.0F;
      this.alpha = 1.0F;
      this.velocityMultiplier = 0.98F;
      this.ascending = false;
      this.world = world;
      this.setBoundingBoxSpacing(0.2F, 0.2F);
      this.setPos(x, y, z);
      this.prevPosX = x;
      this.prevPosY = y;
      this.prevPosZ = z;
      this.maxAge = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
   }

   public Particle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this(world, x, y, z);
      this.velocityX = velocityX + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.4F;
      this.velocityY = velocityY + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.4F;
      this.velocityZ = velocityZ + (Math.random() * (double)2.0F - (double)1.0F) * (double)0.4F;
      double d = (Math.random() + Math.random() + (double)1.0F) * (double)0.15F;
      double e = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
      this.velocityX = this.velocityX / e * d * (double)0.4F;
      this.velocityY = this.velocityY / e * d * (double)0.4F + (double)0.1F;
      this.velocityZ = this.velocityZ / e * d * (double)0.4F;
   }

   public Particle move(float speed) {
      this.velocityX *= (double)speed;
      this.velocityY = (this.velocityY - (double)0.1F) * (double)speed + (double)0.1F;
      this.velocityZ *= (double)speed;
      return this;
   }

   public void setVelocity(double velocityX, double velocityY, double velocityZ) {
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }

   public Particle scale(float scale) {
      this.setBoundingBoxSpacing(0.2F * scale, 0.2F * scale);
      return this;
   }

   public void setColor(float red, float green, float blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   protected void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public void setMaxAge(int maxAge) {
      this.maxAge = maxAge;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.velocityY -= 0.04 * (double)this.gravityStrength;
         this.move(this.velocityX, this.velocityY, this.velocityZ);
         if (this.ascending && this.y == this.prevPosY) {
            this.velocityX *= 1.1;
            this.velocityZ *= 1.1;
         }

         this.velocityX *= (double)this.velocityMultiplier;
         this.velocityY *= (double)this.velocityMultiplier;
         this.velocityZ *= (double)this.velocityMultiplier;
         if (this.onGround) {
            this.velocityX *= (double)0.7F;
            this.velocityZ *= (double)0.7F;
         }

      }
   }

   public abstract void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta);

   public abstract ParticleTextureSheet getType();

   public String toString() {
      String var10000 = this.getClass().getSimpleName();
      return var10000 + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.red + "," + this.green + "," + this.blue + "," + this.alpha + "), Age " + this.age;
   }

   public void markDead() {
      this.dead = true;
   }

   protected void setBoundingBoxSpacing(float spacingXZ, float spacingY) {
      if (spacingXZ != this.spacingXZ || spacingY != this.spacingY) {
         this.spacingXZ = spacingXZ;
         this.spacingY = spacingY;
         Box box = this.getBoundingBox();
         double d = (box.minX + box.maxX - (double)spacingXZ) / (double)2.0F;
         double e = (box.minZ + box.maxZ - (double)spacingXZ) / (double)2.0F;
         this.setBoundingBox(new Box(d, box.minY, e, d + (double)this.spacingXZ, box.minY + (double)this.spacingY, e + (double)this.spacingXZ));
      }

   }

   public void setPos(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
      float f = this.spacingXZ / 2.0F;
      float g = this.spacingY;
      this.setBoundingBox(new Box(x - (double)f, y, z - (double)f, x + (double)f, y + (double)g, z + (double)f));
   }

   public void move(double dx, double dy, double dz) {
      if (!this.stopped) {
         double d = dx;
         double e = dy;
         double f = dz;
         if (this.collidesWithWorld && (dx != (double)0.0F || dy != (double)0.0F || dz != (double)0.0F) && dx * dx + dy * dy + dz * dz < MAX_SQUARED_COLLISION_CHECK_DISTANCE) {
            Vec3d vec3d = Entity.adjustMovementForCollisions((Entity)null, new Vec3d(dx, dy, dz), this.getBoundingBox(), this.world, List.of());
            dx = vec3d.x;
            dy = vec3d.y;
            dz = vec3d.z;
         }

         if (dx != (double)0.0F || dy != (double)0.0F || dz != (double)0.0F) {
            this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
            this.repositionFromBoundingBox();
         }

         if (Math.abs(e) >= (double)1.0E-5F && Math.abs(dy) < (double)1.0E-5F) {
            this.stopped = true;
         }

         this.onGround = e != dy && e < (double)0.0F;
         if (d != dx) {
            this.velocityX = (double)0.0F;
         }

         if (f != dz) {
            this.velocityZ = (double)0.0F;
         }

      }
   }

   protected void repositionFromBoundingBox() {
      Box box = this.getBoundingBox();
      this.x = (box.minX + box.maxX) / (double)2.0F;
      this.y = box.minY;
      this.z = (box.minZ + box.maxZ) / (double)2.0F;
   }

   protected int getBrightness(float tint) {
      BlockPos blockPos = BlockPos.ofFloored(this.x, this.y, this.z);
      return this.world.isChunkLoaded(blockPos) ? WorldRenderer.getLightmapCoordinates(this.world, blockPos) : 0;
   }

   public boolean isAlive() {
      return !this.dead;
   }

   public Box getBoundingBox() {
      return this.boundingBox;
   }

   public void setBoundingBox(Box boundingBox) {
      this.boundingBox = boundingBox;
   }

   public Optional getGroup() {
      return Optional.empty();
   }

   @Environment(EnvType.CLIENT)
   public static record DynamicAlpha(float startAlpha, float endAlpha, float startAtNormalizedAge, float endAtNormalizedAge) {
      public static final DynamicAlpha OPAQUE = new DynamicAlpha(1.0F, 1.0F, 0.0F, 1.0F);

      public boolean isOpaque() {
         return this.startAlpha >= 1.0F && this.endAlpha >= 1.0F;
      }

      public float getAlpha(int age, int maxAge, float tickDelta) {
         if (MathHelper.approximatelyEquals(this.startAlpha, this.endAlpha)) {
            return this.startAlpha;
         } else {
            float f = MathHelper.getLerpProgress(((float)age + tickDelta) / (float)maxAge, this.startAtNormalizedAge, this.endAtNormalizedAge);
            return MathHelper.clampedLerp(this.startAlpha, this.endAlpha, f);
         }
      }
   }
}
