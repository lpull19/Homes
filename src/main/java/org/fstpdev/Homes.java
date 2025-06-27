package org.fstpdev;

import org.bukkit.plugin.java.JavaPlugin;
import org.fstpdev.commands.DelHomeCommand;
import org.fstpdev.commands.HomeCommand;
import org.fstpdev.commands.tabcompleter.HomeTabCompleter;
import org.fstpdev.commands.SetHomeCommand;
import org.fstpdev.listeners.HomeGuiListener;
import org.fstpdev.listeners.PlayerJoinListener;
import org.fstpdev.managers.HomeManager;
import org.fstpdev.managers.LanguageManager;

public class Homes extends JavaPlugin {
   private HomeManager homeManager;
   private LanguageManager languageManager;


   public void onEnable() {
      this.saveDefaultConfig();
      this.homeManager = new HomeManager(this);
      this.languageManager = new LanguageManager(this);
      this.getCommand("home").setExecutor(new HomeCommand(this));
      this.getCommand("sethome").setExecutor(new SetHomeCommand(this));
      this.getCommand("delhome").setExecutor(new DelHomeCommand(this));
      getCommand("home").setTabCompleter(new HomeTabCompleter(this));
      this.getServer().getPluginManager().registerEvents(new HomeGuiListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
      this.getLogger().info("Homes has been enabled!");

   }

   public void onDisable() {
      this.getLogger().info("Homes has been disabled!");
   }

   public LanguageManager getLanguageManager() {
      return languageManager;
   }

   public HomeManager getHomeManager() {
      return this.homeManager;
   }
}
