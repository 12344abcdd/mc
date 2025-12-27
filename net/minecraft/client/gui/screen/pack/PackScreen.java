package net.minecraft.client.gui.screen.pack;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SymlinkWarningScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackOpener;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.path.SymlinkEntry;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PackScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Text AVAILABLE_TITLE = Text.translatable("pack.available.title");
   private static final Text SELECTED_TITLE = Text.translatable("pack.selected.title");
   private static final Text OPEN_FOLDER = Text.translatable("pack.openFolder");
   private static final int field_32395 = 200;
   private static final Text DROP_INFO;
   private static final Text FOLDER_INFO;
   private static final int field_32396 = 20;
   private static final Identifier UNKNOWN_PACK;
   private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
   private final ResourcePackOrganizer organizer;
   @Nullable
   private DirectoryWatcher directoryWatcher;
   private long refreshTimeout;
   private PackListWidget availablePackList;
   private PackListWidget selectedPackList;
   private final Path file;
   private ButtonWidget doneButton;
   private final Map iconTextures = Maps.newHashMap();

   public PackScreen(ResourcePackManager resourcePackManager, Consumer applier, Path file, Text title) {
      super(title);
      this.organizer = new ResourcePackOrganizer(this::updatePackLists, this::getPackIconTexture, resourcePackManager, applier);
      this.file = file;
      this.directoryWatcher = PackScreen.DirectoryWatcher.create(file);
   }

   public void close() {
      this.organizer.apply();
      this.closeDirectoryWatcher();
   }

   private void closeDirectoryWatcher() {
      if (this.directoryWatcher != null) {
         try {
            this.directoryWatcher.close();
            this.directoryWatcher = null;
         } catch (Exception var2) {
         }
      }

   }

   protected void init() {
      DirectionalLayoutWidget directionalLayoutWidget = (DirectionalLayoutWidget)this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(5));
      directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
      directionalLayoutWidget.add(new TextWidget(this.getTitle(), this.textRenderer));
      directionalLayoutWidget.add(new TextWidget(DROP_INFO, this.textRenderer));
      this.availablePackList = (PackListWidget)this.addDrawableChild(new PackListWidget(this.client, this, 200, this.height - 66, AVAILABLE_TITLE));
      this.selectedPackList = (PackListWidget)this.addDrawableChild(new PackListWidget(this.client, this, 200, this.height - 66, SELECTED_TITLE));
      DirectionalLayoutWidget directionalLayoutWidget2 = (DirectionalLayoutWidget)this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
      directionalLayoutWidget2.add(ButtonWidget.builder(OPEN_FOLDER, (button) -> Util.getOperatingSystem().open(this.file)).tooltip(Tooltip.of(FOLDER_INFO)).build());
      this.doneButton = (ButtonWidget)directionalLayoutWidget2.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close()).build());
      this.refresh();
      this.layout.forEachChild((element) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(element);
      });
      this.initTabNavigation();
   }

   protected void initTabNavigation() {
      this.layout.refreshPositions();
      this.availablePackList.position(200, this.layout);
      this.availablePackList.setX(this.width / 2 - 15 - 200);
      this.selectedPackList.position(200, this.layout);
      this.selectedPackList.setX(this.width / 2 + 15);
   }

   public void tick() {
      if (this.directoryWatcher != null) {
         try {
            if (this.directoryWatcher.pollForChange()) {
               this.refreshTimeout = 20L;
            }
         } catch (IOException var2) {
            LOGGER.warn("Failed to poll for directory {} changes, stopping", this.file);
            this.closeDirectoryWatcher();
         }
      }

      if (this.refreshTimeout > 0L && --this.refreshTimeout == 0L) {
         this.refresh();
      }

   }

   private void updatePackLists() {
      this.updatePackList(this.selectedPackList, this.organizer.getEnabledPacks());
      this.updatePackList(this.availablePackList, this.organizer.getDisabledPacks());
      this.doneButton.active = !this.selectedPackList.children().isEmpty();
   }

   private void updatePackList(PackListWidget widget, Stream packs) {
      widget.children().clear();
      PackListWidget.ResourcePackEntry resourcePackEntry = (PackListWidget.ResourcePackEntry)widget.getSelectedOrNull();
      String string = resourcePackEntry == null ? "" : resourcePackEntry.getName();
      widget.setSelected((EntryListWidget.Entry)null);
      packs.forEach((pack) -> {
         PackListWidget.ResourcePackEntry resourcePackEntry = new PackListWidget.ResourcePackEntry(this.client, widget, pack);
         widget.children().add(resourcePackEntry);
         if (pack.getName().equals(string)) {
            widget.setSelected(resourcePackEntry);
         }

      });
   }

   public void switchFocusedList(PackListWidget listWidget) {
      PackListWidget packListWidget = this.selectedPackList == listWidget ? this.availablePackList : this.selectedPackList;
      this.switchFocus(GuiNavigationPath.of((Element)packListWidget.getFirst(), (ParentElement[])(packListWidget, this)));
   }

   public void clearSelection() {
      this.selectedPackList.setSelected((EntryListWidget.Entry)null);
      this.availablePackList.setSelected((EntryListWidget.Entry)null);
   }

   private void refresh() {
      this.organizer.refresh();
      this.updatePackLists();
      this.refreshTimeout = 0L;
      this.iconTextures.clear();
   }

   protected static void copyPacks(MinecraftClient client, List srcPaths, Path destPath) {
      MutableBoolean mutableBoolean = new MutableBoolean();
      srcPaths.forEach((src) -> {
         try {
            Stream<Path> stream = Files.walk(src);

            try {
               stream.forEach((toCopy) -> {
                  try {
                     Util.relativeCopy(src.getParent(), destPath, toCopy);
                  } catch (IOException iOException) {
                     LOGGER.warn("Failed to copy datapack file  from {} to {}", new Object[]{toCopy, destPath, iOException});
                     mutableBoolean.setTrue();
                  }

               });
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var8) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", src, destPath);
            mutableBoolean.setTrue();
         }

      });
      if (mutableBoolean.isTrue()) {
         SystemToast.addPackCopyFailure(client, destPath.toString());
      }

   }

   public void filesDragged(List paths) {
      String string = (String)streamFileNames(paths).collect(Collectors.joining(", "));
      this.client.setScreen(new ConfirmScreen((confirmed) -> {
         if (confirmed) {
            List<Path> list2 = new ArrayList(paths.size());
            Set<Path> set = new HashSet(paths);
            ResourcePackOpener<Path> resourcePackOpener = new ResourcePackOpener(this.client.getSymlinkFinder()) {
               protected Path openZip(Path path) {
                  return path;
               }

               protected Path openDirectory(Path path) {
                  return path;
               }

               // $FF: synthetic method
               protected Object openDirectory(final Path path) throws IOException {
                  return this.openDirectory(path);
               }

               // $FF: synthetic method
               protected Object openZip(final Path path) throws IOException {
                  return this.openZip(path);
               }
            };
            List<SymlinkEntry> list3 = new ArrayList();

            for(Path path : paths) {
               try {
                  Path path2 = (Path)resourcePackOpener.open(path, list3);
                  if (path2 == null) {
                     LOGGER.warn("Path {} does not seem like pack", path);
                  } else {
                     list2.add(path2);
                     set.remove(path2);
                  }
               } catch (IOException iOException) {
                  LOGGER.warn("Failed to check {} for packs", path, iOException);
               }
            }

            if (!list3.isEmpty()) {
               this.client.setScreen(SymlinkWarningScreen.pack(() -> this.client.setScreen(this)));
               return;
            }

            if (!list2.isEmpty()) {
               copyPacks(this.client, list2, this.file);
               this.refresh();
            }

            if (!set.isEmpty()) {
               String string = (String)streamFileNames(set).collect(Collectors.joining(", "));
               this.client.setScreen(new NoticeScreen(() -> this.client.setScreen(this), Text.translatable("pack.dropRejected.title"), Text.translatable("pack.dropRejected.message", new Object[]{string})));
               return;
            }
         }

         this.client.setScreen(this);
      }, Text.translatable("pack.dropConfirm"), Text.literal(string)));
   }

   private static Stream streamFileNames(Collection paths) {
      return paths.stream().map(Path::getFileName).map(Path::toString);
   }

   private Identifier loadPackIcon(TextureManager textureManager, ResourcePackProfile resourcePackProfile) {
      try {
         ResourcePack resourcePack = resourcePackProfile.createResourcePack();

         Identifier var15;
         label69: {
            Identifier var9;
            try {
               InputSupplier<InputStream> inputSupplier = resourcePack.openRoot(new String[]{"pack.png"});
               if (inputSupplier == null) {
                  var15 = UNKNOWN_PACK;
                  break label69;
               }

               String string = resourcePackProfile.getId();
               String var10000 = Util.replaceInvalidChars(string, Identifier::isPathCharacterValid);
               Identifier identifier = Identifier.ofVanilla("pack/" + var10000 + "/" + String.valueOf(Hashing.sha1().hashUnencodedChars(string)) + "/icon");
               InputStream inputStream = (InputStream)inputSupplier.get();

               try {
                  NativeImage nativeImage = NativeImage.read(inputStream);
                  textureManager.registerTexture(identifier, new NativeImageBackedTexture(nativeImage));
                  var9 = identifier;
               } catch (Throwable var12) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (inputStream != null) {
                  inputStream.close();
               }
            } catch (Throwable var13) {
               if (resourcePack != null) {
                  try {
                     resourcePack.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (resourcePack != null) {
               resourcePack.close();
            }

            return var9;
         }

         if (resourcePack != null) {
            resourcePack.close();
         }

         return var15;
      } catch (Exception exception) {
         LOGGER.warn("Failed to load icon from pack {}", resourcePackProfile.getId(), exception);
         return UNKNOWN_PACK;
      }
   }

   private Identifier getPackIconTexture(ResourcePackProfile resourcePackProfile) {
      return (Identifier)this.iconTextures.computeIfAbsent(resourcePackProfile.getId(), (profileName) -> this.loadPackIcon(this.client.getTextureManager(), resourcePackProfile));
   }

   static {
      DROP_INFO = Text.translatable("pack.dropInfo").formatted(Formatting.GRAY);
      FOLDER_INFO = Text.translatable("pack.folderInfo");
      UNKNOWN_PACK = Identifier.ofVanilla("textures/misc/unknown_pack.png");
   }

   @Environment(EnvType.CLIENT)
   static class DirectoryWatcher implements AutoCloseable {
      private final WatchService watchService;
      private final Path path;

      public DirectoryWatcher(Path path) throws IOException {
         this.path = path;
         this.watchService = path.getFileSystem().newWatchService();

         try {
            this.watchDirectory(path);
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);

            try {
               for(Path path2 : directoryStream) {
                  if (Files.isDirectory(path2, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path2);
                  }
               }
            } catch (Throwable var6) {
               if (directoryStream != null) {
                  try {
                     directoryStream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (directoryStream != null) {
               directoryStream.close();
            }

         } catch (Exception exception) {
            this.watchService.close();
            throw exception;
         }
      }

      @Nullable
      public static DirectoryWatcher create(Path path) {
         try {
            return new DirectoryWatcher(path);
         } catch (IOException iOException) {
            PackScreen.LOGGER.warn("Failed to initialize pack directory {} monitoring", path, iOException);
            return null;
         }
      }

      private void watchDirectory(Path path) throws IOException {
         path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
      }

      public boolean pollForChange() throws IOException {
         boolean bl = false;

         WatchKey watchKey;
         while((watchKey = this.watchService.poll()) != null) {
            for(WatchEvent watchEvent : watchKey.pollEvents()) {
               bl = true;
               if (watchKey.watchable() == this.path && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                  Path path = this.path.resolve((Path)watchEvent.context());
                  if (Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
                     this.watchDirectory(path);
                  }
               }
            }

            watchKey.reset();
         }

         return bl;
      }

      public void close() throws IOException {
         this.watchService.close();
      }
   }
}
