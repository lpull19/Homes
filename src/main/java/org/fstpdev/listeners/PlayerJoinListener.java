package org.fstpdev.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.fstpdev.Homes;

public class PlayerJoinListener implements Listener {
   private final Homes plugin;

   public PlayerJoinListener(Homes plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      this.plugin.getHomeManager().getMaxHomes(event.getPlayer());
   }
}
