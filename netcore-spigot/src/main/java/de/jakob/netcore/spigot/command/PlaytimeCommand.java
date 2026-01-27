package de.jakob.netcore.spigot.command;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.common.util.TimeFormatter;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PlaytimeCommand implements CommandExecutor {

    private final NetCore plugin;

    public PlaytimeCommand(NetCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String targetName;

        if (args.length == 0) {
            if (!sender.hasPermission("netcore.playtime")) {
                sender.sendMessage(NetCoreTranslation.NO_PERMISSION.getTranslatedString());
                return true;
            }
            targetName = sender.getName();
        } else if (args.length == 1) {
            if (!sender.hasPermission("netcore.playtime.others")) {
                sender.sendMessage(NetCoreTranslation.NO_PERMISSION.getTranslatedString());
                return true;
            }
            targetName = args[0];
        } else {
            sender.sendMessage(MessageFactory.of("Usage: /playtime <player>").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            return true;
        }

        plugin.getUserManager().loadUser(targetName).thenAccept(user -> {
            if (user == null) {
                sender.sendMessage(MessageFactory.of("§7The playtime could §cnot §7be loaded.").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
                return;
            }
            sender.sendMessage(NetCoreTranslation.PLAYTIME.getCompleteTranslatedString().replace("%playtime%",
                    TimeFormatter.formatPlaytime(user.getPlayTime(),
                            plugin.getConfig().getString("Users.playtime-format", "[%d% Day(s) ][%h% Hour(s) ][%m% Minute(s) ][%s% Second(s)]"))));
        }).exceptionally(throwable -> {
            sender.sendMessage(MessageFactory.of("§cAn error occurred while loading the playtime.").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            throwable.printStackTrace();
            return null;
        });

        return true;
    }
}
