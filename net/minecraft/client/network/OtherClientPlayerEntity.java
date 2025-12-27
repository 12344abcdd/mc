package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class OtherClientPlayerEntity extends AbstractClientPlayerEntity {
   private Vec3d clientVelocity;
   private int velocityLerpDivisor;

   public OtherClientPlayerEntity(ClientWorld clientWorld, GameProfile gameProfile) {
      super(clientWorld, gameProfile);
      this.clientVelocity = Vec3d.ZERO;
      this.noClip = true;
   }

   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * (double)10.0F;
      if (Double.isNaN(d)) {
         d = (double)1.0F;
      }

      d *= (double)64.0F * getRenderDistanceMultiplier();
      return distance < d * d;
   }

   public boolean damage(DamageSource source, float amount) {
      return true;
   }

   public void tick() {
      super.tick();
      this.updateLimbs(false);
   }

   public void tickMovement() {
      if (this.bodyTrackingIncrements > 0) {
         this.lerpPosAndRotation(this.bodyTrackingIncrements, this.serverX, this.serverY, this.serverZ, this.serverYaw, this.serverPitch);
         --this.bodyTrackingIncrements;
      }

      if (this.headTrackingIncrements > 0) {
         this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw);
         --this.headTrackingIncrements;
      }

      if (this.velocityLerpDivisor > 0) {
         this.addVelocityInternal(new Vec3d((this.clientVelocity.x - this.getVelocity().x) / (double)this.velocityLerpDivisor, (this.clientVelocity.y - this.getVelocity().y) / (double)this.velocityLerpDivisor, (this.clientVelocity.z - this.getVelocity().z) / (double)this.velocityLerpDivisor));
         --this.velocityLerpDivisor;
      }

      this.prevStrideDistance = this.strideDistance;
      this.tickHandSwing();
      float f;
      if (this.isOnGround() && !this.isDead()) {
         f = (float)Math.min(0.1, this.getVelocity().horizontalLength());
      } else {
         f = 0.0F;
      }

      this.strideDistance += (f - this.strideDistance) * 0.4F;
      this.getWorld().getProfiler().push("push");
      this.tickCramming();
      this.getWorld().getProfiler().pop();
   }

   public void setVelocityClient(double x, double y, double z) {
      this.clientVelocity = new Vec3d(x, y, z);
      this.velocityLerpDivisor = this.getType().getTrackTickInterval() + 1;
   }

   protected void updatePose() {
   }

   public void sendMessage(Text message) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      minecraftClient.inGameHud.getChatHud().addMessage(message);
   }

   public void onSpawnPacket(EntitySpawnS2CPacket packet) {
      super.onSpawnPacket(packet);
      this.resetPosition();
   }
}
