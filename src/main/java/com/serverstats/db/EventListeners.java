package com.serverstats.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import com.serverstats.db.entities.ServerChatMessageEntity;
import com.serverstats.db.utils.PlayerPosition;

public final class EventListeners {

    private EventListeners() {}

    private static boolean doEveryFewSeconds(final int seconds, final long uptimeTicks) {
        return uptimeTicks > 0 && uptimeTicks % (seconds * 20) == 0;
    }

    public static ServerTickEvents.EndTick onServerUptimeIncreased() {
        return server -> {
            var uptimeTicks = DatabaseWrapper.getInMemoryData().getGlobal().getUptimeTicks();
            DatabaseWrapper.getInMemoryData().getGlobal().setUptimeTicks(++uptimeTicks);

            var players = server.getPlayerManager().getPlayerList();
            onTickElapsedForUsers(players);

            commitOftenUpdatedValues(uptimeTicks, players);
        };
    }

    private static void onTickElapsedForUsers(final List<ServerPlayerEntity> players) {
        for (final ServerPlayerEntity player : players) {
            final var userStats = DatabaseWrapper.getInMemoryData().getUser(player);

            // dimension check
            final String dimension = player.getEntityWorld().getRegistryKey().getValue().getPath();
            switch (dimension) {
                case "overworld" -> userStats.setOverworldTicks(userStats.getOverworldTicks() + 1);
                case "the_nether" -> userStats.setNetherTicks(userStats.getNetherTicks() + 1);
                case "the_end" -> userStats.setEndTicks(userStats.getEndTicks() + 1);
                default -> userStats.setAetherTicks(userStats.getAetherTicks() + 1);
            }

            if (player.isAlive()) {
                // lifespan check
                userStats.setAliveTicks(userStats.getAliveTicks() + 1);

                // distance check
                final PlayerPosition lastPos = DatabaseWrapper.getLastKnownPosition(player);
                if (lastPos != null && dimension.equals(lastPos.getDimension())) {
                    final double extraDistance = distanceBetweenPoints(lastPos, player.getPos());
                    if (extraDistance < 20d) { // more than 20 distance in 1 tick can mean a tp, cheating etc.
                        userStats.setDistanceTraveled(userStats.getDistanceTraveled() + extraDistance);
                    }
                }
                DatabaseWrapper.setLastKnownPosition(player, new PlayerPosition(player.getPos(), dimension));
            }
        }
    }

    private static double distanceBetweenPoints(final PlayerPosition lastPos, final Vec3d currentPos) {
        return Math.sqrt(
                (lastPos.getX() - currentPos.getX()) * (lastPos.getX() - currentPos.getX()) +
                (lastPos.getY() - currentPos.getY()) * (lastPos.getY() - currentPos.getY()) +
                (lastPos.getZ() - currentPos.getZ()) * (lastPos.getZ() - currentPos.getZ())
        );
    }

    // all the following values get updated extremely often, so we won't 'commit' the new values all the time
    private static void commitOftenUpdatedValues(final long uptimeTicks, final List<ServerPlayerEntity> players) {
        if (!doEveryFewSeconds(1, uptimeTicks)) {
            return; // no need to run this 20x per second
        }

        if (doEveryFewSeconds(60, uptimeTicks)) {
            DatabaseWrapper.postGlobalStatToDatabase("uptimeTicks", uptimeTicks);
        }

        for (final ServerPlayerEntity player : players) {
            final var userStats = DatabaseWrapper.getInMemoryData().getUser(player);
            if (doEveryFewSeconds(31, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "blocksPlaced", userStats.getBlocksPlaced());
            }
            if (doEveryFewSeconds(34, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "blocksMined", userStats.getBlocksMined());
            }
            if (doEveryFewSeconds(37, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "oresMined", userStats.getOresMined());
            }
            if (doEveryFewSeconds(41, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "overworldTicks", userStats.getOverworldTicks());
            }
            if (doEveryFewSeconds(43, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "netherTicks", userStats.getNetherTicks());
            }
            if (doEveryFewSeconds(47, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "endTicks", userStats.getEndTicks());
            }
            if (doEveryFewSeconds(49, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "aetherTicks", userStats.getAetherTicks());
            }
            if (doEveryFewSeconds(51, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "aliveTicks", userStats.getAliveTicks());
            }
            if (doEveryFewSeconds(53, uptimeTicks)) {
                DatabaseWrapper.postUserStatToDatabase(player, "distanceTraveled", userStats.getDistanceTraveled());
            }
        }
    }

    public static ServerPlayConnectionEvents.Join onPlayerJoined() {
        return (handler, sender, server) -> {
            var playerCount = server.getCurrentPlayerCount() + 1;
            DatabaseWrapper.getInMemoryData().getGlobal().setPlayersOnline((long) playerCount);
            DatabaseWrapper.postGlobalStatToDatabase("playersOnline", playerCount);
        };
    }

    public static ServerPlayConnectionEvents.Disconnect onPlayerLeft() {
        return (handler, server) -> {
            var playerCount = server.getCurrentPlayerCount() - 1;
            DatabaseWrapper.getInMemoryData().getGlobal().setPlayersOnline((long) playerCount);
            DatabaseWrapper.postGlobalStatToDatabase("playersOnline", playerCount);
        };
    }

    public static ServerMessageEvents.ChatMessage onChatMessage() {
        return (message, sender, params) -> {
            var messageEntity = new ServerChatMessageEntity();
            messageEntity.setTimestampMs(new Date().getTime());
            messageEntity.setUsername(sender.getEntityName());
            messageEntity.setContent(message.getSignedContent());

            processChatMessage(messageEntity);
        };
    }

    public static ServerMessageEvents.GameMessage onSystemMessage() {
        return (server, message, overlay) -> {
            var messageEntity = new ServerChatMessageEntity();
            messageEntity.setTimestampMs(new Date().getTime());
            messageEntity.setUsername("SYSTEM");
            messageEntity.setContent(message.getString());

            processChatMessage(messageEntity);
        };
    }

    private static void processChatMessage(final ServerChatMessageEntity messageEntity) {
        var globalStats = DatabaseWrapper.getInMemoryData().getGlobal();
        globalStats.getChatMessages().add(0, messageEntity);
        globalStats.setChatMessages(new ArrayList<>(globalStats.getChatMessages().subList(0, Math.min(globalStats.getChatMessages().size(), 20)))); // size limit of 20
        DatabaseWrapper.postGlobalStatToDatabase("chatMessages", globalStats.getChatMessages());
    }

    public static PlayerBlockBreakEvents.After onUserBrokeBlock() {
        return (world, player, pos, state, blockEntity) -> {
            var userStats = DatabaseWrapper.getInMemoryData().getUser(player);

            var blocksMined = userStats.getBlocksMined();
            userStats.setBlocksMined(++blocksMined);

            if (isOreBlock(state.getBlock())) {
                var oresMined = userStats.getOresMined();
                userStats.setOresMined(++oresMined);
            }
        };
    }

    private static boolean isOreBlock(final Block block) {
        final String blockName = Registries.BLOCK.getId(block).getPath();

        return switch (blockName) {
            case "coal_ore", "deepslate_coal_ore", "diamond_ore", "deepslate_diamond_ore",
                    "emerald_ore", "deepslate_emerald_ore", "nether_quartz_ore", "iron_ore",
                    "deepslate_iron_ore", "gold_ore", "deepslate_gold_ore", "nether_gold_ore",
                    "copper_ore", "deepslate_copper_ore", "redstone_ore", "deepslate_redstone_ore",
                    "lapis_ore", "deepslate_lapis_ore", "ancient_debris", "ambrosium_ore",
                    "zanite_ore", "gravitite_ore" -> true;
            default -> false;
        };
    }

    public static ServerEntityCombatEvents.AfterKilledOtherEntity onKill() {
        return (world, entity, killedEntity) -> {
            if (entity instanceof ServerPlayerEntity player) {
                var userStats = DatabaseWrapper.getInMemoryData().getUser(player);

                var kills = userStats.getKills();
                userStats.setKills(++kills);
                DatabaseWrapper.postUserStatToDatabase(player, "kills", kills);
            }
        };
    }

}
