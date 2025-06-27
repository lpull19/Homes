package org.fstpdev.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fstpdev.Homes;

public class DelHomeCommand implements CommandExecutor {
   private final Homes plugin;

   public DelHomeCommand(Homes plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getLanguageManager().getMessage("messages.player-only"));
         return true;
      }

      Player player = (Player) sender;

      if (args.length != 1) {
         String message = plugin.getLanguageManager().getMessage("messages.delhome-usage");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      String homeName = args[0].toLowerCase();

      if (this.plugin.getHomeManager().getHome(player, homeName) == null) {
         String message = plugin.getLanguageManager().getMessage("messages.delhome-not-exist");
         player.sendMessage(this.plugin.getHomeManager().colorize(message));
         return true;
      }

      this.plugin.getHomeManager().deleteHome(player, homeName);
      String message = plugin.getLanguageManager().getMessage("messages.delhome-success")
              .replace("{home}", homeName);
      player.sendMessage(this.plugin.getHomeManager().colorize(message));
      return true;
   }
}