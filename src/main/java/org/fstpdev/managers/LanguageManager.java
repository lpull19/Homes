package org.fstpdev.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.fstpdev.Homes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;

public class LanguageManager {
    private final Homes plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public LanguageManager(Homes plugin) {
        this.plugin = plugin;
        this.setupLanguageFile();
    }

    private void setupLanguageFile() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");

        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            plugin.saveResource("lang.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("lang.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            langConfig.setDefaults(defConfig);
        }
    }

    public void reloadLanguageFile() {
        if (langFile == null) {
            langFile = new File(plugin.getDataFolder(), "lang.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defConfigStream = plugin.getResource("lang.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            langConfig.setDefaults(defConfig);
        }
    }

    public void saveLanguageFile() {
        if (langConfig == null || langFile == null) {
            return;
        }
        try {
            langConfig.save(langFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + langFile, ex);
        }
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Missing language key: " + path);
            return "Missing message: " + path;
        }
        return message;
    }

    public String getMessage(String path, String defaultMessage) {
        String message = langConfig.getString(path, defaultMessage);
        return message;
    }

    public List<String> getMessageList(String path) {
        List<String> messages = langConfig.getStringList(path);
        if (messages.isEmpty()) {
            plugin.getLogger().warning("Missing or empty language list: " + path);
        }
        return messages;
    }
}