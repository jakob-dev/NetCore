package de.jakob.netcore.spigot.command.subcommands;

import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ReloadSubCommand extends SubCommand {
    @Override
    public void onCommand(NetCore plugin, CommandSender sender, String label, String[] args) {


        if (!sender.hasPermission("netcore.manage")) {
            sender.sendMessage(NetCoreTranslation.NO_PERMISSION.getCompleteTranslatedString());
            return;
        }

        plugin.reload();
        sender.sendMessage(MessageFactory.of("The plugin has been reloaded.").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());

    }

    @Override
    public List<String> onTabComplete(NetCore plugin, CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
