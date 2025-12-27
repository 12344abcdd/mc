package net.minecraft.client.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.EntityList;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.ClientEntityManager;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientWorld extends World {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final double PARTICLE_Y_OFFSET = 0.05;
   private static final int field_34805 = 10;
   private static final int field_34806 = 1000;
   final EntityList entityList = new EntityList();
   private final ClientEntityManager entityManager = new ClientEntityManager(Entity.class, new ClientEntityHandler());
   private final ClientPlayNetworkHandler networkHandler;
   private final WorldRenderer worldRenderer;
   private final Properties clientWorldProperties;
   private final DimensionEffects dimensionEffects;
   private final TickManager tickManager;
   private final MinecraftClient client = MinecraftClient.getInstance();
   final List players = Lists.newArrayList();
   private final Map mapStates = Maps.newHashMap();
   private static final long field_32640 = 16777215L;
   private int lightningTicksLeft;
   private final Object2ObjectArrayMap colorCache = (Object2ObjectArrayMap)Util.make(new Object2ObjectArrayMap(3), (map) -> {
      map.put(BiomeColors.GRASS_COLOR, new BiomeColorCache((pos) -> this.calculateColor(pos, BiomeColors.GRASS_COLOR)));
      map.put(BiomeColors.FOLIAGE_COLOR, new BiomeColorCache((pos) -> this.calculateColor(pos, BiomeColors.FOLIAGE_COLOR)));
      map.put(BiomeColors.WATER_COLOR, new BiomeColorCache((pos) -> this.calculateColor(pos, BiomeColors.WATER_COLOR)));
   });
   private final ClientChunkManager chunkManager;
   private final Deque chunkUpdaters = Queues.newArrayDeque();
   private int simulationDistance;
   private final PendingUpdateManager pendingUpdateManager = new PendingUpdateManager();
   private static final Set BLOCK_MARKER_ITEMS;

   public void handlePlayerActionResponse(int sequence) {
      this.pendingUpdateManager.processPendingUpdates(sequence, this);
   }

   public void handleBlockUpdate(BlockPos pos, BlockState state, int flags) {
      if (!this.pendingUpdateManager.hasPendingUpdate(pos, state)) {
         super.setBlockState(pos, state, flags, 512);
      }

   }

   public void processPendingUpdate(BlockPos pos, BlockState state, Vec3d playerPos) {
      BlockState blockState = this.getBlockState(pos);
      if (blockState != state) {
         this.setBlockState(pos, state, 19);
         PlayerEntity playerEntity = this.client.player;
         if (this == playerEntity.getWorld() && playerEntity.collidesWithStateAtPos(pos, state)) {
            playerEntity.updatePosition(playerPos.x, playerPos.y, playerPos.z);
         }
      }

   }

   PendingUpdateManager getPendingUpdateManager() {
      return this.pendingUpdateManager;
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
      if (this.pendingUpdateManager.hasPendingSequence()) {
         BlockState blockState = this.getBlockState(pos);
         boolean bl = super.setBlockState(pos, state, flags, maxUpdateDepth);
         if (bl) {
            this.pendingUpdateManager.addPendingUpdate(pos, blockState, this.client.player);
         }

         return bl;
      } else {
         return super.setBlockState(pos, state, flags, maxUpdateDepth);
      }
   }

   public ClientWorld(ClientPlayNetworkHandler networkHandler, Properties properties, RegistryKey registryRef, RegistryEntry dimensionTypeEntry, int loadDistance, int simulationDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed) {
      super(properties, registryRef, networkHandler.getRegistryManager(), dimensionTypeEntry, profiler, true, debugWorld, seed, 1000000);
      this.networkHandler = networkHandler;
      this.chunkManager = new ClientChunkManager(this, loadDistance);
      this.tickManager = new TickManager();
      this.clientWorldProperties = properties;
      this.worldRenderer = worldRenderer;
      this.dimensionEffects = DimensionEffects.byDimensionType((DimensionType)dimensionTypeEntry.value());
      this.setSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.simulationDistance = simulationDistance;
      this.calculateAmbientDarkness();
      this.initWeatherGradients();
   }

   public void enqueueChunkUpdate(Runnable updater) {
      this.chunkUpdaters.add(updater);
   }

   public void runQueuedChunkUpdates() {
      int i = this.chunkUpdaters.size();
      int j = i < 1000 ? Math.max(10, i / 10) : i;

      for(int k = 0; k < j; ++k) {
         Runnable runnable = (Runnable)this.chunkUpdaters.poll();
         if (runnable == null) {
            break;
         }

         runnable.run();
      }

   }

   public boolean hasNoChunkUpdaters() {
      return this.chunkUpdaters.isEmpty();
   }

   public DimensionEffects getDimensionEffects() {
      return this.dimensionEffects;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      this.getWorldBorder().tick();
      if (this.getTickManager().shouldTick()) {
         this.tickTime();
      }

      if (this.lightningTicksLeft > 0) {
         this.setLightningTicksLeft(this.lightningTicksLeft - 1);
      }

      this.getProfiler().push("blocks");
      this.chunkManager.tick(shouldKeepTicking, true);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setTime(this.properties.getTime() + 1L);
      if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
         this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
      }

   }

   public void setTime(long time) {
      this.clientWorldProperties.setTime(time);
   }

   public void setTimeOfDay(long timeOfDay) {
      if (timeOfDay < 0L) {
         timeOfDay = -timeOfDay;
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, (MinecraftServer)null);
      } else {
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(true, (MinecraftServer)null);
      }

      this.clientWorldProperties.setTimeOfDay(timeOfDay);
   }

   public Iterable getEntities() {
      return this.getEntityLookup().iterate();
   }

   public void tickEntities() {
      Profiler profiler = this.getProfiler();
      profiler.push("entities");
      this.entityList.forEach((entity) -> {
         if (!entity.isRemoved() && !entity.hasVehicle() && !this.tickManager.shouldSkipTick(entity)) {
            this.tickEntity(this::tickEntity, entity);
         }
      });
      profiler.pop();
      this.tickBlockEntities();
   }

   public boolean shouldUpdatePostDeath(Entity entity) {
      return entity.getChunkPos().getChebyshevDistance(this.client.player.getChunkPos()) <= this.simulationDistance;
   }

   public void tickEntity(Entity entity) {
      entity.resetPosition();
      ++entity.age;
      this.getProfiler().push(() -> Registries.ENTITY_TYPE.getId(entity.getType()).toString());
      entity.tick();
      this.getProfiler().pop();

      for(Entity entity2 : entity.getPassengerList()) {
         this.tickPassenger(entity, entity2);
      }

   }

   private void tickPassenger(Entity entity, Entity passenger) {
      if (!passenger.isRemoved() && passenger.getVehicle() == entity) {
         if (passenger instanceof PlayerEntity || this.entityList.has(passenger)) {
            passenger.resetPosition();
            ++passenger.age;
            passenger.tickRiding();

            for(Entity entity2 : passenger.getPassengerList()) {
               this.tickPassenger(passenger, entity2);
            }

         }
      } else {
         passenger.stopRiding();
      }
   }

   public void unloadBlockEntities(WorldChunk chunk) {
      chunk.clear();
      this.chunkManager.getLightingProvider().setColumnEnabled(chunk.getPos(), false);
      this.entityManager.stopTicking(chunk.getPos());
   }

   public void resetChunkColor(ChunkPos chunkPos) {
      this.colorCache.forEach((resolver, cache) -> cache.reset(chunkPos.x, chunkPos.z));
      this.entityManager.startTicking(chunkPos);
      this.worldRenderer.method_52815(chunkPos);
   }

   public void reloadColor() {
      this.colorCache.forEach((resolver, cache) -> cache.reset());
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return true;
   }

   public int getRegularEntityCount() {
      return this.entityManager.getEntityCount();
   }

   public void addEntity(Entity entity) {
      this.removeEntity(entity.getId(), RemovalReason.DISCARDED);
      this.entityManager.addEntity(entity);
   }

   public void removeEntity(int entityId, Entity.RemovalReason removalReason) {
      Entity entity = (Entity)this.getEntityLookup().get(entityId);
      if (entity != null) {
         entity.setRemoved(removalReason);
         entity.onRemoved();
      }

   }

   @Nullable
   public Entity getEntityById(int id) {
      return (Entity)this.getEntityLookup().get(id);
   }

   public void disconnect() {
      this.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.status.quitting"));
   }

   public void doRandomBlockDisplayTicks(int centerX, int centerY, int centerZ) {
      int i = 32;
      Random random = Random.create();
      Block block = this.getBlockParticle();
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      for(int j = 0; j < 667; ++j) {
         this.randomBlockDisplayTick(centerX, centerY, centerZ, 16, random, block, mutable);
         this.randomBlockDisplayTick(centerX, centerY, centerZ, 32, random, block, mutable);
      }

   }

   @Nullable
   private Block getBlockParticle() {
      if (this.client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
         ItemStack itemStack = this.client.player.getMainHandStack();
         Item item = itemStack.getItem();
         if (BLOCK_MARKER_ITEMS.contains(item) && item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            return blockItem.getBlock();
         }
      }

      return null;
   }

   public void randomBlockDisplayTick(int centerX, int centerY, int centerZ, int radius, Random random, @Nullable Block block, BlockPos.Mutable pos) {
      int i = centerX + this.random.nextInt(radius) - this.random.nextInt(radius);
      int j = centerY + this.random.nextInt(radius) - this.random.nextInt(radius);
      int k = centerZ + this.random.nextInt(radius) - this.random.nextInt(radius);
      pos.set(i, j, k);
      BlockState blockState = this.getBlockState(pos);
      blockState.getBlock().randomDisplayTick(blockState, this, pos, random);
      FluidState fluidState = this.getFluidState(pos);
      if (!fluidState.isEmpty()) {
         fluidState.randomDisplayTick(this, pos, random);
         ParticleEffect particleEffect = fluidState.getParticle();
         if (particleEffect != null && this.random.nextInt(10) == 0) {
            boolean bl = blockState.isSideSolidFullSquare(this, pos, Direction.DOWN);
            BlockPos blockPos = pos.down();
            this.addParticle(blockPos, this.getBlockState(blockPos), particleEffect, bl);
         }
      }

      if (block == blockState.getBlock()) {
         this.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, blockState), (double)i + (double)0.5F, (double)j + (double)0.5F, (double)k + (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F);
      }

      if (!blockState.isFullCube(this, pos)) {
         ((Biome)this.getBiome(pos).value()).getParticleConfig().ifPresent((config) -> {
            if (config.shouldAddParticle(this.random)) {
               this.addParticle(config.getParticle(), (double)pos.getX() + this.random.nextDouble(), (double)pos.getY() + this.random.nextDouble(), (double)pos.getZ() + this.random.nextDouble(), (double)0.0F, (double)0.0F, (double)0.0F);
            }

         });
      }

   }

   private void addParticle(BlockPos pos, BlockState state, ParticleEffect parameters, boolean solidBelow) {
      if (state.getFluidState().isEmpty()) {
         VoxelShape voxelShape = state.getCollisionShape(this, pos);
         double d = voxelShape.getMax(Axis.Y);
         if (d < (double)1.0F) {
            if (solidBelow) {
               this.addParticle((double)pos.getX(), (double)(pos.getX() + 1), (double)pos.getZ(), (double)(pos.getZ() + 1), (double)(pos.getY() + 1) - 0.05, parameters);
            }
         } else if (!state.isIn(BlockTags.IMPERMEABLE)) {
            double e = voxelShape.getMin(Axis.Y);
            if (e > (double)0.0F) {
               this.addParticle(pos, parameters, voxelShape, (double)pos.getY() + e - 0.05);
            } else {
               BlockPos blockPos = pos.down();
               BlockState blockState = this.getBlockState(blockPos);
               VoxelShape voxelShape2 = blockState.getCollisionShape(this, blockPos);
               double f = voxelShape2.getMax(Axis.Y);
               if (f < (double)1.0F && blockState.getFluidState().isEmpty()) {
                  this.addParticle(pos, parameters, voxelShape, (double)pos.getY() - 0.05);
               }
            }
         }

      }
   }

   private void addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y) {
      this.addParticle((double)pos.getX() + shape.getMin(Axis.X), (double)pos.getX() + shape.getMax(Axis.X), (double)pos.getZ() + shape.getMin(Axis.Z), (double)pos.getZ() + shape.getMax(Axis.Z), y, parameters);
   }

   private void addParticle(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters) {
      this.addParticle(parameters, MathHelper.lerp(this.random.nextDouble(), minX, maxX), y, MathHelper.lerp(this.random.nextDouble(), minZ, maxZ), (double)0.0F, (double)0.0F, (double)0.0F);
   }

   public CrashReportSection addDetailsToCrashReport(CrashReport report) {
      CrashReportSection crashReportSection = super.addDetailsToCrashReport(report);
      crashReportSection.add("Server brand", () -> this.client.player.networkHandler.getBrand());
      crashReportSection.add("Server type", () -> this.client.getServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
      crashReportSection.add("Tracked entity count", () -> String.valueOf(this.getRegularEntityCount()));
      return crashReportSection;
   }

   public void playSound(@Nullable PlayerEntity source, double x, double y, double z, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      if (source == this.client.player) {
         this.playSound(x, y, z, (SoundEvent)sound.value(), category, volume, pitch, false, seed);
      }

   }

   public void playSoundFromEntity(@Nullable PlayerEntity source, Entity entity, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      if (source == this.client.player) {
         this.client.getSoundManager().play(new EntityTrackingSoundInstance((SoundEvent)sound.value(), category, volume, pitch, entity, seed));
      }

   }

   public void playSoundFromEntity(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      this.client.getSoundManager().play(new EntityTrackingSoundInstance(sound, category, volume, pitch, entity, this.random.nextLong()));
   }

   public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
      this.playSound(x, y, z, sound, category, volume, pitch, useDistance, this.random.nextLong());
   }

   private void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed) {
      double d = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
      PositionedSoundInstance positionedSoundInstance = new PositionedSoundInstance(event, category, volume, pitch, Random.create(seed), x, y, z);
      if (useDistance && d > (double)100.0F) {
         double e = Math.sqrt(d) / (double)40.0F;
         this.client.getSoundManager().play(positionedSoundInstance, (int)(e * (double)20.0F));
      } else {
         this.client.getSoundManager().play(positionedSoundInstance);
      }

   }

   public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, List explosions) {
      if (explosions.isEmpty()) {
         for(int i = 0; i < this.random.nextInt(3) + 2; ++i) {
            this.addParticle(ParticleTypes.POOF, x, y, z, this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05);
         }
      } else {
         this.client.particleManager.addParticle(new FireworksSparkParticle.FireworkParticle(this, x, y, z, velocityX, velocityY, velocityZ, this.client.particleManager, explosions));
      }

   }

   public void sendPacket(Packet packet) {
      this.networkHandler.sendPacket(packet);
   }

   public RecipeManager getRecipeManager() {
      return this.networkHandler.getRecipeManager();
   }

   public TickManager getTickManager() {
      return this.tickManager;
   }

   public QueryableTickScheduler getBlockTickScheduler() {
      return EmptyTickSchedulers.getClientTickScheduler();
   }

   public QueryableTickScheduler getFluidTickScheduler() {
      return EmptyTickSchedulers.getClientTickScheduler();
   }

   public ClientChunkManager getChunkManager() {
      return this.chunkManager;
   }

   @Nullable
   public MapState getMapState(MapIdComponent id) {
      return (MapState)this.mapStates.get(id);
   }

   public void putClientsideMapState(MapIdComponent id, MapState state) {
      this.mapStates.put(id, state);
   }

   public void putMapState(MapIdComponent id, MapState state) {
   }

   public MapIdComponent increaseAndGetMapId() {
      return new MapIdComponent(0);
   }

   public Scoreboard getScoreboard() {
      return this.networkHandler.getScoreboard();
   }

   public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.worldRenderer.updateBlock(this, pos, oldState, newState, flags);
   }

   public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
      this.worldRenderer.scheduleBlockRerenderIfNeeded(pos, old, updated);
   }

   public void scheduleBlockRenders(int x, int y, int z) {
      this.worldRenderer.scheduleBlockRenders(x, y, z);
   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
      this.worldRenderer.setBlockBreakingInfo(entityId, pos, progress);
   }

   public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
      this.worldRenderer.processGlobalEvent(eventId, pos, data);
   }

   public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
      try {
         this.worldRenderer.processWorldEvent(eventId, pos, data);
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.create(throwable, "Playing level event");
         CrashReportSection crashReportSection = crashReport.addElement("Level event being played");
         crashReportSection.add("Block coordinates", CrashReportSection.createPositionString(this, pos));
         crashReportSection.add("Event source", player);
         crashReportSection.add("Event type", eventId);
         crashReportSection.add("Event data", data);
         throw new CrashException(crashReport);
      }
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, false, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public List getPlayers() {
      return this.players;
   }

   public RegistryEntry getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS);
   }

   public float getSkyBrightness(float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      float g = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      g = 1.0F - g;
      g *= 1.0F - this.getRainGradient(tickDelta) * 5.0F / 16.0F;
      g *= 1.0F - this.getThunderGradient(tickDelta) * 5.0F / 16.0F;
      return g * 0.8F + 0.2F;
   }

   public Vec3d getSkyColor(Vec3d cameraPos, float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      Vec3d vec3d = cameraPos.subtract((double)2.0F, (double)2.0F, (double)2.0F).multiply((double)0.25F);
      BiomeAccess biomeAccess = this.getBiomeAccess();
      Vec3d vec3d2 = CubicSampler.sampleColor(vec3d, (x, y, z) -> Vec3d.unpackRgb(((Biome)biomeAccess.getBiomeForNoiseGen(x, y, z).value()).getSkyColor()));
      float g = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      float h = (float)vec3d2.x * g;
      float i = (float)vec3d2.y * g;
      float j = (float)vec3d2.z * g;
      float k = this.getRainGradient(tickDelta);
      if (k > 0.0F) {
         float l = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.6F;
         float m = 1.0F - k * 0.75F;
         h = h * m + l * (1.0F - m);
         i = i * m + l * (1.0F - m);
         j = j * m + l * (1.0F - m);
      }

      float l = this.getThunderGradient(tickDelta);
      if (l > 0.0F) {
         float m = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.2F;
         float n = 1.0F - l * 0.75F;
         h = h * n + m * (1.0F - n);
         i = i * n + m * (1.0F - n);
         j = j * n + m * (1.0F - n);
      }

      int o = this.getLightningTicksLeft();
      if (o > 0) {
         float n = (float)o - tickDelta;
         if (n > 1.0F) {
            n = 1.0F;
         }

         n *= 0.45F;
         h = h * (1.0F - n) + 0.8F * n;
         i = i * (1.0F - n) + 0.8F * n;
         j = j * (1.0F - n) + 1.0F * n;
      }

      return new Vec3d((double)h, (double)i, (double)j);
   }

   public Vec3d getCloudsColor(float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      float g = MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      float h = 1.0F;
      float i = 1.0F;
      float j = 1.0F;
      float k = this.getRainGradient(tickDelta);
      if (k > 0.0F) {
         float l = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.6F;
         float m = 1.0F - k * 0.95F;
         h = h * m + l * (1.0F - m);
         i = i * m + l * (1.0F - m);
         j = j * m + l * (1.0F - m);
      }

      h *= g * 0.9F + 0.1F;
      i *= g * 0.9F + 0.1F;
      j *= g * 0.85F + 0.15F;
      float l = this.getThunderGradient(tickDelta);
      if (l > 0.0F) {
         float m = (h * 0.3F + i * 0.59F + j * 0.11F) * 0.2F;
         float n = 1.0F - l * 0.95F;
         h = h * n + m * (1.0F - n);
         i = i * n + m * (1.0F - n);
         j = j * n + m * (1.0F - n);
      }

      return new Vec3d((double)h, (double)i, (double)j);
   }

   public float getStarBrightness(float tickDelta) {
      float f = this.getSkyAngle(tickDelta);
      float g = 1.0F - (MathHelper.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      g = MathHelper.clamp(g, 0.0F, 1.0F);
      return g * g * 0.5F;
   }

   public int getLightningTicksLeft() {
      return (Boolean)this.client.options.getHideLightningFlashes().getValue() ? 0 : this.lightningTicksLeft;
   }

   public void setLightningTicksLeft(int lightningTicksLeft) {
      this.lightningTicksLeft = lightningTicksLeft;
   }

   public float getBrightness(Direction direction, boolean shaded) {
      boolean bl = this.getDimensionEffects().isDarkened();
      if (!shaded) {
         return bl ? 0.9F : 1.0F;
      } else {
         switch (direction) {
            case DOWN:
               return bl ? 0.9F : 0.5F;
            case UP:
               return bl ? 0.9F : 1.0F;
            case NORTH:
            case SOUTH:
               return 0.8F;
            case WEST:
            case EAST:
               return 0.6F;
            default:
               return 1.0F;
         }
      }
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      BiomeColorCache biomeColorCache = (BiomeColorCache)this.colorCache.get(colorResolver);
      return biomeColorCache.getBiomeColor(pos);
   }

   public int calculateColor(BlockPos pos, ColorResolver colorResolver) {
      int i = (Integer)MinecraftClient.getInstance().options.getBiomeBlendRadius().getValue();
      if (i == 0) {
         return colorResolver.getColor((Biome)this.getBiome(pos).value(), (double)pos.getX(), (double)pos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int m = 0;
         CuboidBlockIterator cuboidBlockIterator = new CuboidBlockIterator(pos.getX() - i, pos.getY(), pos.getZ() - i, pos.getX() + i, pos.getY(), pos.getZ() + i);

         int n;
         for(BlockPos.Mutable mutable = new BlockPos.Mutable(); cuboidBlockIterator.step(); m += n & 255) {
            mutable.set(cuboidBlockIterator.getX(), cuboidBlockIterator.getY(), cuboidBlockIterator.getZ());
            n = colorResolver.getColor((Biome)this.getBiome(mutable).value(), (double)mutable.getX(), (double)mutable.getZ());
            k += (n & 16711680) >> 16;
            l += (n & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | m / j & 255;
      }
   }

   public void setSpawnPos(BlockPos pos, float angle) {
      this.properties.setSpawnPos(pos, angle);
   }

   public String toString() {
      return "ClientLevel";
   }

   public Properties getLevelProperties() {
      return this.clientWorldProperties;
   }

   public void emitGameEvent(RegistryEntry event, Vec3d emitterPos, GameEvent.Emitter emitter) {
   }

   protected Map getMapStates() {
      return ImmutableMap.copyOf(this.mapStates);
   }

   protected void putMapStates(Map mapStates) {
      this.mapStates.putAll(mapStates);
   }

   protected EntityLookup getEntityLookup() {
      return this.entityManager.getLookup();
   }

   public String asString() {
      String var10000 = this.chunkManager.getDebugString();
      return "Chunks[C] W: " + var10000 + " E: " + this.entityManager.getDebugString();
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
      this.client.particleManager.addBlockBreakParticles(pos, state);
   }

   public void setSimulationDistance(int simulationDistance) {
      this.simulationDistance = simulationDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public FeatureSet getEnabledFeatures() {
      return this.networkHandler.getEnabledFeatures();
   }

   public BrewingRecipeRegistry getBrewingRecipeRegistry() {
      return this.networkHandler.getBrewingRecipeRegistry();
   }

   // $FF: synthetic method
   public WorldProperties getLevelProperties() {
      return this.getLevelProperties();
   }

   // $FF: synthetic method
   public ChunkManager getChunkManager() {
      return this.getChunkManager();
   }

   static {
      BLOCK_MARKER_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);
   }

   @Environment(EnvType.CLIENT)
   final class ClientEntityHandler implements EntityHandler {
      public void create(Entity entity) {
      }

      public void destroy(Entity entity) {
      }

      public void startTicking(Entity entity) {
         ClientWorld.this.entityList.add(entity);
      }

      public void stopTicking(Entity entity) {
         ClientWorld.this.entityList.remove(entity);
      }

      public void startTracking(Entity entity) {
         if (entity instanceof AbstractClientPlayerEntity) {
            ClientWorld.this.players.add((AbstractClientPlayerEntity)entity);
         }

      }

      public void stopTracking(Entity entity) {
         entity.detach();
         ClientWorld.this.players.remove(entity);
      }

      public void updateLoadStatus(Entity entity) {
      }

      // $FF: synthetic method
      public void updateLoadStatus(final Object entity) {
         this.updateLoadStatus((Entity)entity);
      }

      // $FF: synthetic method
      public void stopTracking(final Object entity) {
         this.stopTracking((Entity)entity);
      }

      // $FF: synthetic method
      public void startTracking(final Object entity) {
         this.startTracking((Entity)entity);
      }

      // $FF: synthetic method
      public void startTicking(final Object entity) {
         this.startTicking((Entity)entity);
      }

      // $FF: synthetic method
      public void destroy(final Object entity) {
         this.destroy((Entity)entity);
      }

      // $FF: synthetic method
      public void create(final Object entity) {
         this.create((Entity)entity);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Properties implements MutableWorldProperties {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean flatWorld;
      private BlockPos spawnPos;
      private float spawnAngle;
      private long time;
      private long timeOfDay;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public Properties(Difficulty difficulty, boolean hardcore, boolean flatWorld) {
         this.difficulty = difficulty;
         this.hardcore = hardcore;
         this.flatWorld = flatWorld;
         this.gameRules = new GameRules();
      }

      public BlockPos getSpawnPos() {
         return this.spawnPos;
      }

      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      public long getTime() {
         return this.time;
      }

      public long getTimeOfDay() {
         return this.timeOfDay;
      }

      public void setTime(long time) {
         this.time = time;
      }

      public void setTimeOfDay(long timeOfDay) {
         this.timeOfDay = timeOfDay;
      }

      public void setSpawnPos(BlockPos pos, float angle) {
         this.spawnPos = pos.toImmutable();
         this.spawnAngle = angle;
      }

      public boolean isThundering() {
         return false;
      }

      public boolean isRaining() {
         return this.raining;
      }

      public void setRaining(boolean raining) {
         this.raining = raining;
      }

      public boolean isHardcore() {
         return this.hardcore;
      }

      public GameRules getGameRules() {
         return this.gameRules;
      }

      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
         super.populateCrashReport(reportSection, world);
      }

      public void setDifficulty(Difficulty difficulty) {
         this.difficulty = difficulty;
      }

      public void setDifficultyLocked(boolean difficultyLocked) {
         this.difficultyLocked = difficultyLocked;
      }

      public double getSkyDarknessHeight(HeightLimitView world) {
         return this.flatWorld ? (double)world.getBottomY() : (double)63.0F;
      }

      public float getHorizonShadingRatio() {
         return this.flatWorld ? 1.0F : 0.03125F;
      }
   }
}
