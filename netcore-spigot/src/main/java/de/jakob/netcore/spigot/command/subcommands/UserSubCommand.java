package de.jakob.netcore.spigot.command.subcommands;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.common.util.TimeFormatter;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserSubCommand extends SubCommand {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Override
    public void onCommand(NetCore plugin, CommandSender sender, String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage(MessageFactory.of("Usage: /netcore user <name>").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            return;
        }

        String targetName = args[1];
        sender.sendMessage(MessageFactory.of("§7Loading user data...").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());

        plugin.getUserManager().loadUser(targetName).thenAccept(user -> {
            if (user == null) {
                sender.sendMessage(MessageFactory.of("§7User '§c" + targetName + " §7'not found!").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
                return;
            }

            sender.sendMessage("§7User Info: §b" + user.getName());
            sender.sendMessage("    §7UUID: §f" + user.getUUID());
            sender.sendMessage("    §7IP: §f" + user.getIPAddress());
            sender.sendMessage("    §7First Join: §f" + dateFormat.format(new Date(user.getFirstJoinTimestamp())));
            sender.sendMessage("    §7Last Join: §f" + dateFormat.format(new Date(user.getLastJoin())));
            sender.sendMessage("    §7Playtime: §f" + TimeFormatter.formatPlaytime(user.getPlayTime(), "[%d% Day(s) ][%h% Hour(s) ][%m% Minute(s) ][%s% Second(s)]"));
            sender.sendMessage("    §7Session-Time: §f" + TimeFormatter.formatPlaytime(user.getSessionTime(), "[%d% Day(s) ][%h% Hour(s) ][%m% Minute(s) ][%s% Second(s)]"));
            sender.sendMessage("    §7Settings: §f" + user.getSettings().toString());
        }).exceptionally(throwable -> {
            sender.sendMessage(MessageFactory.of("§cAn error occurred while loading user data.").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    public List<String> onTabComplete(NetCore plugin, CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
