package de.jakob.netcore.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.jakob.netcore.velocity.NetCore;

public class ConnectionListener {

    private final NetCore plugin;

    public ConnectionListener(NetCore plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        plugin.getUserManager().handleProxyLogin(player.getUniqueId(), player.getUsername(), player.getRemoteAddress().getHostName()).thenRun(() ->
                plugin.getLogger().info("Proxy join [{}]", player.getUsername()));
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        Player player = event.getPlayer();
        plugin.getUserManager().handleProxyQuit(player.getUniqueId());
    }

}
