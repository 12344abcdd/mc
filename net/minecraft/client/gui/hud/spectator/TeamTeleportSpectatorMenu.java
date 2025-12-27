package net.minecraft.client.gui.hud.spectator;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class TeamTeleportSpectatorMenu implements SpectatorMenuCommandGroup, SpectatorMenuCommand {
   private static final Identifier TEXTURE = Identifier.ofVanilla("spectator/teleport_to_team");
   private static final Text TEAM_TELEPORT_TEXT = Text.translatable("spectatorMenu.team_teleport");
   private static final Text PROMPT_TEXT = Text.translatable("spectatorMenu.team_teleport.prompt");
   private final List commands;

   public TeamTeleportSpectatorMenu() {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      this.commands = getCommands(minecraftClient, minecraftClient.world.getScoreboard());
   }

   private static List getCommands(MinecraftClient client, Scoreboard scoreboard) {
      return scoreboard.getTeams().stream().flatMap((team) -> TeamTeleportSpectatorMenu.TeleportToSpecificTeamCommand.create(client, team).stream()).toList();
   }

   public List getCommands() {
      return this.commands;
   }

   public Text getPrompt() {
      return PROMPT_TEXT;
   }

   public void use(SpectatorMenu menu) {
      menu.selectElement(this);
   }

   public Text getName() {
      return TEAM_TELEPORT_TEXT;
   }

   public void renderIcon(DrawContext context, float brightness, int alpha) {
      context.drawGuiTexture(TEXTURE, 0, 0, 16, 16);
   }

   public boolean isEnabled() {
      return !this.commands.isEmpty();
   }

   @Environment(EnvType.CLIENT)
   static class TeleportToSpecificTeamCommand implements SpectatorMenuCommand {
      private final Team team;
      private final Supplier skinTexturesSupplier;
      private final List scoreboardEntries;

      private TeleportToSpecificTeamCommand(Team team, List scoreboardEntries, Supplier skinTexturesSupplier) {
         this.team = team;
         this.scoreboardEntries = scoreboardEntries;
         this.skinTexturesSupplier = skinTexturesSupplier;
      }

      public static Optional create(MinecraftClient client, Team team) {
         List<PlayerListEntry> list = new ArrayList();

         for(String string : team.getPlayerList()) {
            PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(string);
            if (playerListEntry != null && playerListEntry.getGameMode() != GameMode.SPECTATOR) {
               list.add(playerListEntry);
            }
         }

         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            GameProfile gameProfile = ((PlayerListEntry)list.get(Random.create().nextInt(list.size()))).getProfile();
            Supplier<SkinTextures> supplier = client.getSkinProvider().getSkinTexturesSupplier(gameProfile);
            return Optional.of(new TeleportToSpecificTeamCommand(team, list, supplier));
         }
      }

      public void use(SpectatorMenu menu) {
         menu.selectElement(new TeleportSpectatorMenu(this.scoreboardEntries));
      }

      public Text getName() {
         return this.team.getDisplayName();
      }

      public void renderIcon(DrawContext context, float brightness, int alpha) {
         Integer integer = this.team.getColor().getColorValue();
         if (integer != null) {
            float f = (float)(integer >> 16 & 255) / 255.0F;
            float g = (float)(integer >> 8 & 255) / 255.0F;
            float h = (float)(integer & 255) / 255.0F;
            context.fill(1, 1, 15, 15, MathHelper.packRgb(f * brightness, g * brightness, h * brightness) | alpha << 24);
         }

         context.setShaderColor(brightness, brightness, brightness, (float)alpha / 255.0F);
         PlayerSkinDrawer.draw(context, (SkinTextures)((SkinTextures)this.skinTexturesSupplier.get()), 2, 2, 12);
         context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      public boolean isEnabled() {
         return true;
      }
   }
}
