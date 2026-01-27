package de.jakob.netcore.spigot.command;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.spigot.NetCore;
import de.jakob.netcore.spigot.command.subcommands.ConnectionSubCommand;
import de.jakob.netcore.spigot.command.subcommands.ReloadSubCommand;
import de.jakob.netcore.spigot.command.subcommands.SubCommand;
import de.jakob.netcore.spigot.command.subcommands.UserSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetCoreCommand implements TabExecutor {

    private final NetCore plugin;
    private final Map<String, SubCommand> subCommands = new HashMap<>();


    public NetCoreCommand(NetCore plugin) {
        this.plugin = plugin;
        subCommands.put("reload", new ReloadSubCommand());
        subCommands.put("connection", new ConnectionSubCommand());
        subCommands.put("user", new UserSubCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        FileConfiguration config = plugin.getConfig();

        if (!sender.hasPermission("netcore.manage")) {
            sender.sendMessage(NetCoreTranslation.NO_PERMISSION.getTranslatedString());
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(MessageFactory.of("Running NetCore Version §b" + plugin.getDescription().getVersion()).prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            sender.sendMessage(MessageFactory.of("For connection testing run /netcore §bconnection§7.").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());

            return true;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            sender.sendMessage(MessageFactory.of("This subcommand §cdoesn't §7exist!").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());
            return false;
        }

        subCommands.get(args[0]).onCommand(plugin, sender, label, args);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(subCommands.keySet());
        }

        if (args.length > 1) {

            if (!subCommands.containsKey(args[0])) {
                return null;
            }

            return subCommands.get(args[0]).onTabComplete(plugin, sender, label, args);
        }
        return null;
    }
}
