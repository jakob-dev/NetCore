package de.jakob.netcore.spigot.command.subcommands;

import de.jakob.netcore.spigot.NetCore;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    public abstract void onCommand(NetCore plugin, CommandSender sender, String label, String[] args);

    public abstract List<String> onTabComplete(NetCore plugin, CommandSender sender, String label, String[] args);

}
