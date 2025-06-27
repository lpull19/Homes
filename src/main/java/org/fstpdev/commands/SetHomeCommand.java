package org.fstpdev.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fstpdev.Homes;

public class SetHomeCommand implements CommandExecutor {
   private final Homes plugin;

   public SetHomeCommand(Homes plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getLanguageManager().getMessage("messages.player-only"));
         return true;
      }

      Player player = (Player) sender;

      if (!player.getWorld().getName().equals("world")) {
         String message = plugin.getLanguageManager().getMessage("messages.sethome-overworld-only");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      int currentHomes = this.plugin.getHomeManager().getHomeCount(player);
      int maxHomes = this.plugin.getHomeManager().getMaxHomes(player);

      if (currentHomes >= maxHomes) {
         String message = plugin.getLanguageManager().getMessage("messages.sethome-max-homes");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      if (args.length == 0) {
         String message = plugin.getLanguageManager().getMessage("messages.sethome-usage");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      String homeName = args[0].toLowerCase();

      if (homeName.length() > 16) {
         String message = plugin.getLanguageManager().getMessage("messages.sethome-name-too-long");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      if (this.plugin.getHomeManager().getAllHomes(player).containsKey(homeName)) {
         String message = plugin.getLanguageManager().getMessage("messages.sethome-already-exists");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      this.plugin.getHomeManager().setHome(player, homeName, player.getLocation());
      String message = plugin.getLanguageManager().getMessage("messages.sethome-success")
              .replace("{home}", homeName);
      player.sendMessage(this.plugin.getHomeManager().colorize(message));

      // Reopen the GUI after setting a home
      Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("home"));
      return true;
   }
}