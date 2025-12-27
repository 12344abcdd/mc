package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleGroup;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ParticleManager implements ResourceReloader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceFinder FINDER = ResourceFinder.json("particles");
   private static final Identifier PARTICLES_PATH = Identifier.ofVanilla("particles");
   private static final int MAX_PARTICLE_COUNT = 16384;
   private static final List PARTICLE_TEXTURE_SHEETS;
   protected ClientWorld world;
   private final Map particles = Maps.newIdentityHashMap();
   private final Queue newEmitterParticles = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final Random random = Random.create();
   private final Int2ObjectMap factories = new Int2ObjectOpenHashMap();
   private final Queue newParticles = Queues.newArrayDeque();
   private final Map spriteAwareFactories = Maps.newHashMap();
   private final SpriteAtlasTexture particleAtlasTexture;
   private final Object2IntOpenHashMap groupCounts = new Object2IntOpenHashMap();

   public ParticleManager(ClientWorld world, TextureManager textureManager) {
      this.particleAtlasTexture = new SpriteAtlasTexture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
      textureManager.registerTexture(this.particleAtlasTexture.getId(), this.particleAtlasTexture);
      this.world = world;
      this.textureManager = textureManager;
      this.registerDefaultFactories();
   }

   private void registerDefaultFactories() {
      this.registerFactory(ParticleTypes.ANGRY_VILLAGER, (SpriteAwareFactory)(EmotionParticle.AngryVillagerFactory::new));
      this.registerFactory(ParticleTypes.BLOCK_MARKER, (ParticleFactory)(new BlockMarkerParticle.Factory()));
      this.registerFactory(ParticleTypes.BLOCK, (ParticleFactory)(new BlockDustParticle.Factory()));
      this.registerFactory(ParticleTypes.BUBBLE, (SpriteAwareFactory)(WaterBubbleParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, (SpriteAwareFactory)(BubbleColumnUpParticle.Factory::new));
      this.registerFactory(ParticleTypes.BUBBLE_POP, (SpriteAwareFactory)(BubblePopParticle.Factory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, (SpriteAwareFactory)(CampfireSmokeParticle.CosySmokeFactory::new));
      this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, (SpriteAwareFactory)(CampfireSmokeParticle.SignalSmokeFactory::new));
      this.registerFactory(ParticleTypes.CLOUD, (SpriteAwareFactory)(CloudParticle.CloudFactory::new));
      this.registerFactory(ParticleTypes.COMPOSTER, (SpriteAwareFactory)(SuspendParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIT, (SpriteAwareFactory)(DamageParticle.Factory::new));
      this.registerFactory(ParticleTypes.CURRENT_DOWN, (SpriteAwareFactory)(CurrentDownParticle.Factory::new));
      this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, (SpriteAwareFactory)(DamageParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.DRAGON_BREATH, (SpriteAwareFactory)(DragonBreathParticle.Factory::new));
      this.registerFactory(ParticleTypes.DOLPHIN, (SpriteAwareFactory)(SuspendParticle.DolphinFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_LAVA, BlockLeakParticle::createDrippingLava);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_LAVA, BlockLeakParticle::createFallingLava);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_LAVA, BlockLeakParticle::createLandingLava);
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_WATER, BlockLeakParticle::createDrippingWater);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_WATER, BlockLeakParticle::createFallingWater);
      this.registerFactory(ParticleTypes.DUST, RedDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Factory::new);
      this.registerFactory(ParticleTypes.EFFECT, (SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.ELDER_GUARDIAN, (ParticleFactory)(new ElderGuardianAppearanceParticle.Factory()));
      this.registerFactory(ParticleTypes.ENCHANTED_HIT, (SpriteAwareFactory)(DamageParticle.EnchantedHitFactory::new));
      this.registerFactory(ParticleTypes.ENCHANT, (SpriteAwareFactory)(ConnectionParticle.EnchantFactory::new));
      this.registerFactory(ParticleTypes.END_ROD, (SpriteAwareFactory)(EndRodParticle.Factory::new));
      this.registerFactory(ParticleTypes.ENTITY_EFFECT, SpellParticle.EntityFactory::new);
      this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, (ParticleFactory)(new ExplosionEmitterParticle.Factory()));
      this.registerFactory(ParticleTypes.EXPLOSION, (SpriteAwareFactory)(ExplosionLargeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SONIC_BOOM, (SpriteAwareFactory)(SonicBoomParticle.Factory::new));
      this.registerFactory(ParticleTypes.FALLING_DUST, BlockFallingDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.GUST, (SpriteAwareFactory)(GustParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMALL_GUST, (SpriteAwareFactory)(GustParticle.SmallGustFactory::new));
      this.registerFactory(ParticleTypes.GUST_EMITTER_LARGE, (ParticleFactory)(new GustEmitterParticle.Factory((double)3.0F, 7, 0)));
      this.registerFactory(ParticleTypes.GUST_EMITTER_SMALL, (ParticleFactory)(new GustEmitterParticle.Factory((double)1.0F, 3, 2)));
      this.registerFactory(ParticleTypes.FIREWORK, (SpriteAwareFactory)(FireworksSparkParticle.ExplosionFactory::new));
      this.registerFactory(ParticleTypes.FISHING, (SpriteAwareFactory)(FishingParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLAME, (SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.INFESTED, (SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.SCULK_SOUL, (SpriteAwareFactory)(SoulParticle.SculkSoulFactory::new));
      this.registerFactory(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Factory::new);
      this.registerFactory(ParticleTypes.SCULK_CHARGE_POP, (SpriteAwareFactory)(SculkChargePopParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL, (SpriteAwareFactory)(SoulParticle.Factory::new));
      this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, (SpriteAwareFactory)(FlameParticle.Factory::new));
      this.registerFactory(ParticleTypes.FLASH, (SpriteAwareFactory)(FireworksSparkParticle.FlashFactory::new));
      this.registerFactory(ParticleTypes.HAPPY_VILLAGER, (SpriteAwareFactory)(SuspendParticle.HappyVillagerFactory::new));
      this.registerFactory(ParticleTypes.HEART, (SpriteAwareFactory)(EmotionParticle.HeartFactory::new));
      this.registerFactory(ParticleTypes.INSTANT_EFFECT, (SpriteAwareFactory)(SpellParticle.InstantFactory::new));
      this.registerFactory(ParticleTypes.ITEM, (ParticleFactory)(new CrackParticle.ItemFactory()));
      this.registerFactory(ParticleTypes.ITEM_SLIME, (ParticleFactory)(new CrackParticle.SlimeballFactory()));
      this.registerFactory(ParticleTypes.ITEM_COBWEB, (ParticleFactory)(new CrackParticle.CobwebFactory()));
      this.registerFactory(ParticleTypes.ITEM_SNOWBALL, (ParticleFactory)(new CrackParticle.SnowballFactory()));
      this.registerFactory(ParticleTypes.LARGE_SMOKE, (SpriteAwareFactory)(LargeFireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.LAVA, (SpriteAwareFactory)(LavaEmberParticle.Factory::new));
      this.registerFactory(ParticleTypes.MYCELIUM, (SpriteAwareFactory)(SuspendParticle.MyceliumFactory::new));
      this.registerFactory(ParticleTypes.NAUTILUS, (SpriteAwareFactory)(ConnectionParticle.NautilusFactory::new));
      this.registerFactory(ParticleTypes.NOTE, (SpriteAwareFactory)(NoteParticle.Factory::new));
      this.registerFactory(ParticleTypes.POOF, (SpriteAwareFactory)(ExplosionSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.PORTAL, (SpriteAwareFactory)(PortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.RAIN, (SpriteAwareFactory)(RainSplashParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMOKE, (SpriteAwareFactory)(FireSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.WHITE_SMOKE, (SpriteAwareFactory)(WhiteSmokeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SNEEZE, (SpriteAwareFactory)(CloudParticle.SneezeFactory::new));
      this.registerFactory(ParticleTypes.SNOWFLAKE, (SpriteAwareFactory)(SnowflakeParticle.Factory::new));
      this.registerFactory(ParticleTypes.SPIT, (SpriteAwareFactory)(SpitParticle.Factory::new));
      this.registerFactory(ParticleTypes.SWEEP_ATTACK, (SpriteAwareFactory)(SweepAttackParticle.Factory::new));
      this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, (SpriteAwareFactory)(TotemParticle.Factory::new));
      this.registerFactory(ParticleTypes.SQUID_INK, (SpriteAwareFactory)(SquidInkParticle.Factory::new));
      this.registerFactory(ParticleTypes.UNDERWATER, (SpriteAwareFactory)(WaterSuspendParticle.UnderwaterFactory::new));
      this.registerFactory(ParticleTypes.SPLASH, (SpriteAwareFactory)(WaterSplashParticle.SplashFactory::new));
      this.registerFactory(ParticleTypes.WITCH, (SpriteAwareFactory)(SpellParticle.WitchFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_HONEY, BlockLeakParticle::createDrippingHoney);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_HONEY, BlockLeakParticle::createFallingHoney);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_HONEY, BlockLeakParticle::createLandingHoney);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_NECTAR, BlockLeakParticle::createFallingNectar);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_SPORE_BLOSSOM, BlockLeakParticle::createFallingSporeBlossom);
      this.registerFactory(ParticleTypes.SPORE_BLOSSOM_AIR, (SpriteAwareFactory)(WaterSuspendParticle.SporeBlossomAirFactory::new));
      this.registerFactory(ParticleTypes.ASH, (SpriteAwareFactory)(AshParticle.Factory::new));
      this.registerFactory(ParticleTypes.CRIMSON_SPORE, (SpriteAwareFactory)(WaterSuspendParticle.CrimsonSporeFactory::new));
      this.registerFactory(ParticleTypes.WARPED_SPORE, (SpriteAwareFactory)(WaterSuspendParticle.WarpedSporeFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, BlockLeakParticle::createDrippingObsidianTear);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, BlockLeakParticle::createFallingObsidianTear);
      this.registerBlockLeakFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, BlockLeakParticle::createLandingObsidianTear);
      this.registerFactory(ParticleTypes.REVERSE_PORTAL, (SpriteAwareFactory)(ReversePortalParticle.Factory::new));
      this.registerFactory(ParticleTypes.WHITE_ASH, (SpriteAwareFactory)(WhiteAshParticle.Factory::new));
      this.registerFactory(ParticleTypes.SMALL_FLAME, (SpriteAwareFactory)(FlameParticle.SmallFactory::new));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_WATER, BlockLeakParticle::createDrippingDripstoneWater);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_WATER, BlockLeakParticle::createFallingDripstoneWater);
      this.registerFactory(ParticleTypes.CHERRY_LEAVES, (SpriteAwareFactory)((spriteProvider) -> (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> new CherryLeavesParticle(world, x, y, z, spriteProvider)));
      this.registerBlockLeakFactory(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, BlockLeakParticle::createDrippingDripstoneLava);
      this.registerBlockLeakFactory(ParticleTypes.FALLING_DRIPSTONE_LAVA, BlockLeakParticle::createFallingDripstoneLava);
      this.registerFactory(ParticleTypes.VIBRATION, VibrationParticle.Factory::new);
      this.registerFactory(ParticleTypes.GLOW_SQUID_INK, (SpriteAwareFactory)(SquidInkParticle.GlowSquidInkFactory::new));
      this.registerFactory(ParticleTypes.GLOW, (SpriteAwareFactory)(GlowParticle.GlowFactory::new));
      this.registerFactory(ParticleTypes.WAX_ON, (SpriteAwareFactory)(GlowParticle.WaxOnFactory::new));
      this.registerFactory(ParticleTypes.WAX_OFF, (SpriteAwareFactory)(GlowParticle.WaxOffFactory::new));
      this.registerFactory(ParticleTypes.ELECTRIC_SPARK, (SpriteAwareFactory)(GlowParticle.ElectricSparkFactory::new));
      this.registerFactory(ParticleTypes.SCRAPE, (SpriteAwareFactory)(GlowParticle.ScrapeFactory::new));
      this.registerFactory(ParticleTypes.SHRIEK, ShriekParticle.Factory::new);
      this.registerFactory(ParticleTypes.EGG_CRACK, (SpriteAwareFactory)(SuspendParticle.EggCrackFactory::new));
      this.registerFactory(ParticleTypes.DUST_PLUME, (SpriteAwareFactory)(DustPlumeParticle.Factory::new));
      this.registerFactory(ParticleTypes.TRIAL_SPAWNER_DETECTION, (SpriteAwareFactory)(TrialSpawnerDetectionParticle.Factory::new));
      this.registerFactory(ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS, (SpriteAwareFactory)(TrialSpawnerDetectionParticle.Factory::new));
      this.registerFactory(ParticleTypes.VAULT_CONNECTION, (SpriteAwareFactory)(ConnectionParticle.VaultConnectionFactory::new));
      this.registerFactory(ParticleTypes.DUST_PILLAR, (ParticleFactory)(new BlockDustParticle.DustPillarFactory()));
      this.registerFactory(ParticleTypes.RAID_OMEN, (SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.TRIAL_OMEN, (SpriteAwareFactory)(SpellParticle.DefaultFactory::new));
      this.registerFactory(ParticleTypes.OMINOUS_SPAWNING, (SpriteAwareFactory)(OminousSpawningParticle.Factory::new));
   }

   private void registerFactory(ParticleType type, ParticleFactory factory) {
      this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), factory);
   }

   private void registerBlockLeakFactory(ParticleType type, ParticleFactory.BlockLeakParticleFactory factory) {
      this.registerFactory(type, (SpriteAwareFactory)((spriteBillboardParticle) -> (type, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle spriteBillboardParticle = factory.createParticle(type, world, x, y, z, velocityX, velocityY, velocityZ);
            if (spriteBillboardParticle != null) {
               spriteBillboardParticle.setSprite(spriteBillboardParticle);
            }

            return spriteBillboardParticle;
         }));
   }

   private void registerFactory(ParticleType type, SpriteAwareFactory factory) {
      SimpleSpriteProvider simpleSpriteProvider = new SimpleSpriteProvider();
      this.spriteAwareFactories.put(Registries.PARTICLE_TYPE.getId(type), simpleSpriteProvider);
      this.factories.put(Registries.PARTICLE_TYPE.getRawId(type), factory.create(simpleSpriteProvider));
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture<List<ReloadResult>> completableFuture = CompletableFuture.supplyAsync(() -> FINDER.findResources(manager), prepareExecutor).thenCompose((particles) -> {
         List<CompletableFuture<ReloadResult>> list = new ArrayList(particles.size());
         particles.forEach((id, resource) -> {
            Identifier identifier = FINDER.toResourceId(id);
            list.add(CompletableFuture.supplyAsync(() -> {
               @Environment(EnvType.CLIENT)
               record ReloadResult(Identifier id, Optional sprites) {
               }

               return new ReloadResult(identifier, this.loadTextureList(identifier, resource));
            }, prepareExecutor));
         });
         return Util.combineSafe(list);
      });
      CompletableFuture<SpriteLoader.StitchResult> completableFuture2 = SpriteLoader.fromAtlas(this.particleAtlasTexture).load(manager, PARTICLES_PATH, 0, prepareExecutor).thenCompose(SpriteLoader.StitchResult::whenComplete);
      CompletableFuture var10000 = CompletableFuture.allOf(completableFuture2, completableFuture);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((void_) -> {
         this.clearParticles();
         applyProfiler.startTick();
         applyProfiler.push("upload");
         SpriteLoader.StitchResult stitchResult = (SpriteLoader.StitchResult)completableFuture2.join();
         this.particleAtlasTexture.upload(stitchResult);
         applyProfiler.swap("bindSpriteSets");
         Set<Identifier> set = new HashSet();
         Sprite sprite = stitchResult.missing();
         ((List)completableFuture.join()).forEach((result) -> {
            Optional<List<Identifier>> optional = result.sprites();
            if (!optional.isEmpty()) {
               List<Sprite> list = new ArrayList();

               for(Identifier identifier : (List)optional.get()) {
                  Sprite sprite2 = (Sprite)stitchResult.regions().get(identifier);
                  if (sprite2 == null) {
                     set.add(identifier);
                     list.add(sprite);
                  } else {
                     list.add(sprite2);
                  }
               }

               if (list.isEmpty()) {
                  list.add(sprite);
               }

               ((SimpleSpriteProvider)this.spriteAwareFactories.get(result.id())).setSprites(list);
            }
         });
         if (!set.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(Identifier::toString).collect(Collectors.joining(",")));
         }

         applyProfiler.pop();
         applyProfiler.endTick();
      }, applyExecutor);
   }

   public void clearAtlas() {
      this.particleAtlasTexture.clear();
   }

   private Optional loadTextureList(Identifier id, Resource resource) {
      if (!this.spriteAwareFactories.containsKey(id)) {
         LOGGER.debug("Redundant texture list for particle: {}", id);
         return Optional.empty();
      } else {
         try {
            Reader reader = resource.getReader();

            Optional var5;
            try {
               ParticleTextureData particleTextureData = ParticleTextureData.load(JsonHelper.deserialize(reader));
               var5 = Optional.of(particleTextureData.getTextureList());
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return var5;
         } catch (IOException iOException) {
            throw new IllegalStateException("Failed to load description for particle " + String.valueOf(id), iOException);
         }
      }
   }

   public void addEmitter(Entity entity, ParticleEffect parameters) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters));
   }

   public void addEmitter(Entity entity, ParticleEffect parameters, int maxAge) {
      this.newEmitterParticles.add(new EmitterParticle(this.world, entity, parameters, maxAge));
   }

   @Nullable
   public Particle addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      Particle particle = this.createParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
      if (particle != null) {
         this.addParticle(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private Particle createParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      ParticleFactory<T> particleFactory = (ParticleFactory)this.factories.get(Registries.PARTICLE_TYPE.getRawId(parameters.getType()));
      return particleFactory == null ? null : particleFactory.createParticle(parameters, this.world, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(Particle particle) {
      Optional<ParticleGroup> optional = particle.getGroup();
      if (optional.isPresent()) {
         if (this.canAdd((ParticleGroup)optional.get())) {
            this.newParticles.add(particle);
            this.addTo((ParticleGroup)optional.get(), 1);
         }
      } else {
         this.newParticles.add(particle);
      }

   }

   public void tick() {
      this.particles.forEach((sheet, queue) -> {
         this.world.getProfiler().push(sheet.toString());
         this.tickParticles(queue);
         this.world.getProfiler().pop();
      });
      if (!this.newEmitterParticles.isEmpty()) {
         List<EmitterParticle> list = Lists.newArrayList();

         for(EmitterParticle emitterParticle : this.newEmitterParticles) {
            emitterParticle.tick();
            if (!emitterParticle.isAlive()) {
               list.add(emitterParticle);
            }
         }

         this.newEmitterParticles.removeAll(list);
      }

      Particle particle;
      if (!this.newParticles.isEmpty()) {
         while((particle = (Particle)this.newParticles.poll()) != null) {
            ((Queue)this.particles.computeIfAbsent(particle.getType(), (sheet) -> EvictingQueue.create(16384))).add(particle);
         }
      }

   }

   private void tickParticles(Collection particles) {
      if (!particles.isEmpty()) {
         Iterator<Particle> iterator = particles.iterator();

         while(iterator.hasNext()) {
            Particle particle = (Particle)iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               particle.getGroup().ifPresent((group) -> this.addTo(group, -1));
               iterator.remove();
            }
         }
      }

   }

   private void addTo(ParticleGroup group, int count) {
      this.groupCounts.addTo(group, count);
   }

   private void tickParticle(Particle particle) {
      try {
         particle.tick();
      } catch (Throwable throwable) {
         CrashReport crashReport = CrashReport.create(throwable, "Ticking Particle");
         CrashReportSection crashReportSection = crashReport.addElement("Particle being ticked");
         Objects.requireNonNull(particle);
         crashReportSection.add("Particle", particle::toString);
         ParticleTextureSheet var10002 = particle.getType();
         Objects.requireNonNull(var10002);
         crashReportSection.add("Particle Type", var10002::toString);
         throw new CrashException(crashReport);
      }
   }

   public void renderParticles(LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta) {
      lightmapTextureManager.enable();
      RenderSystem.enableDepthTest();

      for(ParticleTextureSheet particleTextureSheet : PARTICLE_TEXTURE_SHEETS) {
         Queue<Particle> queue = (Queue)this.particles.get(particleTextureSheet);
         if (queue != null && !queue.isEmpty()) {
            RenderSystem.setShader(GameRenderer::getParticleProgram);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = particleTextureSheet.begin(tessellator, this.textureManager);
            if (bufferBuilder != null) {
               for(Particle particle : queue) {
                  try {
                     particle.buildGeometry(bufferBuilder, camera, tickDelta);
                  } catch (Throwable throwable) {
                     CrashReport crashReport = CrashReport.create(throwable, "Rendering Particle");
                     CrashReportSection crashReportSection = crashReport.addElement("Particle being rendered");
                     Objects.requireNonNull(particle);
                     crashReportSection.add("Particle", particle::toString);
                     Objects.requireNonNull(particleTextureSheet);
                     crashReportSection.add("Particle Type", particleTextureSheet::toString);
                     throw new CrashException(crashReport);
                  }
               }

               BuiltBuffer builtBuffer = bufferBuilder.endNullable();
               if (builtBuffer != null) {
                  BufferRenderer.drawWithGlobalProgram(builtBuffer);
               }
            }
         }
      }

      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      lightmapTextureManager.disable();
   }

   public void setWorld(@Nullable ClientWorld world) {
      this.world = world;
      this.clearParticles();
      this.newEmitterParticles.clear();
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
      if (!state.isAir() && state.hasBlockBreakParticles()) {
         VoxelShape voxelShape = state.getOutlineShape(this.world, pos);
         double d = (double)0.25F;
         voxelShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double d = Math.min((double)1.0F, maxX - minX);
            double e = Math.min((double)1.0F, maxY - minY);
            double f = Math.min((double)1.0F, maxZ - minZ);
            int i = Math.max(2, MathHelper.ceil(d / (double)0.25F));
            int j = Math.max(2, MathHelper.ceil(e / (double)0.25F));
            int k = Math.max(2, MathHelper.ceil(f / (double)0.25F));

            for(int l = 0; l < i; ++l) {
               for(int m = 0; m < j; ++m) {
                  for(int n = 0; n < k; ++n) {
                     double g = ((double)l + (double)0.5F) / (double)i;
                     double h = ((double)m + (double)0.5F) / (double)j;
                     double o = ((double)n + (double)0.5F) / (double)k;
                     double p = g * d + minX;
                     double q = h * e + minY;
                     double r = o * f + minZ;
                     this.addParticle(new BlockDustParticle(this.world, (double)pos.getX() + p, (double)pos.getY() + q, (double)pos.getZ() + r, g - (double)0.5F, h - (double)0.5F, o - (double)0.5F, state, pos));
                  }
               }
            }

         });
      }
   }

   public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
      BlockState blockState = this.world.getBlockState(pos);
      if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.hasBlockBreakParticles()) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         Box box = blockState.getOutlineShape(this.world, pos).getBoundingBox();
         double d = (double)i + this.random.nextDouble() * (box.maxX - box.minX - (double)0.2F) + (double)0.1F + box.minX;
         double e = (double)j + this.random.nextDouble() * (box.maxY - box.minY - (double)0.2F) + (double)0.1F + box.minY;
         double g = (double)k + this.random.nextDouble() * (box.maxZ - box.minZ - (double)0.2F) + (double)0.1F + box.minZ;
         if (direction == Direction.DOWN) {
            e = (double)j + box.minY - (double)0.1F;
         }

         if (direction == Direction.UP) {
            e = (double)j + box.maxY + (double)0.1F;
         }

         if (direction == Direction.NORTH) {
            g = (double)k + box.minZ - (double)0.1F;
         }

         if (direction == Direction.SOUTH) {
            g = (double)k + box.maxZ + (double)0.1F;
         }

         if (direction == Direction.WEST) {
            d = (double)i + box.minX - (double)0.1F;
         }

         if (direction == Direction.EAST) {
            d = (double)i + box.maxX + (double)0.1F;
         }

         this.addParticle((new BlockDustParticle(this.world, d, e, g, (double)0.0F, (double)0.0F, (double)0.0F, blockState, pos)).move(0.2F).scale(0.6F));
      }
   }

   public String getDebugString() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   private boolean canAdd(ParticleGroup group) {
      return this.groupCounts.getInt(group) < group.getMaxCount();
   }

   private void clearParticles() {
      this.particles.clear();
      this.newParticles.clear();
      this.newEmitterParticles.clear();
      this.groupCounts.clear();
   }

   static {
      PARTICLE_TEXTURE_SHEETS = ImmutableList.of(ParticleTextureSheet.TERRAIN_SHEET, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE, ParticleTextureSheet.PARTICLE_SHEET_LIT, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT, ParticleTextureSheet.CUSTOM);
   }

   @Environment(EnvType.CLIENT)
   static class SimpleSpriteProvider implements SpriteProvider {
      private List sprites;

      public Sprite getSprite(int age, int maxAge) {
         return (Sprite)this.sprites.get(age * (this.sprites.size() - 1) / maxAge);
      }

      public Sprite getSprite(Random random) {
         return (Sprite)this.sprites.get(random.nextInt(this.sprites.size()));
      }

      public void setSprites(List sprites) {
         this.sprites = ImmutableList.copyOf(sprites);
      }
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   interface SpriteAwareFactory {
      ParticleFactory create(SpriteProvider spriteProvider);
   }
}
