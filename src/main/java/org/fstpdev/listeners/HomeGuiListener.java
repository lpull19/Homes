package org.fstpdev.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.fstpdev.Homes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeGuiListener implements Listener {
    private final Homes plugin;
    private final Map<UUID, Location> teleportingPlayers = new HashMap<>();
    private final Map<UUID, Boolean> awaitingHomeName = new HashMap<>();

    public HomeGuiListener(Homes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals("<shift:-8><glyph:homes>")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GRAY_BED) {
                if (!player.getWorld().getName().equals("world")) {
                    String message = plugin.getLanguageManager().getMessage("messages.gui-overworld-only");
                    player.sendMessage(plugin.getHomeManager().colorize(message));
                    return;
                }
                // Player clicked an empty home slot -> Ask for a home name
                askForHomeName(player);
            } else if (clickedItem.getType() == Material.RED_BED) {
                // Player clicked an existing home -> Teleport
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) return;

                String homeName = ChatColor.stripColor(meta.getDisplayName());
                handleTeleport(player, homeName);
            } else if (clickedItem.getType() == Material.LIME_DYE) {
                // Player clicked delete home option
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) return;

                String homeName = ChatColor.stripColor(meta.getDisplayName());
                handleDeleteHome(player, homeName);
            }
        }
    }

    /** Asks the player to enter a home name in chat */
    private void askForHomeName(Player player) {
        player.closeInventory();
        String message = plugin.getLanguageManager().getMessage("messages.gui-home-name-prompt");
        player.sendMessage(plugin.getHomeManager().colorize(message));
        awaitingHomeName.put(player.getUniqueId(), true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingHomeName.containsKey(uuid)) return; // Ignore if player isn't naming a home

        event.setCancelled(true); // Prevent message from appearing in chat
        String homeName = event.getMessage().trim(); // Trim spaces

        // Fix: Strip any color codes from the name before checking length
        String strippedName = ChatColor.stripColor(homeName);

        // Debug: Check actual length of input
        System.out.println("Home name received (raw): " + homeName + " | Length: " + homeName.length());
        System.out.println("Home name received (stripped): " + strippedName + " | Length: " + strippedName.length());

        // Allow "cancel" or "stop" to exit
        if (strippedName.equalsIgnoreCase("cancel") || strippedName.equalsIgnoreCase("stop")) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-cancelled");
            player.sendMessage(plugin.getHomeManager().colorize(message));
            awaitingHomeName.remove(uuid);
            return;
        }

        // Fix: Ensure the length check works correctly (using stripped name)
        if (strippedName.length() > 16) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-name-too-long");
            player.sendMessage(plugin.getHomeManager().colorize(message));
            return;
        }

        // Fix: Make sure duplicate homes are checked properly
        if (plugin.getHomeManager().getAllHomes(player).containsKey(strippedName)) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-already-exists");
            player.sendMessage(plugin.getHomeManager().colorize(message));
            return;
        }

        if (plugin.getHomeManager().getHomeCount(player) >= plugin.getHomeManager().getMaxHomes(player)) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-limit-reached");
            player.sendMessage(plugin.getHomeManager().colorize(message));
            return;
        }

        // Save home with proper capitalization
        plugin.getHomeManager().setHome(player, strippedName, player.getLocation());
        String message = plugin.getLanguageManager().getMessage("messages.gui-home-saved")
                .replace("{home}", homeName);
        player.sendMessage(plugin.getHomeManager().colorize(message));
        awaitingHomeName.remove(uuid);

        // Reopen GUI after setting the home
        Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("home"));
    }

    /** Handles teleportation to a home */
    private void handleTeleport(final Player player, String homeName) {
        final Location home = this.plugin.getHomeManager().getHome(player, homeName);
        if (home == null) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-not-found");
            player.sendMessage(this.plugin.getHomeManager().colorize(message));
            return;
        }

        player.closeInventory();

        final Location startLoc = player.getLocation();
        this.teleportingPlayers.put(player.getUniqueId(), startLoc);

        new BukkitRunnable() {
            int countdown = 5;

            public void run() {
                if (teleportingPlayers.containsKey(player.getUniqueId()) &&
                        startLoc.getBlock().getLocation().equals(player.getLocation().getBlock().getLocation())) {
                    if (countdown > 0) {
                        String message = plugin.getLanguageManager().getMessage("messages.teleport-countdown")
                                .replace("{countdown}", String.valueOf(countdown));
                        player.sendMessage(plugin.getHomeManager().colorize(message));
                        countdown--;
                    } else {
                        player.teleport(home);
                        String message = plugin.getLanguageManager().getMessage("messages.teleport-success")
                                .replace("{home}", homeName);
                        player.sendMessage(plugin.getHomeManager().colorize(message));
                        teleportingPlayers.remove(player.getUniqueId());
                        this.cancel();
                    }
                } else {
                    teleportingPlayers.remove(player.getUniqueId());
                    String message = plugin.getLanguageManager().getMessage("messages.teleport-cancelled");
                    player.sendMessage(plugin.getHomeManager().colorize(message));
                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

    /** Handles deleting a home */
    private void handleDeleteHome(Player player, String homeName) {
        // Fix: Ensure we strip color codes before checking if the home exists
        String strippedName = ChatColor.stripColor(homeName).replace("Delete ", "");

        // Debug: Print home name comparison
        System.out.println("Trying to delete home: " + strippedName);

        // Check if the home exists in the stored data
        Location home = this.plugin.getHomeManager().getHome(player, strippedName);
        if (home == null) {
            String message = plugin.getLanguageManager().getMessage("messages.gui-home-not-found");
            player.sendMessage(this.plugin.getHomeManager().colorize(message));
            return;
        }

        // Delete the home
        this.plugin.getHomeManager().deleteHome(player, strippedName);
        String message = plugin.getLanguageManager().getMessage("messages.gui-home-deleted")
                .replace("{home}", strippedName);
        player.sendMessage(this.plugin.getHomeManager().colorize(message));

        // Refresh the GUI after deleting
        Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("home"));
    }
}