package de.jakob.netcore.spigot.tablist;

import de.jakob.netcore.common.depend.PlaceholderAPI;
import de.jakob.netcore.common.util.ChatFormatter;
import de.jakob.netcore.common.depend.LuckPerms;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class TablistManager implements Listener {

    private final NetCore plugin;
    private boolean enabled;
    private String format;

    public TablistManager(NetCore plugin) {
        this.plugin = plugin;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("Tablist.enabled", true);
        this.format = config.getString("Tablist.format", "%prefix% &7%player_name% %suffix%");
    }

    public void updateTablist(Player player) {
        if(!enabled) return;

        String output = format;

        String prefix = LuckPerms.getPrefix(player.getUniqueId());
        String suffix = LuckPerms.getSuffix(player.getUniqueId());

        output = output.replace("%prefix%", prefix);
        output = output.replace("%suffix%", suffix);
        output = output.replace("%player_name%", player.getName());

        if (PlaceholderAPI.enabled) {
            output = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, output);
        }

        player.setPlayerListName(ChatFormatter.translate(output));

        if (LuckPerms.isEnabled()) {
            try {
                int weight = LuckPerms.getWeight(player.getUniqueId());
                Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

                String teamName = String.format("nc_%06d", 100000 - weight);

                Team team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }

                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }
            } catch (Exception e) {
                // Ignore scoreboard errors (e.g. plugin disabled)
            }
        }
    }

    public void updateAll() {
        if (!enabled) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTablist(player);
        }
    }
}
