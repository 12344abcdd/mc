package net.minecraft.client.option;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class GameOptions {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Gson GSON = new Gson();
   private static final TypeToken STRING_LIST_TYPE = new TypeToken() {
   };
   public static final int field_32149 = 2;
   public static final int field_32150 = 4;
   public static final int field_32152 = 8;
   public static final int field_32153 = 12;
   public static final int field_32154 = 16;
   public static final int field_32155 = 32;
   private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
   public static final String EMPTY_STRING = "";
   private static final Text DARK_MOJANG_STUDIOS_BACKGROUND_COLOR_TOOLTIP = Text.translatable("options.darkMojangStudiosBackgroundColor.tooltip");
   private final SimpleOption monochromeLogo;
   private static final Text HIDE_LIGHTNING_FLASHES_TOOLTIP = Text.translatable("options.hideLightningFlashes.tooltip");
   private final SimpleOption hideLightningFlashes;
   private static final Text HIDE_SPLASH_TEXTS_TOOLTIP = Text.translatable("options.hideSplashTexts.tooltip");
   private final SimpleOption hideSplashTexts;
   private final SimpleOption mouseSensitivity;
   private final SimpleOption viewDistance;
   private final SimpleOption simulationDistance;
   private int serverViewDistance;
   private final SimpleOption entityDistanceScaling;
   public static final int MAX_FRAMERATE = 260;
   private final SimpleOption maxFps;
   private final SimpleOption cloudRenderMode;
   private static final Text FAST_GRAPHICS_TOOLTIP = Text.translatable("options.graphics.fast.tooltip");
   private static final Text FABULOUS_GRAPHICS_TOOLTIP;
   private static final Text FANCY_GRAPHICS_TOOLTIP;
   private final SimpleOption graphicsMode;
   private final SimpleOption ao;
   private static final Text NONE_CHUNK_BUILDER_MODE_TOOLTIP;
   private static final Text BY_PLAYER_CHUNK_BUILDER_MODE_TOOLTIP;
   private static final Text NEARBY_CHUNK_BUILDER_MODE_TOOLTIP;
   private final SimpleOption chunkBuilderMode;
   public List resourcePacks;
   public List incompatibleResourcePacks;
   private final SimpleOption chatVisibility;
   private final SimpleOption chatOpacity;
   private final SimpleOption chatLineSpacing;
   private static final Text MENU_BACKGROUND_BLURRINESS_TOOLTIP;
   private static final int DEFAULT_MENU_BACKGROUND_BLURRINESS = 5;
   private final SimpleOption menuBackgroundBlurriness;
   private final SimpleOption textBackgroundOpacity;
   private final SimpleOption panoramaSpeed;
   private static final Text HIGH_CONTRAST_TOOLTIP;
   private final SimpleOption highContrast;
   private final SimpleOption narratorHotkey;
   @Nullable
   public String fullscreenResolution;
   public boolean hideServerAddress;
   public boolean advancedItemTooltips;
   public boolean pauseOnLostFocus;
   private final Set enabledPlayerModelParts;
   private final SimpleOption mainArm;
   public int overrideWidth;
   public int overrideHeight;
   private final SimpleOption chatScale;
   private final SimpleOption chatWidth;
   private final SimpleOption chatHeightUnfocused;
   private final SimpleOption chatHeightFocused;
   private final SimpleOption chatDelay;
   private static final Text NOTIFICATION_DISPLAY_TIME_TOOLTIP;
   private final SimpleOption notificationDisplayTime;
   private final SimpleOption mipmapLevels;
   public boolean useNativeTransport;
   private final SimpleOption attackIndicator;
   public TutorialStep tutorialStep;
   public boolean joinedFirstServer;
   public boolean hideBundleTutorial;
   private final SimpleOption biomeBlendRadius;
   private final SimpleOption mouseWheelSensitivity;
   private final SimpleOption rawMouseInput;
   public int glDebugVerbosity;
   private final SimpleOption autoJump;
   private final SimpleOption operatorItemsTab;
   private final SimpleOption autoSuggestions;
   private final SimpleOption chatColors;
   private final SimpleOption chatLinks;
   private final SimpleOption chatLinksPrompt;
   private final SimpleOption enableVsync;
   private final SimpleOption entityShadows;
   private final SimpleOption forceUnicodeFont;
   private final SimpleOption japaneseGlyphVariants;
   private final SimpleOption invertYMouse;
   private final SimpleOption discreteMouseScroll;
   private static final Text REALMS_NOTIFICATIONS_TOOLTIP;
   private final SimpleOption realmsNotifications;
   private static final Text ALLOW_SERVER_LISTING_TOOLTIP;
   private final SimpleOption allowServerListing;
   private final SimpleOption reducedDebugInfo;
   private final Map soundVolumeLevels;
   private final SimpleOption showSubtitles;
   private static final Text DIRECTIONAL_AUDIO_ON_TOOLTIP;
   private static final Text DIRECTIONAL_AUDIO_OFF_TOOLTIP;
   private final SimpleOption directionalAudio;
   private final SimpleOption backgroundForChatOnly;
   private final SimpleOption touchscreen;
   private final SimpleOption fullscreen;
   private final SimpleOption bobView;
   private static final Text TOGGLE_KEY_TEXT;
   private static final Text HOLD_KEY_TEXT;
   private final SimpleOption sneakToggled;
   private final SimpleOption sprintToggled;
   public boolean skipMultiplayerWarning;
   private static final Text HIDE_MATCHED_NAMES_TOOLTIP;
   private final SimpleOption hideMatchedNames;
   private final SimpleOption showAutosaveIndicator;
   private static final Text ONLY_SHOW_SECURE_CHAT_TOOLTIP;
   private final SimpleOption onlyShowSecureChat;
   public final KeyBinding forwardKey;
   public final KeyBinding leftKey;
   public final KeyBinding backKey;
   public final KeyBinding rightKey;
   public final KeyBinding jumpKey;
   public final KeyBinding sneakKey;
   public final KeyBinding sprintKey;
   public final KeyBinding inventoryKey;
   public final KeyBinding swapHandsKey;
   public final KeyBinding dropKey;
   public final KeyBinding useKey;
   public final KeyBinding attackKey;
   public final KeyBinding pickItemKey;
   public final KeyBinding chatKey;
   public final KeyBinding playerListKey;
   public final KeyBinding commandKey;
   public final KeyBinding socialInteractionsKey;
   public final KeyBinding screenshotKey;
   public final KeyBinding togglePerspectiveKey;
   public final KeyBinding smoothCameraKey;
   public final KeyBinding fullscreenKey;
   public final KeyBinding spectatorOutlinesKey;
   public final KeyBinding advancementsKey;
   public final KeyBinding[] hotbarKeys;
   public final KeyBinding saveToolbarActivatorKey;
   public final KeyBinding loadToolbarActivatorKey;
   public final KeyBinding[] allKeys;
   protected MinecraftClient client;
   private final File optionsFile;
   public boolean hudHidden;
   private Perspective perspective;
   public String lastServer;
   public boolean smoothCameraEnabled;
   private final SimpleOption fov;
   private static final Text TELEMETRY_TOOLTIP;
   private final SimpleOption telemetryOptInExtra;
   private static final Text SCREEN_EFFECT_SCALE_TOOLTIP;
   private final SimpleOption distortionEffectScale;
   private static final Text FOV_EFFECT_SCALE_TOOLTIP;
   private final SimpleOption fovEffectScale;
   private static final Text DARKNESS_EFFECT_SCALE_TOOLTIP;
   private final SimpleOption darknessEffectScale;
   private static final Text GLINT_SPEED_TOOLTIP;
   private final SimpleOption glintSpeed;
   private static final Text GLINT_STRENGTH_TOOLTIP;
   private final SimpleOption glintStrength;
   private static final Text DAMAGE_TILT_STRENGTH_TOOLTIP;
   private final SimpleOption damageTiltStrength;
   private final SimpleOption gamma;
   public static final int field_43405 = 0;
   private static final int MAX_SERIALIZABLE_GUI_SCALE = 2147483646;
   private final SimpleOption guiScale;
   private final SimpleOption particles;
   private final SimpleOption narrator;
   public String language;
   private final SimpleOption soundDevice;
   public boolean onboardAccessibility;
   public boolean syncChunkWrites;

   public SimpleOption getMonochromeLogo() {
      return this.monochromeLogo;
   }

   public SimpleOption getHideLightningFlashes() {
      return this.hideLightningFlashes;
   }

   public SimpleOption getHideSplashTexts() {
      return this.hideSplashTexts;
   }

   public SimpleOption getMouseSensitivity() {
      return this.mouseSensitivity;
   }

   public SimpleOption getViewDistance() {
      return this.viewDistance;
   }

   public SimpleOption getSimulationDistance() {
      return this.simulationDistance;
   }

   public SimpleOption getEntityDistanceScaling() {
      return this.entityDistanceScaling;
   }

   public SimpleOption getMaxFps() {
      return this.maxFps;
   }

   public SimpleOption getCloudRenderMode() {
      return this.cloudRenderMode;
   }

   public SimpleOption getGraphicsMode() {
      return this.graphicsMode;
   }

   public SimpleOption getAo() {
      return this.ao;
   }

   public SimpleOption getChunkBuilderMode() {
      return this.chunkBuilderMode;
   }

   public void refreshResourcePacks(ResourcePackManager resourcePackManager) {
      List<String> list = ImmutableList.copyOf(this.resourcePacks);
      this.resourcePacks.clear();
      this.incompatibleResourcePacks.clear();

      for(ResourcePackProfile resourcePackProfile : resourcePackManager.getEnabledProfiles()) {
         if (!resourcePackProfile.isPinned()) {
            this.resourcePacks.add(resourcePackProfile.getId());
            if (!resourcePackProfile.getCompatibility().isCompatible()) {
               this.incompatibleResourcePacks.add(resourcePackProfile.getId());
            }
         }
      }

      this.write();
      List<String> list2 = ImmutableList.copyOf(this.resourcePacks);
      if (!list2.equals(list)) {
         this.client.reloadResources();
      }

   }

   public SimpleOption getChatVisibility() {
      return this.chatVisibility;
   }

   public SimpleOption getChatOpacity() {
      return this.chatOpacity;
   }

   public SimpleOption getChatLineSpacing() {
      return this.chatLineSpacing;
   }

   public SimpleOption getMenuBackgroundBlurriness() {
      return this.menuBackgroundBlurriness;
   }

   public int getMenuBackgroundBlurrinessValue() {
      return (Integer)this.getMenuBackgroundBlurriness().getValue();
   }

   public SimpleOption getTextBackgroundOpacity() {
      return this.textBackgroundOpacity;
   }

   public SimpleOption getPanoramaSpeed() {
      return this.panoramaSpeed;
   }

   public SimpleOption getHighContrast() {
      return this.highContrast;
   }

   public SimpleOption getNarratorHotkey() {
      return this.narratorHotkey;
   }

   public SimpleOption getMainArm() {
      return this.mainArm;
   }

   public SimpleOption getChatScale() {
      return this.chatScale;
   }

   public SimpleOption getChatWidth() {
      return this.chatWidth;
   }

   public SimpleOption getChatHeightUnfocused() {
      return this.chatHeightUnfocused;
   }

   public SimpleOption getChatHeightFocused() {
      return this.chatHeightFocused;
   }

   public SimpleOption getChatDelay() {
      return this.chatDelay;
   }

   public SimpleOption getNotificationDisplayTime() {
      return this.notificationDisplayTime;
   }

   public SimpleOption getMipmapLevels() {
      return this.mipmapLevels;
   }

   public SimpleOption getAttackIndicator() {
      return this.attackIndicator;
   }

   public SimpleOption getBiomeBlendRadius() {
      return this.biomeBlendRadius;
   }

   private static double toMouseWheelSensitivityValue(int value) {
      return Math.pow((double)10.0F, (double)value / (double)100.0F);
   }

   private static int toMouseWheelSensitivitySliderProgressValue(double value) {
      return MathHelper.floor(Math.log10(value) * (double)100.0F);
   }

   public SimpleOption getMouseWheelSensitivity() {
      return this.mouseWheelSensitivity;
   }

   public SimpleOption getRawMouseInput() {
      return this.rawMouseInput;
   }

   public SimpleOption getAutoJump() {
      return this.autoJump;
   }

   public SimpleOption getOperatorItemsTab() {
      return this.operatorItemsTab;
   }

   public SimpleOption getAutoSuggestions() {
      return this.autoSuggestions;
   }

   public SimpleOption getChatColors() {
      return this.chatColors;
   }

   public SimpleOption getChatLinks() {
      return this.chatLinks;
   }

   public SimpleOption getChatLinksPrompt() {
      return this.chatLinksPrompt;
   }

   public SimpleOption getEnableVsync() {
      return this.enableVsync;
   }

   public SimpleOption getEntityShadows() {
      return this.entityShadows;
   }

   private static void onFontOptionsChanged() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (minecraftClient.getWindow() != null) {
         minecraftClient.onFontOptionsChanged();
         minecraftClient.onResolutionChanged();
      }

   }

   public SimpleOption getForceUnicodeFont() {
      return this.forceUnicodeFont;
   }

   private static boolean shouldUseJapaneseGlyphsByDefault() {
      return Locale.getDefault().getLanguage().equalsIgnoreCase("ja");
   }

   public SimpleOption getJapaneseGlyphVariants() {
      return this.japaneseGlyphVariants;
   }

   public SimpleOption getInvertYMouse() {
      return this.invertYMouse;
   }

   public SimpleOption getDiscreteMouseScroll() {
      return this.discreteMouseScroll;
   }

   public SimpleOption getRealmsNotifications() {
      return this.realmsNotifications;
   }

   public SimpleOption getAllowServerListing() {
      return this.allowServerListing;
   }

   public SimpleOption getReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public final float getSoundVolume(SoundCategory category) {
      return ((Double)this.getSoundVolumeOption(category).getValue()).floatValue();
   }

   public final SimpleOption getSoundVolumeOption(SoundCategory category) {
      return (SimpleOption)Objects.requireNonNull((SimpleOption)this.soundVolumeLevels.get(category));
   }

   private SimpleOption createSoundVolumeOption(String key, SoundCategory category) {
      return new SimpleOption(key, SimpleOption.emptyTooltip(), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> MinecraftClient.getInstance().getSoundManager().updateSoundVolume(category, value.floatValue()));
   }

   public SimpleOption getShowSubtitles() {
      return this.showSubtitles;
   }

   public SimpleOption getDirectionalAudio() {
      return this.directionalAudio;
   }

   public SimpleOption getBackgroundForChatOnly() {
      return this.backgroundForChatOnly;
   }

   public SimpleOption getTouchscreen() {
      return this.touchscreen;
   }

   public SimpleOption getFullscreen() {
      return this.fullscreen;
   }

   public SimpleOption getBobView() {
      return this.bobView;
   }

   public SimpleOption getSneakToggled() {
      return this.sneakToggled;
   }

   public SimpleOption getSprintToggled() {
      return this.sprintToggled;
   }

   public SimpleOption getHideMatchedNames() {
      return this.hideMatchedNames;
   }

   public SimpleOption getShowAutosaveIndicator() {
      return this.showAutosaveIndicator;
   }

   public SimpleOption getOnlyShowSecureChat() {
      return this.onlyShowSecureChat;
   }

   public SimpleOption getFov() {
      return this.fov;
   }

   public SimpleOption getTelemetryOptInExtra() {
      return this.telemetryOptInExtra;
   }

   public SimpleOption getDistortionEffectScale() {
      return this.distortionEffectScale;
   }

   public SimpleOption getFovEffectScale() {
      return this.fovEffectScale;
   }

   public SimpleOption getDarknessEffectScale() {
      return this.darknessEffectScale;
   }

   public SimpleOption getGlintSpeed() {
      return this.glintSpeed;
   }

   public SimpleOption getGlintStrength() {
      return this.glintStrength;
   }

   public SimpleOption getDamageTiltStrength() {
      return this.damageTiltStrength;
   }

   public SimpleOption getGamma() {
      return this.gamma;
   }

   public SimpleOption getGuiScale() {
      return this.guiScale;
   }

   public SimpleOption getParticles() {
      return this.particles;
   }

   public SimpleOption getNarrator() {
      return this.narrator;
   }

   public SimpleOption getSoundDevice() {
      return this.soundDevice;
   }

   public void setAccessibilityOnboarded() {
      this.onboardAccessibility = false;
      this.write();
   }

   public GameOptions(MinecraftClient client, File optionsFile) {
      this.monochromeLogo = SimpleOption.ofBoolean("options.darkMojangStudiosBackgroundColor", SimpleOption.constantTooltip(DARK_MOJANG_STUDIOS_BACKGROUND_COLOR_TOOLTIP), false);
      this.hideLightningFlashes = SimpleOption.ofBoolean("options.hideLightningFlashes", SimpleOption.constantTooltip(HIDE_LIGHTNING_FLASHES_TOOLTIP), false);
      this.hideSplashTexts = SimpleOption.ofBoolean("options.hideSplashTexts", SimpleOption.constantTooltip(HIDE_SPLASH_TEXTS_TOOLTIP), false);
      this.mouseSensitivity = new SimpleOption("options.sensitivity", SimpleOption.emptyTooltip(), (optionText, value) -> {
         if (value == (double)0.0F) {
            return getGenericValueText(optionText, Text.translatable("options.sensitivity.min"));
         } else {
            return value == (double)1.0F ? getGenericValueText(optionText, Text.translatable("options.sensitivity.max")) : getPercentValueText(optionText, (double)2.0F * value);
         }
      }, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.5F, (value) -> {
      });
      this.serverViewDistance = 0;
      this.entityDistanceScaling = new SimpleOption("options.entityDistanceScaling", SimpleOption.emptyTooltip(), GameOptions::getPercentValueText, (new SimpleOption.ValidatingIntSliderCallbacks(2, 20)).withModifier((sliderProgressValue) -> (double)sliderProgressValue / (double)4.0F, (value) -> (int)(value * (double)4.0F)), Codec.doubleRange((double)0.5F, (double)5.0F), (double)1.0F, (value) -> {
      });
      this.maxFps = new SimpleOption("options.framerateLimit", SimpleOption.emptyTooltip(), (optionText, value) -> value == 260 ? getGenericValueText(optionText, Text.translatable("options.framerateLimit.max")) : getGenericValueText(optionText, Text.translatable("options.framerate", new Object[]{value})), (new SimpleOption.ValidatingIntSliderCallbacks(1, 26)).withModifier((value) -> value * 10, (value) -> value / 10), Codec.intRange(10, 260), 120, (value) -> MinecraftClient.getInstance().getWindow().setFramerateLimit(value));
      this.cloudRenderMode = new SimpleOption("options.renderClouds", SimpleOption.emptyTooltip(), SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(CloudRenderMode.values()), Codec.withAlternative(CloudRenderMode.CODEC, Codec.BOOL, (value) -> value ? CloudRenderMode.FANCY : CloudRenderMode.OFF)), CloudRenderMode.FANCY, (cloudRenderMode) -> {
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            Framebuffer framebuffer = MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer();
            if (framebuffer != null) {
               framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }
         }

      });
      this.graphicsMode = new SimpleOption("options.graphics", (value) -> {
         Tooltip var10000;
         switch (value) {
            case FANCY -> var10000 = Tooltip.of(FANCY_GRAPHICS_TOOLTIP);
            case FAST -> var10000 = Tooltip.of(FAST_GRAPHICS_TOOLTIP);
            case FABULOUS -> var10000 = Tooltip.of(FABULOUS_GRAPHICS_TOOLTIP);
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }, (optionText, value) -> {
         MutableText mutableText = Text.translatable(value.getTranslationKey());
         return value == GraphicsMode.FABULOUS ? mutableText.formatted(Formatting.ITALIC) : mutableText;
      }, new SimpleOption.AlternateValuesSupportingCyclingCallbacks(Arrays.asList(GraphicsMode.values()), (List)Stream.of(GraphicsMode.values()).filter((graphicsMode) -> graphicsMode != GraphicsMode.FABULOUS).collect(Collectors.toList()), () -> MinecraftClient.getInstance().isRunning() && MinecraftClient.getInstance().getVideoWarningManager().hasCancelledAfterWarning(), (option, graphicsMode) -> {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         VideoWarningManager videoWarningManager = minecraftClient.getVideoWarningManager();
         if (graphicsMode == GraphicsMode.FABULOUS && videoWarningManager.canWarn()) {
            videoWarningManager.scheduleWarning();
         } else {
            option.setValue(graphicsMode);
            minecraftClient.worldRenderer.reload();
         }
      }, Codec.INT.xmap(GraphicsMode::byId, GraphicsMode::getId)), GraphicsMode.FANCY, (value) -> {
      });
      this.ao = SimpleOption.ofBoolean("options.ao", true, (value) -> MinecraftClient.getInstance().worldRenderer.reload());
      this.chunkBuilderMode = new SimpleOption("options.prioritizeChunkUpdates", (value) -> {
         Tooltip var10000;
         switch (value) {
            case NONE -> var10000 = Tooltip.of(NONE_CHUNK_BUILDER_MODE_TOOLTIP);
            case PLAYER_AFFECTED -> var10000 = Tooltip.of(BY_PLAYER_CHUNK_BUILDER_MODE_TOOLTIP);
            case NEARBY -> var10000 = Tooltip.of(NEARBY_CHUNK_BUILDER_MODE_TOOLTIP);
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }, SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(ChunkBuilderMode.values()), Codec.INT.xmap(ChunkBuilderMode::get, ChunkBuilderMode::getId)), ChunkBuilderMode.NONE, (value) -> {
      });
      this.resourcePacks = Lists.newArrayList();
      this.incompatibleResourcePacks = Lists.newArrayList();
      this.chatVisibility = new SimpleOption("options.chat.visibility", SimpleOption.emptyTooltip(), SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(ChatVisibility.values()), Codec.INT.xmap(ChatVisibility::byId, ChatVisibility::getId)), ChatVisibility.FULL, (value) -> {
      });
      this.chatOpacity = new SimpleOption("options.chat.opacity", SimpleOption.emptyTooltip(), (optionText, value) -> getPercentValueText(optionText, value * 0.9 + 0.1), SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.chatLineSpacing = new SimpleOption("options.chat.line_spacing", SimpleOption.emptyTooltip(), GameOptions::getPercentValueText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.0F, (value) -> {
      });
      this.menuBackgroundBlurriness = new SimpleOption("options.accessibility.menu_background_blurriness", SimpleOption.constantTooltip(MENU_BACKGROUND_BLURRINESS_TOOLTIP), GameOptions::getGenericValueOrOffText, new SimpleOption.ValidatingIntSliderCallbacks(0, 10), 5, (value) -> {
      });
      this.textBackgroundOpacity = new SimpleOption("options.accessibility.text_background_opacity", SimpleOption.emptyTooltip(), GameOptions::getPercentValueText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.5F, (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.panoramaSpeed = new SimpleOption("options.accessibility.panorama_speed", SimpleOption.emptyTooltip(), GameOptions::getPercentValueText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> {
      });
      this.highContrast = SimpleOption.ofBoolean("options.accessibility.high_contrast", SimpleOption.constantTooltip(HIGH_CONTRAST_TOOLTIP), false, (value) -> {
         ResourcePackManager resourcePackManager = MinecraftClient.getInstance().getResourcePackManager();
         boolean bl = resourcePackManager.getEnabledIds().contains("high_contrast");
         if (!bl && value) {
            if (resourcePackManager.enable("high_contrast")) {
               this.refreshResourcePacks(resourcePackManager);
            }
         } else if (bl && !value && resourcePackManager.disable("high_contrast")) {
            this.refreshResourcePacks(resourcePackManager);
         }

      });
      this.narratorHotkey = SimpleOption.ofBoolean("options.accessibility.narrator_hotkey", SimpleOption.constantTooltip(MinecraftClient.IS_SYSTEM_MAC ? Text.translatable("options.accessibility.narrator_hotkey.mac.tooltip") : Text.translatable("options.accessibility.narrator_hotkey.tooltip")), true);
      this.pauseOnLostFocus = true;
      this.enabledPlayerModelParts = EnumSet.allOf(PlayerModelPart.class);
      this.mainArm = new SimpleOption("options.mainHand", SimpleOption.emptyTooltip(), SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(Arm.values()), Arm.CODEC), Arm.RIGHT, (value) -> this.sendClientSettings());
      this.chatScale = new SimpleOption("options.chat.scale", SimpleOption.emptyTooltip(), (optionText, value) -> (Text)(value == (double)0.0F ? ScreenTexts.composeToggleText(optionText, false) : getPercentValueText(optionText, value)), SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.chatWidth = new SimpleOption("options.chat.width", SimpleOption.emptyTooltip(), (optionText, value) -> getPixelValueText(optionText, ChatHud.getWidth(value)), SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.chatHeightUnfocused = new SimpleOption("options.chat.height.unfocused", SimpleOption.emptyTooltip(), (optionText, value) -> getPixelValueText(optionText, ChatHud.getHeight(value)), SimpleOption.DoubleSliderCallbacks.INSTANCE, ChatHud.getDefaultUnfocusedHeight(), (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.chatHeightFocused = new SimpleOption("options.chat.height.focused", SimpleOption.emptyTooltip(), (optionText, value) -> getPixelValueText(optionText, ChatHud.getHeight(value)), SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> MinecraftClient.getInstance().inGameHud.getChatHud().reset());
      this.chatDelay = new SimpleOption("options.chat.delay_instant", SimpleOption.emptyTooltip(), (optionText, value) -> value <= (double)0.0F ? Text.translatable("options.chat.delay_none") : Text.translatable("options.chat.delay", new Object[]{String.format(Locale.ROOT, "%.1f", value)}), (new SimpleOption.ValidatingIntSliderCallbacks(0, 60)).withModifier((value) -> (double)value / (double)10.0F, (value) -> (int)(value * (double)10.0F)), Codec.doubleRange((double)0.0F, (double)6.0F), (double)0.0F, (value) -> MinecraftClient.getInstance().getMessageHandler().setChatDelay(value));
      this.notificationDisplayTime = new SimpleOption("options.notifications.display_time", SimpleOption.constantTooltip(NOTIFICATION_DISPLAY_TIME_TOOLTIP), (optionText, value) -> getGenericValueText(optionText, Text.translatable("options.multiplier", new Object[]{value})), (new SimpleOption.ValidatingIntSliderCallbacks(5, 100)).withModifier((sliderProgressValue) -> (double)sliderProgressValue / (double)10.0F, (value) -> (int)(value * (double)10.0F)), Codec.doubleRange((double)0.5F, (double)10.0F), (double)1.0F, (value) -> {
      });
      this.mipmapLevels = new SimpleOption("options.mipmapLevels", SimpleOption.emptyTooltip(), (optionText, value) -> (Text)(value == 0 ? ScreenTexts.composeToggleText(optionText, false) : getGenericValueText(optionText, value)), new SimpleOption.ValidatingIntSliderCallbacks(0, 4), 4, (value) -> {
      });
      this.useNativeTransport = true;
      this.attackIndicator = new SimpleOption("options.attackIndicator", SimpleOption.emptyTooltip(), SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(AttackIndicator.values()), Codec.INT.xmap(AttackIndicator::byId, AttackIndicator::getId)), AttackIndicator.CROSSHAIR, (value) -> {
      });
      this.tutorialStep = TutorialStep.MOVEMENT;
      this.joinedFirstServer = false;
      this.hideBundleTutorial = false;
      this.biomeBlendRadius = new SimpleOption("options.biomeBlendRadius", SimpleOption.emptyTooltip(), (optionText, value) -> {
         int i = value * 2 + 1;
         return getGenericValueText(optionText, Text.translatable("options.biomeBlendRadius." + i));
      }, new SimpleOption.ValidatingIntSliderCallbacks(0, 7, false), 2, (value) -> MinecraftClient.getInstance().worldRenderer.reload());
      this.mouseWheelSensitivity = new SimpleOption("options.mouseWheelSensitivity", SimpleOption.emptyTooltip(), (optionText, value) -> getGenericValueText(optionText, Text.literal(String.format(Locale.ROOT, "%.2f", value))), (new SimpleOption.ValidatingIntSliderCallbacks(-200, 100)).withModifier(GameOptions::toMouseWheelSensitivityValue, GameOptions::toMouseWheelSensitivitySliderProgressValue), Codec.doubleRange(toMouseWheelSensitivityValue(-200), toMouseWheelSensitivityValue(100)), toMouseWheelSensitivityValue(0), (value) -> {
      });
      this.rawMouseInput = SimpleOption.ofBoolean("options.rawMouseInput", true, (value) -> {
         Window window = MinecraftClient.getInstance().getWindow();
         if (window != null) {
            window.setRawMouseMotion(value);
         }

      });
      this.glDebugVerbosity = 1;
      this.autoJump = SimpleOption.ofBoolean("options.autoJump", false);
      this.operatorItemsTab = SimpleOption.ofBoolean("options.operatorItemsTab", false);
      this.autoSuggestions = SimpleOption.ofBoolean("options.autoSuggestCommands", true);
      this.chatColors = SimpleOption.ofBoolean("options.chat.color", true);
      this.chatLinks = SimpleOption.ofBoolean("options.chat.links", true);
      this.chatLinksPrompt = SimpleOption.ofBoolean("options.chat.links.prompt", true);
      this.enableVsync = SimpleOption.ofBoolean("options.vsync", true, (value) -> {
         if (MinecraftClient.getInstance().getWindow() != null) {
            MinecraftClient.getInstance().getWindow().setVsync(value);
         }

      });
      this.entityShadows = SimpleOption.ofBoolean("options.entityShadows", true);
      this.forceUnicodeFont = SimpleOption.ofBoolean("options.forceUnicodeFont", false, (value) -> onFontOptionsChanged());
      this.japaneseGlyphVariants = SimpleOption.ofBoolean("options.japaneseGlyphVariants", SimpleOption.constantTooltip(Text.translatable("options.japaneseGlyphVariants.tooltip")), shouldUseJapaneseGlyphsByDefault(), (value) -> onFontOptionsChanged());
      this.invertYMouse = SimpleOption.ofBoolean("options.invertMouse", false);
      this.discreteMouseScroll = SimpleOption.ofBoolean("options.discrete_mouse_scroll", false);
      this.realmsNotifications = SimpleOption.ofBoolean("options.realmsNotifications", SimpleOption.constantTooltip(REALMS_NOTIFICATIONS_TOOLTIP), true);
      this.allowServerListing = SimpleOption.ofBoolean("options.allowServerListing", SimpleOption.constantTooltip(ALLOW_SERVER_LISTING_TOOLTIP), true, (value) -> this.sendClientSettings());
      this.reducedDebugInfo = SimpleOption.ofBoolean("options.reducedDebugInfo", false);
      this.soundVolumeLevels = (Map)Util.make(new EnumMap(SoundCategory.class), (soundVolumeLevels) -> {
         for(SoundCategory soundCategory : SoundCategory.values()) {
            soundVolumeLevels.put(soundCategory, this.createSoundVolumeOption("soundCategory." + soundCategory.getName(), soundCategory));
         }

      });
      this.showSubtitles = SimpleOption.ofBoolean("options.showSubtitles", false);
      this.directionalAudio = SimpleOption.ofBoolean("options.directionalAudio", (value) -> value ? Tooltip.of(DIRECTIONAL_AUDIO_ON_TOOLTIP) : Tooltip.of(DIRECTIONAL_AUDIO_OFF_TOOLTIP), false, (value) -> {
         SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
         soundManager.reloadSounds();
         soundManager.play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
      });
      this.backgroundForChatOnly = new SimpleOption("options.accessibility.text_background", SimpleOption.emptyTooltip(), (optionText, value) -> value ? Text.translatable("options.accessibility.text_background.chat") : Text.translatable("options.accessibility.text_background.everywhere"), SimpleOption.BOOLEAN, true, (value) -> {
      });
      this.touchscreen = SimpleOption.ofBoolean("options.touchscreen", false);
      this.fullscreen = SimpleOption.ofBoolean("options.fullscreen", false, (value) -> {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient.getWindow() != null && minecraftClient.getWindow().isFullscreen() != value) {
            minecraftClient.getWindow().toggleFullscreen();
            this.getFullscreen().setValue(minecraftClient.getWindow().isFullscreen());
         }

      });
      this.bobView = SimpleOption.ofBoolean("options.viewBobbing", true);
      this.sneakToggled = new SimpleOption("key.sneak", SimpleOption.emptyTooltip(), (optionText, value) -> value ? TOGGLE_KEY_TEXT : HOLD_KEY_TEXT, SimpleOption.BOOLEAN, false, (value) -> {
      });
      this.sprintToggled = new SimpleOption("key.sprint", SimpleOption.emptyTooltip(), (optionText, value) -> value ? TOGGLE_KEY_TEXT : HOLD_KEY_TEXT, SimpleOption.BOOLEAN, false, (value) -> {
      });
      this.hideMatchedNames = SimpleOption.ofBoolean("options.hideMatchedNames", SimpleOption.constantTooltip(HIDE_MATCHED_NAMES_TOOLTIP), true);
      this.showAutosaveIndicator = SimpleOption.ofBoolean("options.autosaveIndicator", true);
      this.onlyShowSecureChat = SimpleOption.ofBoolean("options.onlyShowSecureChat", SimpleOption.constantTooltip(ONLY_SHOW_SECURE_CHAT_TOOLTIP), false);
      this.forwardKey = new KeyBinding("key.forward", 87, "key.categories.movement");
      this.leftKey = new KeyBinding("key.left", 65, "key.categories.movement");
      this.backKey = new KeyBinding("key.back", 83, "key.categories.movement");
      this.rightKey = new KeyBinding("key.right", 68, "key.categories.movement");
      this.jumpKey = new KeyBinding("key.jump", 32, "key.categories.movement");
      SimpleOption var10006 = this.sneakToggled;
      Objects.requireNonNull(var10006);
      this.sneakKey = new StickyKeyBinding("key.sneak", 340, "key.categories.movement", var10006::getValue);
      var10006 = this.sprintToggled;
      Objects.requireNonNull(var10006);
      this.sprintKey = new StickyKeyBinding("key.sprint", 341, "key.categories.movement", var10006::getValue);
      this.inventoryKey = new KeyBinding("key.inventory", 69, "key.categories.inventory");
      this.swapHandsKey = new KeyBinding("key.swapOffhand", 70, "key.categories.inventory");
      this.dropKey = new KeyBinding("key.drop", 81, "key.categories.inventory");
      this.useKey = new KeyBinding("key.use", InputUtil.Type.MOUSE, 1, "key.categories.gameplay");
      this.attackKey = new KeyBinding("key.attack", InputUtil.Type.MOUSE, 0, "key.categories.gameplay");
      this.pickItemKey = new KeyBinding("key.pickItem", InputUtil.Type.MOUSE, 2, "key.categories.gameplay");
      this.chatKey = new KeyBinding("key.chat", 84, "key.categories.multiplayer");
      this.playerListKey = new KeyBinding("key.playerlist", 258, "key.categories.multiplayer");
      this.commandKey = new KeyBinding("key.command", 47, "key.categories.multiplayer");
      this.socialInteractionsKey = new KeyBinding("key.socialInteractions", 80, "key.categories.multiplayer");
      this.screenshotKey = new KeyBinding("key.screenshot", 291, "key.categories.misc");
      this.togglePerspectiveKey = new KeyBinding("key.togglePerspective", 294, "key.categories.misc");
      this.smoothCameraKey = new KeyBinding("key.smoothCamera", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
      this.fullscreenKey = new KeyBinding("key.fullscreen", 300, "key.categories.misc");
      this.spectatorOutlinesKey = new KeyBinding("key.spectatorOutlines", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.misc");
      this.advancementsKey = new KeyBinding("key.advancements", 76, "key.categories.misc");
      this.hotbarKeys = new KeyBinding[]{new KeyBinding("key.hotbar.1", 49, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 50, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 51, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 52, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 53, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 54, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 55, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 56, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 57, "key.categories.inventory")};
      this.saveToolbarActivatorKey = new KeyBinding("key.saveToolbarActivator", 67, "key.categories.creative");
      this.loadToolbarActivatorKey = new KeyBinding("key.loadToolbarActivator", 88, "key.categories.creative");
      this.allKeys = (KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.attackKey, this.useKey, this.forwardKey, this.leftKey, this.backKey, this.rightKey, this.jumpKey, this.sneakKey, this.sprintKey, this.dropKey, this.inventoryKey, this.chatKey, this.playerListKey, this.pickItemKey, this.commandKey, this.socialInteractionsKey, this.screenshotKey, this.togglePerspectiveKey, this.smoothCameraKey, this.fullscreenKey, this.spectatorOutlinesKey, this.swapHandsKey, this.saveToolbarActivatorKey, this.loadToolbarActivatorKey, this.advancementsKey}, this.hotbarKeys);
      this.perspective = Perspective.FIRST_PERSON;
      this.lastServer = "";
      this.fov = new SimpleOption("options.fov", SimpleOption.emptyTooltip(), (optionText, value) -> {
         Text var10000;
         switch (value) {
            case 70 -> var10000 = getGenericValueText(optionText, Text.translatable("options.fov.min"));
            case 110 -> var10000 = getGenericValueText(optionText, Text.translatable("options.fov.max"));
            default -> var10000 = getGenericValueText(optionText, value);
         }

         return var10000;
      }, new SimpleOption.ValidatingIntSliderCallbacks(30, 110), Codec.DOUBLE.xmap((value) -> (int)(value * (double)40.0F + (double)70.0F), (value) -> ((double)value - (double)70.0F) / (double)40.0F), 70, (value) -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate());
      this.telemetryOptInExtra = SimpleOption.ofBoolean("options.telemetry.button", SimpleOption.constantTooltip(TELEMETRY_TOOLTIP), (optionText, value) -> {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (!minecraftClient.isTelemetryEnabledByApi()) {
            return Text.translatable("options.telemetry.state.none");
         } else {
            return value && minecraftClient.isOptionalTelemetryEnabledByApi() ? Text.translatable("options.telemetry.state.all") : Text.translatable("options.telemetry.state.minimal");
         }
      }, false, (value) -> {
      });
      this.distortionEffectScale = new SimpleOption("options.screenEffectScale", SimpleOption.constantTooltip(SCREEN_EFFECT_SCALE_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> {
      });
      this.fovEffectScale = new SimpleOption("options.fovEffectScale", SimpleOption.constantTooltip(FOV_EFFECT_SCALE_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(MathHelper::square, Math::sqrt), Codec.doubleRange((double)0.0F, (double)1.0F), (double)1.0F, (value) -> {
      });
      this.darknessEffectScale = new SimpleOption("options.darknessEffectScale", SimpleOption.constantTooltip(DARKNESS_EFFECT_SCALE_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE.withModifier(MathHelper::square, Math::sqrt), (double)1.0F, (value) -> {
      });
      this.glintSpeed = new SimpleOption("options.glintSpeed", SimpleOption.constantTooltip(GLINT_SPEED_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.5F, (value) -> {
      });
      this.glintStrength = new SimpleOption("options.glintStrength", SimpleOption.constantTooltip(GLINT_STRENGTH_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.75F, RenderSystem::setShaderGlintAlpha);
      this.damageTiltStrength = new SimpleOption("options.damageTiltStrength", SimpleOption.constantTooltip(DAMAGE_TILT_STRENGTH_TOOLTIP), GameOptions::getPercentValueOrOffText, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)1.0F, (value) -> {
      });
      this.gamma = new SimpleOption("options.gamma", SimpleOption.emptyTooltip(), (optionText, value) -> {
         int i = (int)(value * (double)100.0F);
         if (i == 0) {
            return getGenericValueText(optionText, Text.translatable("options.gamma.min"));
         } else if (i == 50) {
            return getGenericValueText(optionText, Text.translatable("options.gamma.default"));
         } else {
            return i == 100 ? getGenericValueText(optionText, Text.translatable("options.gamma.max")) : getGenericValueText(optionText, i);
         }
      }, SimpleOption.DoubleSliderCallbacks.INSTANCE, (double)0.5F, (value) -> {
      });
      this.guiScale = new SimpleOption("options.guiScale", SimpleOption.emptyTooltip(), (optionText, value) -> value == 0 ? Text.translatable("options.guiScale.auto") : Text.literal(Integer.toString(value)), new SimpleOption.MaxSuppliableIntCallbacks(0, () -> {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         return !minecraftClient.isRunning() ? 2147483646 : minecraftClient.getWindow().calculateScaleFactor(0, minecraftClient.forcesUnicodeFont());
      }, 2147483646), 0, (value) -> this.client.onResolutionChanged());
      this.particles = new SimpleOption("options.particles", SimpleOption.emptyTooltip(), SimpleOption.enumValueText(), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(ParticlesMode.values()), Codec.INT.xmap(ParticlesMode::byId, ParticlesMode::getId)), ParticlesMode.ALL, (value) -> {
      });
      this.narrator = new SimpleOption("options.narrator", SimpleOption.emptyTooltip(), (optionText, value) -> (Text)(this.client.getNarratorManager().isActive() ? value.getName() : Text.translatable("options.narrator.notavailable")), new SimpleOption.PotentialValuesBasedCallbacks(Arrays.asList(NarratorMode.values()), Codec.INT.xmap(NarratorMode::byId, NarratorMode::getId)), NarratorMode.OFF, (value) -> this.client.getNarratorManager().onModeChange(value));
      this.language = "en_us";
      this.soundDevice = new SimpleOption("options.audioDevice", SimpleOption.emptyTooltip(), (optionText, value) -> {
         if ("".equals(value)) {
            return Text.translatable("options.audioDevice.default");
         } else {
            return value.startsWith("OpenAL Soft on ") ? Text.literal(value.substring(SoundSystem.OPENAL_SOFT_ON_LENGTH)) : Text.literal(value);
         }
      }, new SimpleOption.LazyCyclingCallbacks(() -> Stream.concat(Stream.of(""), MinecraftClient.getInstance().getSoundManager().getSoundDevices().stream()).toList(), (value) -> MinecraftClient.getInstance().isRunning() && value != "" && !MinecraftClient.getInstance().getSoundManager().getSoundDevices().contains(value) ? Optional.empty() : Optional.of(value), Codec.STRING), "", (value) -> {
         SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
         soundManager.reloadSounds();
         soundManager.play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
      });
      this.onboardAccessibility = true;
      this.client = client;
      this.optionsFile = new File(optionsFile, "options.txt");
      boolean bl = Runtime.getRuntime().maxMemory() >= 1000000000L;
      this.viewDistance = new SimpleOption("options.renderDistance", SimpleOption.emptyTooltip(), (optionText, value) -> getGenericValueText(optionText, Text.translatable("options.chunks", new Object[]{value})), new SimpleOption.ValidatingIntSliderCallbacks(2, bl ? 32 : 16, false), 12, (value) -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate());
      this.simulationDistance = new SimpleOption("options.simulationDistance", SimpleOption.emptyTooltip(), (optionText, value) -> getGenericValueText(optionText, Text.translatable("options.chunks", new Object[]{value})), new SimpleOption.ValidatingIntSliderCallbacks(5, bl ? 32 : 16, false), 12, (value) -> {
      });
      this.syncChunkWrites = Util.getOperatingSystem() == OperatingSystem.WINDOWS;
      this.load();
   }

   public float getTextBackgroundOpacity(float fallback) {
      return (Boolean)this.backgroundForChatOnly.getValue() ? fallback : ((Double)this.getTextBackgroundOpacity().getValue()).floatValue();
   }

   public int getTextBackgroundColor(float fallbackOpacity) {
      return (int)(this.getTextBackgroundOpacity(fallbackOpacity) * 255.0F) << 24 & -16777216;
   }

   public int getTextBackgroundColor(int fallbackColor) {
      return (Boolean)this.backgroundForChatOnly.getValue() ? fallbackColor : (int)((Double)this.textBackgroundOpacity.getValue() * (double)255.0F) << 24 & -16777216;
   }

   public void setKeyCode(KeyBinding key, InputUtil.Key code) {
      key.setBoundKey(code);
      this.write();
   }

   private void acceptProfiledOptions(OptionVisitor visitor) {
      visitor.accept("ao", this.ao);
      visitor.accept("biomeBlendRadius", this.biomeBlendRadius);
      visitor.accept("enableVsync", this.enableVsync);
      visitor.accept("entityDistanceScaling", this.entityDistanceScaling);
      visitor.accept("entityShadows", this.entityShadows);
      visitor.accept("forceUnicodeFont", this.forceUnicodeFont);
      visitor.accept("japaneseGlyphVariants", this.japaneseGlyphVariants);
      visitor.accept("fov", this.fov);
      visitor.accept("fovEffectScale", this.fovEffectScale);
      visitor.accept("darknessEffectScale", this.darknessEffectScale);
      visitor.accept("glintSpeed", this.glintSpeed);
      visitor.accept("glintStrength", this.glintStrength);
      visitor.accept("prioritizeChunkUpdates", this.chunkBuilderMode);
      visitor.accept("fullscreen", this.fullscreen);
      visitor.accept("gamma", this.gamma);
      visitor.accept("graphicsMode", this.graphicsMode);
      visitor.accept("guiScale", this.guiScale);
      visitor.accept("maxFps", this.maxFps);
      visitor.accept("mipmapLevels", this.mipmapLevels);
      visitor.accept("narrator", this.narrator);
      visitor.accept("particles", this.particles);
      visitor.accept("reducedDebugInfo", this.reducedDebugInfo);
      visitor.accept("renderClouds", this.cloudRenderMode);
      visitor.accept("renderDistance", this.viewDistance);
      visitor.accept("simulationDistance", this.simulationDistance);
      visitor.accept("screenEffectScale", this.distortionEffectScale);
      visitor.accept("soundDevice", this.soundDevice);
   }

   private void accept(Visitor visitor) {
      this.acceptProfiledOptions(visitor);
      visitor.accept("autoJump", this.autoJump);
      visitor.accept("operatorItemsTab", this.operatorItemsTab);
      visitor.accept("autoSuggestions", this.autoSuggestions);
      visitor.accept("chatColors", this.chatColors);
      visitor.accept("chatLinks", this.chatLinks);
      visitor.accept("chatLinksPrompt", this.chatLinksPrompt);
      visitor.accept("discrete_mouse_scroll", this.discreteMouseScroll);
      visitor.accept("invertYMouse", this.invertYMouse);
      visitor.accept("realmsNotifications", this.realmsNotifications);
      visitor.accept("showSubtitles", this.showSubtitles);
      visitor.accept("directionalAudio", this.directionalAudio);
      visitor.accept("touchscreen", this.touchscreen);
      visitor.accept("bobView", this.bobView);
      visitor.accept("toggleCrouch", this.sneakToggled);
      visitor.accept("toggleSprint", this.sprintToggled);
      visitor.accept("darkMojangStudiosBackground", this.monochromeLogo);
      visitor.accept("hideLightningFlashes", this.hideLightningFlashes);
      visitor.accept("hideSplashTexts", this.hideSplashTexts);
      visitor.accept("mouseSensitivity", this.mouseSensitivity);
      visitor.accept("damageTiltStrength", this.damageTiltStrength);
      visitor.accept("highContrast", this.highContrast);
      visitor.accept("narratorHotkey", this.narratorHotkey);
      List var10003 = this.resourcePacks;
      Function var10004 = GameOptions::parseList;
      Gson var10005 = GSON;
      Objects.requireNonNull(var10005);
      this.resourcePacks = (List)visitor.visitObject("resourcePacks", var10003, var10004, var10005::toJson);
      var10003 = this.incompatibleResourcePacks;
      var10004 = GameOptions::parseList;
      var10005 = GSON;
      Objects.requireNonNull(var10005);
      this.incompatibleResourcePacks = (List)visitor.visitObject("incompatibleResourcePacks", var10003, var10004, var10005::toJson);
      this.lastServer = visitor.visitString("lastServer", this.lastServer);
      this.language = visitor.visitString("lang", this.language);
      visitor.accept("chatVisibility", this.chatVisibility);
      visitor.accept("chatOpacity", this.chatOpacity);
      visitor.accept("chatLineSpacing", this.chatLineSpacing);
      visitor.accept("textBackgroundOpacity", this.textBackgroundOpacity);
      visitor.accept("backgroundForChatOnly", this.backgroundForChatOnly);
      this.hideServerAddress = visitor.visitBoolean("hideServerAddress", this.hideServerAddress);
      this.advancedItemTooltips = visitor.visitBoolean("advancedItemTooltips", this.advancedItemTooltips);
      this.pauseOnLostFocus = visitor.visitBoolean("pauseOnLostFocus", this.pauseOnLostFocus);
      this.overrideWidth = visitor.visitInt("overrideWidth", this.overrideWidth);
      this.overrideHeight = visitor.visitInt("overrideHeight", this.overrideHeight);
      visitor.accept("chatHeightFocused", this.chatHeightFocused);
      visitor.accept("chatDelay", this.chatDelay);
      visitor.accept("chatHeightUnfocused", this.chatHeightUnfocused);
      visitor.accept("chatScale", this.chatScale);
      visitor.accept("chatWidth", this.chatWidth);
      visitor.accept("notificationDisplayTime", this.notificationDisplayTime);
      this.useNativeTransport = visitor.visitBoolean("useNativeTransport", this.useNativeTransport);
      visitor.accept("mainHand", this.mainArm);
      visitor.accept("attackIndicator", this.attackIndicator);
      this.tutorialStep = (TutorialStep)visitor.visitObject("tutorialStep", this.tutorialStep, TutorialStep::byName, TutorialStep::getName);
      visitor.accept("mouseWheelSensitivity", this.mouseWheelSensitivity);
      visitor.accept("rawMouseInput", this.rawMouseInput);
      this.glDebugVerbosity = visitor.visitInt("glDebugVerbosity", this.glDebugVerbosity);
      this.skipMultiplayerWarning = visitor.visitBoolean("skipMultiplayerWarning", this.skipMultiplayerWarning);
      visitor.accept("hideMatchedNames", this.hideMatchedNames);
      this.joinedFirstServer = visitor.visitBoolean("joinedFirstServer", this.joinedFirstServer);
      this.hideBundleTutorial = visitor.visitBoolean("hideBundleTutorial", this.hideBundleTutorial);
      this.syncChunkWrites = visitor.visitBoolean("syncChunkWrites", this.syncChunkWrites);
      visitor.accept("showAutosaveIndicator", this.showAutosaveIndicator);
      visitor.accept("allowServerListing", this.allowServerListing);
      visitor.accept("onlyShowSecureChat", this.onlyShowSecureChat);
      visitor.accept("panoramaScrollSpeed", this.panoramaSpeed);
      visitor.accept("telemetryOptInExtra", this.telemetryOptInExtra);
      this.onboardAccessibility = visitor.visitBoolean("onboardAccessibility", this.onboardAccessibility);
      visitor.accept("menuBackgroundBlurriness", this.menuBackgroundBlurriness);

      for(KeyBinding keyBinding : this.allKeys) {
         String string = keyBinding.getBoundKeyTranslationKey();
         String string2 = visitor.visitString("key_" + keyBinding.getTranslationKey(), string);
         if (!string.equals(string2)) {
            keyBinding.setBoundKey(InputUtil.fromTranslationKey(string2));
         }
      }

      for(SoundCategory soundCategory : SoundCategory.values()) {
         visitor.accept("soundCategory_" + soundCategory.getName(), (SimpleOption)this.soundVolumeLevels.get(soundCategory));
      }

      for(PlayerModelPart playerModelPart : PlayerModelPart.values()) {
         boolean bl = this.enabledPlayerModelParts.contains(playerModelPart);
         boolean bl2 = visitor.visitBoolean("modelPart_" + playerModelPart.getName(), bl);
         if (bl2 != bl) {
            this.setPlayerModelPart(playerModelPart, bl2);
         }
      }

   }

   public void load() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         NbtCompound nbtCompound = new NbtCompound();
         BufferedReader bufferedReader = Files.newReader(this.optionsFile, Charsets.UTF_8);

         try {
            bufferedReader.lines().forEach((line) -> {
               try {
                  Iterator<String> iterator = COLON_SPLITTER.split(line).iterator();
                  nbtCompound.putString((String)iterator.next(), (String)iterator.next());
               } catch (Exception var3) {
                  LOGGER.warn("Skipping bad option: {}", line);
               }

            });
         } catch (Throwable var6) {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }

         final NbtCompound nbtCompound2 = this.update(nbtCompound);
         if (!nbtCompound2.contains("graphicsMode") && nbtCompound2.contains("fancyGraphics")) {
            if (isTrue(nbtCompound2.getString("fancyGraphics"))) {
               this.graphicsMode.setValue(GraphicsMode.FANCY);
            } else {
               this.graphicsMode.setValue(GraphicsMode.FAST);
            }
         }

         this.accept(new Visitor() {
            @Nullable
            private String find(String key) {
               return nbtCompound2.contains(key) ? nbtCompound2.get(key).asString() : null;
            }

            public void accept(String key, SimpleOption option) {
               String string = this.find(key);
               if (string != null) {
                  JsonReader jsonReader = new JsonReader(new StringReader(string.isEmpty() ? "\"\"" : string));
                  JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                  DataResult<T> dataResult = option.getCodec().parse(JsonOps.INSTANCE, jsonElement);
                  dataResult.error().ifPresent((error) -> GameOptions.LOGGER.error("Error parsing option value " + string + " for option " + String.valueOf(option) + ": " + error.message()));
                  Objects.requireNonNull(option);
                  dataResult.ifSuccess(option::setValue);
               }

            }

            public int visitInt(String key, int current) {
               String string = this.find(key);
               if (string != null) {
                  try {
                     return Integer.parseInt(string);
                  } catch (NumberFormatException numberFormatException) {
                     GameOptions.LOGGER.warn("Invalid integer value for option {} = {}", new Object[]{key, string, numberFormatException});
                  }
               }

               return current;
            }

            public boolean visitBoolean(String key, boolean current) {
               String string = this.find(key);
               return string != null ? GameOptions.isTrue(string) : current;
            }

            public String visitString(String key, String current) {
               return (String)MoreObjects.firstNonNull(this.find(key), current);
            }

            public float visitFloat(String key, float current) {
               String string = this.find(key);
               if (string != null) {
                  if (GameOptions.isTrue(string)) {
                     return 1.0F;
                  }

                  if (GameOptions.isFalse(string)) {
                     return 0.0F;
                  }

                  try {
                     return Float.parseFloat(string);
                  } catch (NumberFormatException numberFormatException) {
                     GameOptions.LOGGER.warn("Invalid floating point value for option {} = {}", new Object[]{key, string, numberFormatException});
                  }
               }

               return current;
            }

            public Object visitObject(String key, Object current, Function decoder, Function encoder) {
               String string = this.find(key);
               return string == null ? current : decoder.apply(string);
            }
         });
         if (nbtCompound2.contains("fullscreenResolution")) {
            this.fullscreenResolution = nbtCompound2.getString("fullscreenResolution");
         }

         if (this.client.getWindow() != null) {
            this.client.getWindow().setFramerateLimit((Integer)this.maxFps.getValue());
         }

         KeyBinding.updateKeysByCode();
      } catch (Exception exception) {
         LOGGER.error("Failed to load options", exception);
      }

   }

   static boolean isTrue(String value) {
      return "true".equals(value);
   }

   static boolean isFalse(String value) {
      return "false".equals(value);
   }

   private NbtCompound update(NbtCompound nbt) {
      int i = 0;

      try {
         i = Integer.parseInt(nbt.getString("version"));
      } catch (RuntimeException var4) {
      }

      return DataFixTypes.OPTIONS.update(this.client.getDataFixer(), nbt, i);
   }

   public void write() {
      try {
         final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));

         try {
            printWriter.println("version:" + SharedConstants.getGameVersion().getSaveVersion().getId());
            this.accept(new Visitor() {
               public void print(String key) {
                  printWriter.print(key);
                  printWriter.print(':');
               }

               public void accept(String key, SimpleOption option) {
                  option.getCodec().encodeStart(JsonOps.INSTANCE, option.getValue()).ifError((error) -> {
                     Logger var10000 = GameOptions.LOGGER;
                     String var10001 = String.valueOf(option);
                     var10000.error("Error saving option " + var10001 + ": " + String.valueOf(error));
                  }).ifSuccess((json) -> {
                     this.print(key);
                     printWriter.println(GameOptions.GSON.toJson(json));
                  });
               }

               public int visitInt(String key, int current) {
                  this.print(key);
                  printWriter.println(current);
                  return current;
               }

               public boolean visitBoolean(String key, boolean current) {
                  this.print(key);
                  printWriter.println(current);
                  return current;
               }

               public String visitString(String key, String current) {
                  this.print(key);
                  printWriter.println(current);
                  return current;
               }

               public float visitFloat(String key, float current) {
                  this.print(key);
                  printWriter.println(current);
                  return current;
               }

               public Object visitObject(String key, Object current, Function decoder, Function encoder) {
                  this.print(key);
                  printWriter.println((String)encoder.apply(current));
                  return current;
               }
            });
            if (this.client.getWindow().getFullscreenVideoMode().isPresent()) {
               printWriter.println("fullscreenResolution:" + ((VideoMode)this.client.getWindow().getFullscreenVideoMode().get()).asString());
            }
         } catch (Throwable var5) {
            try {
               printWriter.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         printWriter.close();
      } catch (Exception exception) {
         LOGGER.error("Failed to save options", exception);
      }

      this.sendClientSettings();
   }

   public SyncedClientOptions getSyncedOptions() {
      int i = 0;

      for(PlayerModelPart playerModelPart : this.enabledPlayerModelParts) {
         i |= playerModelPart.getBitFlag();
      }

      return new SyncedClientOptions(this.language, (Integer)this.viewDistance.getValue(), (ChatVisibility)this.chatVisibility.getValue(), (Boolean)this.chatColors.getValue(), i, (Arm)this.mainArm.getValue(), this.client.shouldFilterText(), (Boolean)this.allowServerListing.getValue());
   }

   public void sendClientSettings() {
      if (this.client.player != null) {
         this.client.player.networkHandler.sendPacket(new ClientOptionsC2SPacket(this.getSyncedOptions()));
      }

   }

   private void setPlayerModelPart(PlayerModelPart part, boolean enabled) {
      if (enabled) {
         this.enabledPlayerModelParts.add(part);
      } else {
         this.enabledPlayerModelParts.remove(part);
      }

   }

   public boolean isPlayerModelPartEnabled(PlayerModelPart part) {
      return this.enabledPlayerModelParts.contains(part);
   }

   public void togglePlayerModelPart(PlayerModelPart part, boolean enabled) {
      this.setPlayerModelPart(part, enabled);
      this.sendClientSettings();
   }

   public CloudRenderMode getCloudRenderModeValue() {
      return this.getClampedViewDistance() >= 4 ? (CloudRenderMode)this.cloudRenderMode.getValue() : CloudRenderMode.OFF;
   }

   public boolean shouldUseNativeTransport() {
      return this.useNativeTransport;
   }

   public void addResourcePackProfilesToManager(ResourcePackManager manager) {
      Set<String> set = Sets.newLinkedHashSet();
      Iterator<String> iterator = this.resourcePacks.iterator();

      while(iterator.hasNext()) {
         String string = (String)iterator.next();
         ResourcePackProfile resourcePackProfile = manager.getProfile(string);
         if (resourcePackProfile == null && !string.startsWith("file/")) {
            resourcePackProfile = manager.getProfile("file/" + string);
         }

         if (resourcePackProfile == null) {
            LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", string);
            iterator.remove();
         } else if (!resourcePackProfile.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
            LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", string);
            iterator.remove();
         } else if (resourcePackProfile.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
            LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", string);
            this.incompatibleResourcePacks.remove(string);
         } else {
            set.add(resourcePackProfile.getId());
         }
      }

      manager.setEnabledProfiles(set);
   }

   public Perspective getPerspective() {
      return this.perspective;
   }

   public void setPerspective(Perspective perspective) {
      this.perspective = perspective;
   }

   private static List parseList(String content) {
      List<String> list = (List)JsonHelper.deserialize(GSON, content, STRING_LIST_TYPE);
      return (List)(list != null ? list : Lists.newArrayList());
   }

   public File getOptionsFile() {
      return this.optionsFile;
   }

   public String collectProfiledOptions() {
      final List<Pair<String, Object>> list = new ArrayList();
      this.acceptProfiledOptions(new OptionVisitor() {
         public void accept(String key, SimpleOption option) {
            list.add(Pair.of(key, option.getValue()));
         }
      });
      list.add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenResolution)));
      list.add(Pair.of("glDebugVerbosity", this.glDebugVerbosity));
      list.add(Pair.of("overrideHeight", this.overrideHeight));
      list.add(Pair.of("overrideWidth", this.overrideWidth));
      list.add(Pair.of("syncChunkWrites", this.syncChunkWrites));
      list.add(Pair.of("useNativeTransport", this.useNativeTransport));
      list.add(Pair.of("resourcePacks", this.resourcePacks));
      return (String)list.stream().sorted(Comparator.comparing(Pair::getFirst)).map((option) -> {
         String var10000 = (String)option.getFirst();
         return var10000 + ": " + String.valueOf(option.getSecond());
      }).collect(Collectors.joining(System.lineSeparator()));
   }

   public void setServerViewDistance(int serverViewDistance) {
      this.serverViewDistance = serverViewDistance;
   }

   public int getClampedViewDistance() {
      return this.serverViewDistance > 0 ? Math.min((Integer)this.viewDistance.getValue(), this.serverViewDistance) : (Integer)this.viewDistance.getValue();
   }

   private static Text getPixelValueText(Text prefix, int value) {
      return Text.translatable("options.pixel_value", new Object[]{prefix, value});
   }

   private static Text getPercentValueText(Text prefix, double value) {
      return Text.translatable("options.percent_value", new Object[]{prefix, (int)(value * (double)100.0F)});
   }

   public static Text getGenericValueText(Text prefix, Text value) {
      return Text.translatable("options.generic_value", new Object[]{prefix, value});
   }

   public static Text getGenericValueText(Text prefix, int value) {
      return getGenericValueText(prefix, Text.literal(Integer.toString(value)));
   }

   public static Text getGenericValueOrOffText(Text prefix, int value) {
      return value == 0 ? getGenericValueText(prefix, ScreenTexts.OFF) : getGenericValueText(prefix, value);
   }

   private static Text getPercentValueOrOffText(Text prefix, double value) {
      return value == (double)0.0F ? getGenericValueText(prefix, ScreenTexts.OFF) : getPercentValueText(prefix, value);
   }

   static {
      FABULOUS_GRAPHICS_TOOLTIP = Text.translatable("options.graphics.fabulous.tooltip", new Object[]{Text.translatable("options.graphics.fabulous").formatted(Formatting.ITALIC)});
      FANCY_GRAPHICS_TOOLTIP = Text.translatable("options.graphics.fancy.tooltip");
      NONE_CHUNK_BUILDER_MODE_TOOLTIP = Text.translatable("options.prioritizeChunkUpdates.none.tooltip");
      BY_PLAYER_CHUNK_BUILDER_MODE_TOOLTIP = Text.translatable("options.prioritizeChunkUpdates.byPlayer.tooltip");
      NEARBY_CHUNK_BUILDER_MODE_TOOLTIP = Text.translatable("options.prioritizeChunkUpdates.nearby.tooltip");
      MENU_BACKGROUND_BLURRINESS_TOOLTIP = Text.translatable("options.accessibility.menu_background_blurriness.tooltip");
      HIGH_CONTRAST_TOOLTIP = Text.translatable("options.accessibility.high_contrast.tooltip");
      NOTIFICATION_DISPLAY_TIME_TOOLTIP = Text.translatable("options.notifications.display_time.tooltip");
      REALMS_NOTIFICATIONS_TOOLTIP = Text.translatable("options.realmsNotifications.tooltip");
      ALLOW_SERVER_LISTING_TOOLTIP = Text.translatable("options.allowServerListing.tooltip");
      DIRECTIONAL_AUDIO_ON_TOOLTIP = Text.translatable("options.directionalAudio.on.tooltip");
      DIRECTIONAL_AUDIO_OFF_TOOLTIP = Text.translatable("options.directionalAudio.off.tooltip");
      TOGGLE_KEY_TEXT = Text.translatable("options.key.toggle");
      HOLD_KEY_TEXT = Text.translatable("options.key.hold");
      HIDE_MATCHED_NAMES_TOOLTIP = Text.translatable("options.hideMatchedNames.tooltip");
      ONLY_SHOW_SECURE_CHAT_TOOLTIP = Text.translatable("options.onlyShowSecureChat.tooltip");
      TELEMETRY_TOOLTIP = Text.translatable("options.telemetry.button.tooltip", new Object[]{Text.translatable("options.telemetry.state.minimal"), Text.translatable("options.telemetry.state.all")});
      SCREEN_EFFECT_SCALE_TOOLTIP = Text.translatable("options.screenEffectScale.tooltip");
      FOV_EFFECT_SCALE_TOOLTIP = Text.translatable("options.fovEffectScale.tooltip");
      DARKNESS_EFFECT_SCALE_TOOLTIP = Text.translatable("options.darknessEffectScale.tooltip");
      GLINT_SPEED_TOOLTIP = Text.translatable("options.glintSpeed.tooltip");
      GLINT_STRENGTH_TOOLTIP = Text.translatable("options.glintStrength.tooltip");
      DAMAGE_TILT_STRENGTH_TOOLTIP = Text.translatable("options.damageTiltStrength.tooltip");
   }

   @Environment(EnvType.CLIENT)
   interface OptionVisitor {
      void accept(String key, SimpleOption option);
   }

   @Environment(EnvType.CLIENT)
   interface Visitor extends OptionVisitor {
      int visitInt(String key, int current);

      boolean visitBoolean(String key, boolean current);

      String visitString(String key, String current);

      float visitFloat(String key, float current);

      Object visitObject(String key, Object current, Function decoder, Function encoder);
   }
}
