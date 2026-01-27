package de.jakob.netcore.spigot.chat;

import de.jakob.netcore.common.depend.PlaceholderAPI;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.common.util.ChatFormatter;
import de.jakob.netcore.common.depend.LuckPerms;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatManager {

    private final NetCore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, String> lastMessages = new HashMap<>();

    private boolean enabled;
    private long cooldownTime;
    private boolean antiSpam;
    private boolean antiCaps;
    private String format;

    private boolean blockedCommandsWhitelist;
    private String blockedCommandsMessage;
    private List<String> blockedCommandsList;

    public ChatManager(NetCore plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        this.enabled = plugin.getConfig().getBoolean("Chat.enabled", true);
        this.cooldownTime = plugin.getConfig().getLong("Chat.cooldown", 3) * 1000;
        this.antiSpam = plugin.getConfig().getBoolean("Chat.anti-spam", true);
        this.antiCaps = plugin.getConfig().getBoolean("Chat.anti-caps", true);
        this.format = plugin.getConfig().getString("Chat.format", "%prefix% §7%player_name% §8» §7%message%");

        this.blockedCommandsWhitelist = plugin.getConfig().getBoolean("Chat.blocked-commands.whitelist", true);
        this.blockedCommandsMessage = ChatFormatter.translate(plugin.getConfig().getString("Chat.blocked-commands.message", "&cYou are not permitted to use this command!"));
        this.blockedCommandsList = plugin.getConfig().getStringList("Chat.blocked-commands.commands");
        this.blockedCommandsList.replaceAll(cmd -> cmd.toLowerCase().startsWith("/") ? cmd.substring(1).toLowerCase() : cmd.toLowerCase());
    }


    public boolean canChat(Player player, String message) {
        if (player.hasPermission("netcore.chat.bypass")) return true;

        if (!enabled) return true;

        // Cooldown
        if (cooldowns.containsKey(player.getUniqueId())) {
            long remaining = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
            if (remaining > 0) {
                player.sendMessage(NetCoreTranslation.CHAT_COOLDOWN.getCompleteTranslatedString().replace("%duration%", String.format("%.1f", remaining / 1000.0)));
                return false;
            }
        }

        if (antiSpam) {
            String last = lastMessages.get(player.getUniqueId());
            if (last != null && last.equalsIgnoreCase(message)) {
                player.sendMessage(NetCoreTranslation.SAME_MESSAGE.getCompleteTranslatedString());
                return false;
            }
        }

        if (antiCaps && isCaps(message)) {
            player.sendMessage(NetCoreTranslation.NO_CAPS.getCompleteTranslatedString());
            return false;
        }

        return true;
    }

    public void onChat(Player player, String message) {
        if (player.hasPermission("netcore.chat.bypass")) return;
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
        lastMessages.put(player.getUniqueId(), message);
    }

    private boolean isCaps(String message) {
        if (message.length() < 5) return false;
        int caps = 0;
        int letters = 0;
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) caps++;
            }
        }
        return letters > 0 && (double) caps / letters > 0.5;
    }

    public String getFormat(Player player) {
        String output = format;

        output = output.replace("%player_name%", "%1$s");
        output = output.replace("%message%", "%2$s");

        String prefix = LuckPerms.getPrefix(player.getUniqueId());
        output = output.replace("%prefix%", prefix);

        if (PlaceholderAPI.enabled) {
            output = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, output);
        }

        return ChatFormatter.translate(output);
    }

    public boolean isCommandBlocked(String commandLine) {
        if (commandLine.startsWith("/")) {
            commandLine = commandLine.substring(1);
        }
        String[] parts = commandLine.split(" ");
        String command = parts[0].toLowerCase();

        boolean listed = blockedCommandsList.contains(command);

        if (blockedCommandsWhitelist) {
            return !listed;
        }

        return listed;
    }

    public String getBlockedCommandMessage() {
        return blockedCommandsMessage;
    }

    public boolean isEnabled() {
        return enabled;
    }
}