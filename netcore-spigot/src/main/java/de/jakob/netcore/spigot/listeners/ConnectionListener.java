package de.jakob.netcore.spigot.listeners;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final NetCore plugin;

    public ConnectionListener(NetCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {

        try {
            plugin.getUserManager().handleServerLogin(event.getUniqueId()).join();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load user data for " + event.getName());
            e.printStackTrace();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Failed to load your data. Please try again.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getScoreboardManager().handleJoin(player);
        plugin.getTablistManager().updateTablist(player);

        if (plugin.getChatManager().isEnabled()) {
            event.setJoinMessage(new MessageFactory(plugin.getConfig().getString("Chat.join-message")).replace("%player_name%", player.getName()).build());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove from local cache
        plugin.getUserManager().handleServerQuit(player.getUniqueId());

        plugin.getScoreboardManager().handleQuit(player);

        if (plugin.getChatManager().isEnabled()) {
            event.setQuitMessage(new MessageFactory(plugin.getConfig().getString("Chat.quit-message")).replace("%player_name%", player.getName()).build());
        }
    }

}