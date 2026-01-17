package de.jakob.netcore.spigot.scoreboard;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PacketScoreboard {

    private final Player bukkitPlayer;
    private final String objectiveId;

    private final Objective dummyObjective;

    private final Map<Integer, String> lineCache = new HashMap<>();

    public PacketScoreboard(Player player, String id, String title) {
        this.bukkitPlayer = player;
        this.objectiveId = id;

        Scoreboard dummyScoreboard = new Scoreboard();
        this.dummyObjective = new Objective(
                dummyScoreboard,
                objectiveId,
                ObjectiveCriteria.DUMMY,
                Component.literal(title),
                ObjectiveCriteria.RenderType.INTEGER,
                false,
                BlankFormat.INSTANCE
        );
    }

    public void create() {
        lineCache.clear();

        ClientboundSetObjectivePacket removePacket = new ClientboundSetObjectivePacket(dummyObjective, 1);
        ClientboundSetObjectivePacket createPacket = new ClientboundSetObjectivePacket(dummyObjective, 0);
        ClientboundSetDisplayObjectivePacket displayPacket = new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, dummyObjective);

        sendPacket(removePacket);
        sendPacket(createPacket);
        sendPacket(displayPacket);
    }


    public void setLine(int lineIndex, String text) {
        if (lineCache.containsKey(lineIndex) && lineCache.get(lineIndex).equals(text)) {
            return;
        }

        lineCache.put(lineIndex, text);

        String scoreHolderName = String.valueOf(lineIndex);

        ClientboundSetScorePacket scorePacket = new ClientboundSetScorePacket(
                scoreHolderName,
                objectiveId,
                lineIndex,
                Optional.of(Component.literal(text)),
                Optional.empty()
        );

        sendPacket(scorePacket);
    }

    public void remove() {
        ClientboundSetObjectivePacket removePacket = new ClientboundSetObjectivePacket(dummyObjective, 1);
        sendPacket(removePacket);
        lineCache.clear();
    }

    public UUID getPlayerUUID() {
        return bukkitPlayer.getUniqueId();
    }

    private void sendPacket(Packet<?> packet) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) bukkitPlayer).getHandle().connection;
        connection.send(packet);
    }
}