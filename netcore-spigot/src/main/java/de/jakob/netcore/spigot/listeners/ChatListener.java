package de.jakob.netcore.spigot.listeners;

import de.jakob.netcore.common.util.ChatFormatter;
import de.jakob.netcore.spigot.NetCore;
import de.jakob.netcore.spigot.chat.ChatManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.entity.Player;
import java.util.List;

public class ChatListener implements Listener {

    private final NetCore plugin;

    public ChatListener(NetCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        if (event.getPlayer().hasPermission("netcore.chat.bypass")) return;
        ChatManager chatManager = plugin.getChatManager();
        event.getCommands().removeIf(chatManager::isCommandBlocked);
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("netcore.chat.bypass")) return;
        ChatManager chatManager = plugin.getChatManager();
        if (chatManager.isCommandBlocked(event.getMessage())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(chatManager.getBlockedCommandMessage());
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) return;

        if (player.hasPermission("netcore.chat.bypass")) return;
        
        ChatManager chatManager = plugin.getChatManager();
        String buffer = event.getBuffer();

        if (buffer.startsWith("/") && !buffer.contains(" ")) {
            List<String> completions = event.getCompletions();
            completions.removeIf(cmd -> chatManager.isCommandBlocked("/" + cmd));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        ChatManager chatManager = plugin.getChatManager();

        if (!chatManager.canChat(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            return;
        }

        if (event.getPlayer().hasPermission("netcore.chat.color")) {
            String translated = ChatFormatter.translate(event.getMessage());
            event.setMessage(translated);
        }

        chatManager.onChat(event.getPlayer(), event.getMessage());

        String format = chatManager.getFormat(event.getPlayer());
        try {
            event.setFormat(format);
        } catch (Exception e) {
            event.setFormat("<%1$s> %2$s");
        }
    }
}