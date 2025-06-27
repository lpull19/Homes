package org.fstpdev.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.fstpdev.Homes;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {
   private final Homes plugin;
   private final Map<UUID, FileConfiguration> playerData = new HashMap<>();
   private final File dataFolder;
   private final Map<UUID, Boolean> teleportingPlayers = new HashMap<>();

   // Configuration for home permissions
   private final Map<String, Integer> permissionToMaxHomes = new HashMap<>();
   private int defaultMaxHomes = 2;

   public HomeManager(Homes plugin) {
      this.plugin = plugin;
      this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
      if (!this.dataFolder.exists()) {
         this.dataFolder.mkdirs();
      }

      // Load permission configuration
      loadPermissionConfig();
   }

   /**
    * Load permission configuration from config.yml
    */
   private void loadPermissionConfig() {
      FileConfiguration config = plugin.getConfig();

      // Load default max homes
      this.defaultMaxHomes = config.getInt("default-max-homes", 2);

      // Load permission mappings
      ConfigurationSection permissionsSection = config.getConfigurationSection("permissions");
      if (permissionsSection != null) {
         for (String permission : permissionsSection.getKeys(false)) {
            int maxHomes = permissionsSection.getInt(permission + ".max-homes", defaultMaxHomes);
            permissionToMaxHomes.put(permission, maxHomes);
         }
      }

      // Log loaded permissions for debugging
      plugin.getLogger().info("Loaded " + permissionToMaxHomes.size() + " home permission mappings");
      plugin.getLogger().info("Default max homes: " + defaultMaxHomes);
   }

   /**
    * Reload permission configuration (useful for /reload commands)
    */
   public void reloadPermissionConfig() {
      permissionToMaxHomes.clear();
      loadPermissionConfig();
   }

   /**
    * Get the maximum number of homes a player can have based on their permissions
    */
   public int getMaxHomes(Player player) {
      // Find the highest max-homes value from all permissions the player has
      int maxHomes = defaultMaxHomes;

      for (Map.Entry<String, Integer> entry : permissionToMaxHomes.entrySet()) {
         if (player.hasPermission(entry.getKey()) && entry.getValue() > maxHomes) {
            maxHomes = entry.getValue();
         }
      }

      return maxHomes;
   }

   /** Save a home by name */
   public void setHome(Player player, String name, Location location) {
      FileConfiguration config = getPlayerConfig(player.getUniqueId());
      config.set("homes." + name.toLowerCase() + ".world", location.getWorld().getName());
      config.set("homes." + name.toLowerCase() + ".x", location.getX());
      config.set("homes." + name.toLowerCase() + ".y", location.getY());
      config.set("homes." + name.toLowerCase() + ".z", location.getZ());
      config.set("homes." + name.toLowerCase() + ".yaw", location.getYaw());
      config.set("homes." + name.toLowerCase() + ".pitch", location.getPitch());
      savePlayerConfig(player.getUniqueId());
   }

   /** Get a home by name */
   public Location getHome(Player player, String name) {
      FileConfiguration config = getPlayerConfig(player.getUniqueId());
      if (!config.contains("homes." + name.toLowerCase())) return null;

      return new Location(
              Bukkit.getWorld(config.getString("homes." + name.toLowerCase() + ".world")),
              config.getDouble("homes." + name.toLowerCase() + ".x"),
              config.getDouble("homes." + name.toLowerCase() + ".y"),
              config.getDouble("homes." + name.toLowerCase() + ".z"),
              (float) config.getDouble("homes." + name.toLowerCase() + ".yaw"),
              (float) config.getDouble("homes." + name.toLowerCase() + ".pitch")
      );
   }

   /** Delete a home */
   public void deleteHome(Player player, String name) {
      FileConfiguration config = getPlayerConfig(player.getUniqueId());
      config.set("homes." + name.toLowerCase(), null);
      savePlayerConfig(player.getUniqueId());
   }

   /** Get the number of homes a player has */
   public int getHomeCount(Player player) {
      FileConfiguration config = getPlayerConfig(player.getUniqueId());
      return config.contains("homes") ? config.getConfigurationSection("homes").getKeys(false).size() : 0;
   }

   /** Get all homes (name -> location) */
   public Map<String, Location> getAllHomes(Player player) {
      FileConfiguration config = getPlayerConfig(player.getUniqueId());
      Map<String, Location> homes = new HashMap<>();

      if (config.contains("homes")) {
         for (String name : config.getConfigurationSection("homes").getKeys(false)) {
            homes.put(name, getHome(player, name));
         }
      }
      return homes;
   }

   /** Load player config */
   private FileConfiguration getPlayerConfig(UUID uuid) {
      if (playerData.containsKey(uuid)) return playerData.get(uuid);

      File playerFile = new File(dataFolder, uuid.toString() + ".yml");
      FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
      playerData.put(uuid, config);
      return config;
   }

   public Map<UUID, Boolean> getTeleportingPlayers() {
      return teleportingPlayers;
   }

   /** Save player config */
   private void savePlayerConfig(UUID uuid) {
      try {
         File playerFile = new File(dataFolder, uuid.toString() + ".yml");
         playerData.get(uuid).save(playerFile);
      } catch (IOException e) {
         plugin.getLogger().severe("Could not save player data for " + uuid);
         e.printStackTrace();
      }
   }

   public String colorize(String message) {
      return ChatColor.translateAlternateColorCodes('&', message);
   }

   /**
    * Get all configured permissions and their max homes (for debugging/admin commands)
    */
   public Map<String, Integer> getPermissionMappings() {
      return Collections.unmodifiableMap(permissionToMaxHomes);
   }

   /**
    * Get the default max homes value
    */
   public int getDefaultMaxHomes() {
      return defaultMaxHomes;
   }
}