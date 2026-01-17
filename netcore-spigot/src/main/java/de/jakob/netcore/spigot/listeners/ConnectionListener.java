package de.jakob.netcore.spigot.listeners;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final NetCore plugin;

    public ConnectionListener(NetCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getScoreboardManager().handleJoin(player);

        if (plugin.getChatManager().isEnabled()) {
            event.setJoinMessage(new MessageFactory(plugin.getConfig().getString("Chat.join-message")).replace("%player_name%", player.getName()).build());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getScoreboardManager().handleQuit(player);

        if (plugin.getChatManager().isEnabled()) {
            event.setQuitMessage(new MessageFactory(plugin.getConfig().getString("Chat.quit-message")).replace("%player_name%", player.getName()).build());
        }
    }

}
