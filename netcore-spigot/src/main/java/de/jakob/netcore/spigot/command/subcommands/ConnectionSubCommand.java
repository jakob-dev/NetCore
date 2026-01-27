package de.jakob.netcore.spigot.command.subcommands;

import de.jakob.netcore.api.database.queries.Query;
import de.jakob.netcore.common.messages.MessageFactory;
import de.jakob.netcore.common.messages.NetCoreTranslation;
import de.jakob.netcore.spigot.NetCore;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.List;

public class ConnectionSubCommand extends SubCommand {
    @Override
    public void onCommand(NetCore plugin, CommandSender sender, String label, String[] args) {

        if (!sender.hasPermission("netcore.connection")) {
            sender.sendMessage(NetCoreTranslation.NO_PERMISSION.getTranslatedString());
            return;
        }

        sender.sendMessage(MessageFactory.of("Testing connection...").prefix(NetCoreTranslation.PREFIX.getTranslatedString()).build());

        long startTime = System.nanoTime();
        Query testQuery = new Query(plugin.getDatabaseManager().getGlobalDatabaseProvider(), "SELECT VERSION();");
        testQuery.executeQueryAsync().thenAccept(resultSet -> {

            try {
                long endTime = System.nanoTime();
                if (resultSet.next()) {
                    String version = resultSet.getString(1);
                    long duration = endTime - startTime;

                    sender.sendMessage("§7Database: ");
                    sender.sendMessage("    §7Status: " + (plugin.getDatabaseManager().getGlobalDatabaseProvider().isConnected() ? "§aconnected" : "§cnot connected"));
                    sender.sendMessage("    §7Provider: §b" + plugin.getDatabaseManager().getGlobalProviderType().name());
                    sender.sendMessage("    §7Version: §b" + version);
                    sender.sendMessage("    §7Query-Speed: §b" + duration + " §7ns §7[§b" + (duration / 1000000) + " §7ms]");
                    sender.sendMessage("§7Redis: ");
                    sender.sendMessage("    §7Status: " + (plugin.getRedisProvider().getRedisClient().ping().equalsIgnoreCase("PONG") ? "§aconnected" : "§cnot connected"));
                    sender.sendMessage("§7Proxy: ");
                    sender.sendMessage("    §7Status: " + "§cN/A");

                }


            } catch (SQLException e) {
                sender.sendMessage(new MessageFactory(NetCoreTranslation.PREFIX.getTranslatedString()).build() + "There has been an §cerror §7testing the connection!");
                plugin.getLogger().severe(e.getMessage());
            }
        });

    }

    @Override
    public List<String> onTabComplete(NetCore plugin, CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
