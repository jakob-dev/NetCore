package de.jakob.netcore.spigot.scoreboard;

import de.jakob.netcore.common.util.ChatFormatter;
import de.jakob.netcore.common.util.TimeFormatter;
import de.jakob.netcore.common.depend.PlaceholderAPI;
import de.jakob.netcore.api.user.User;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private final NetCore plugin;
    private boolean enabled;
    private BukkitTask bukkitTask;

    private final Map<UUID, PacketScoreboard> scoreboardMap;
    private String scoreboardTitle;
    private List<String> scoreboardLines;
    private String playtimeFormat;


    public ScoreboardManager(NetCore plugin) {
        this.plugin = plugin;
        scoreboardMap = new ConcurrentHashMap<>();
        reloadConfig();
    }

    public void reloadConfig() {
        onDisable();
        this.enabled = plugin.getConfig().getBoolean("Scoreboard.enabled");
        if (!enabled) {
            return;
        }
        this.scoreboardTitle = ChatFormatter.translate(plugin.getConfig().getString("Scoreboard.title", "§e§lSERVER"));
        this.scoreboardLines = plugin.getConfig().getStringList("Scoreboard.scores");
        this.playtimeFormat = plugin.getConfig().getString("Scoreboard.playtime-format", "[%d% Day(s) ][%h% Hour(s) ][%m% Minute(s) ][%s% Second(s)]");
        long interval = plugin.getConfig().getLong("Scoreboard.update-interval", 20L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            handleJoin(player);
        }

        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20L, interval);

    }

    public void handleJoin(Player player) {
        if (!enabled) {
            return;
        }
        String objectiveId = "netcore_" + player.getUniqueId().toString().substring(0, 8);
        PacketScoreboard scoreboard = new PacketScoreboard(player, objectiveId, scoreboardTitle);
        scoreboard.create();
        scoreboardMap.put(player.getUniqueId(), scoreboard);
        updateBoard(scoreboard);

    }

    public void handleQuit(Player player) {
        PacketScoreboard board = scoreboardMap.remove(player.getUniqueId());
        if (board != null) {
            board.remove();
        }
    }


    private void updateAll() {
        for (PacketScoreboard board : scoreboardMap.values()) {
            updateBoard(board);
        }
    }

    private void updateBoard(PacketScoreboard board) {
        Player player = Bukkit.getPlayer(board.getPlayerUUID());
        if (player == null || !player.isOnline()) return;

        int score = scoreboardLines.size();

        for (String line : scoreboardLines) {
            String text = line;

            if (text.contains("%playtime%")) {
                User user = plugin.getUserManager().getCachedUser(player.getUniqueId());
                if (user != null) {
                    text = text.replace("%playtime%", TimeFormatter.formatPlaytime(user.getPlayTime(), playtimeFormat));
                } else {
                    text = text.replace("%playtime%", "Loading...");
                }
            }

            if (PlaceholderAPI.enabled) {
                try {
                    text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
                } catch (Exception e) {
                    plugin.getLogger().warning(e.getMessage());
                }
            }

            text = ChatFormatter.translate(text);

            board.setLine(score, text);
            score--;
        }
    }


    public void onDisable() {
        if (scoreboardMap != null) {
            scoreboardMap.values().forEach(PacketScoreboard::remove);
            scoreboardMap.clear();
        }
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
    }
}