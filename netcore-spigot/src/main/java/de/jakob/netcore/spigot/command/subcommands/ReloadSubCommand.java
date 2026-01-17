package de.jakob.netcore.spigot.command.subcommands;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ReloadSubCommand extends SubCommand {
    @Override
    public void onCommand(NetCore plugin, CommandSender sender, String label, String[] args) {

        FileConfiguration config = plugin.getConfig();

        if (!sender.hasPermission("netcore.manage")) {
            sender.sendMessage(new MessageFactory(config.getString("Messages.no-permission")).prefix(config.getString("Messages.prefix")).build());

            return;
        }

        plugin.reload();
        sender.sendMessage(MessageFactory.of("The plugin has been reloaded!").prefix(config.getString("Messages.prefix")).build());

    }

    @Override
    public List<String> onTabComplete(NetCore plugin, CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
