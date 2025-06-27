package org.fstpdev.commands;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.fstpdev.Homes;

public class HomeCommand implements CommandExecutor {
   private final Homes plugin;

   public HomeCommand(Homes plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getLanguageManager().getMessage("messages.player-only"));
         return true;
      }

      Player player = (Player) sender;
      Map<String, Location> homes = plugin.getHomeManager().getAllHomes(player);

      if (args.length >= 1) {
         final String inputName = args[0];
         Location targetHome = null;

         // Search for a matching home name (case-insensitive)
         for (Map.Entry<String, Location> entry : homes.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(inputName)) {
               targetHome = entry.getValue();
               break;
            }
         }

         if (targetHome != null) {
            // If a match is found, teleport
            Location startLoc = player.getLocation().getBlock().getLocation(); // snapshot for movement check
            UUID uuid = player.getUniqueId();
            final Location finalTargetHome = targetHome;
            plugin.getHomeManager().getTeleportingPlayers().put(uuid, true); // mark as teleporting

            new BukkitRunnable() {
               int countdown = 5;

               @Override
               public void run() {
                  // Check if still teleporting and hasn't moved
                  if (plugin.getHomeManager().getTeleportingPlayers().containsKey(uuid)
                          && startLoc.equals(player.getLocation().getBlock().getLocation())) {
                     if (countdown > 0) {
                        String message = plugin.getLanguageManager().getMessage("messages.teleport-countdown")
                                .replace("{countdown}", String.valueOf(countdown));
                        player.sendMessage(plugin.getHomeManager().colorize(message));
                        countdown--;
                     } else {
                        player.teleport(finalTargetHome);
                        String message = plugin.getLanguageManager().getMessage("messages.teleport-success")
                                .replace("{home}", inputName);
                        player.sendMessage(plugin.getHomeManager().colorize(message));
                        plugin.getHomeManager().getTeleportingPlayers().remove(uuid);
                        this.cancel();
                     }
                  } else {
                     plugin.getHomeManager().getTeleportingPlayers().remove(uuid);
                     String message = plugin.getLanguageManager().getMessage("messages.teleport-cancelled");
                     player.sendMessage(plugin.getHomeManager().colorize(message));
                     this.cancel();
                  }
               }
            }.runTaskTimer(plugin, 0L, 20L);
            return true;
         } else {
            // If not found, show message and fall through to open GUI
            String message = plugin.getLanguageManager().getMessage("messages.home-not-found")
                    .replace("{home}", inputName);
            player.sendMessage(plugin.getHomeManager().colorize(message));
         }
      }

      // No args, or home not found: open GUI
      this.openHomesGui(player);
      return true;
   }

   private void openHomesGui(Player player) {
      Inventory gui = Bukkit.createInventory(player, 45, "<shift:-8><glyph:homes>");

      Map<String, Location> homes = this.plugin.getHomeManager().getAllHomes(player);
      int maxHomes = this.plugin.getHomeManager().getMaxHomes(player);

      // Sort homes alphabetically to keep them in order
      List<String> sortedHomes = new ArrayList<>(homes.keySet());
      Collections.sort(sortedHomes, String.CASE_INSENSITIVE_ORDER);

      // Slot positions for beds (first row: 10-16, second row: 28-34)
      int[] bedSlots = {10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34};
      int slotIndex = 0;

      for (String homeName : sortedHomes) {
         if (slotIndex >= bedSlots.length) break; // Prevent out-of-bounds slot placement

         Location homeLocation = homes.get(homeName);

         // Create home bed item
         ItemStack bed = new ItemStack(Material.RED_BED);
         ItemMeta bedMeta = bed.getItemMeta();

         String displayName = plugin.getLanguageManager().getMessage("messages.gui.home-name")
                 .replace("{home}", homeName);
         bedMeta.setDisplayName(this.plugin.getHomeManager().colorize(displayName));

         // Format coordinates and world name
         String worldName = homeLocation.getWorld().getName();
         int x = homeLocation.getBlockX();
         int y = homeLocation.getBlockY();
         int z = homeLocation.getBlockZ();

         // Get lore from language file
         List<String> loreTemplate = plugin.getLanguageManager().getMessageList("messages.gui.home-lore");
         List<String> lore = new ArrayList<>();
         for (String line : loreTemplate) {
            String processedLine = line
                    .replace("{world}", worldName)
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(y))
                    .replace("{z}", String.valueOf(z));
            lore.add(this.plugin.getHomeManager().colorize(processedLine));
         }

         bedMeta.setLore(lore);
         bed.setItemMeta(bedMeta);
         gui.setItem(bedSlots[slotIndex], bed);

         // Create delete button (placed directly BELOW the bed)
         ItemStack dye = new ItemStack(Material.LIME_DYE);
         ItemMeta dyeMeta = dye.getItemMeta();

         String deleteName = plugin.getLanguageManager().getMessage("messages.gui.delete-name")
                 .replace("{home}", homeName);
         dyeMeta.setDisplayName(this.plugin.getHomeManager().colorize(deleteName));

         List<String> deleteLoreTemplate = plugin.getLanguageManager().getMessageList("messages.gui.delete-lore");
         List<String> deleteLore = new ArrayList<>();
         for (String line : deleteLoreTemplate) {
            deleteLore.add(this.plugin.getHomeManager().colorize(line));
         }
         dyeMeta.setLore(deleteLore);

         dye.setItemMeta(dyeMeta);
         gui.setItem(bedSlots[slotIndex] + 9, dye); // Place delete button directly under the bed

         slotIndex++; // Move to next slot in the array
      }

      // Add gray beds for empty slots (if player hasn't reached max homes)
      while (slotIndex < maxHomes && slotIndex < bedSlots.length) {
         ItemStack grayBed = new ItemStack(Material.GRAY_BED);
         ItemMeta grayBedMeta = grayBed.getItemMeta();

         String emptyName = plugin.getLanguageManager().getMessage("messages.gui.empty-home-name");
         grayBedMeta.setDisplayName(this.plugin.getHomeManager().colorize(emptyName));

         List<String> emptyLoreTemplate = plugin.getLanguageManager().getMessageList("messages.gui.empty-home-lore");
         List<String> emptyLore = new ArrayList<>();
         for (String line : emptyLoreTemplate) {
            emptyLore.add(this.plugin.getHomeManager().colorize(line));
         }
         grayBedMeta.setLore(emptyLore);

         grayBed.setItemMeta(grayBedMeta);
         gui.setItem(bedSlots[slotIndex], grayBed);

         slotIndex++;
      }

      player.openInventory(gui);
   }
}