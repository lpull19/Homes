package org.fstpdev.commands.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.fstpdev.Homes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomeTabCompleter implements TabCompleter {

    private final Homes plugin;

    public HomeTabCompleter(Homes plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        Player player = (Player) sender;

        if (args.length == 1) {
            Map<String, ?> homes = plugin.getHomeManager().getAllHomes(player);
            List<String> suggestions = new ArrayList<>();

            for (String homeName : homes.keySet()) {
                if (homeName.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(homeName);
                }
            }

            Collections.sort(suggestions, String.CASE_INSENSITIVE_ORDER);
            return suggestions;
        }

        return Collections.emptyList();
    }
}