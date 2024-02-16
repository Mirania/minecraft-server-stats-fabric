package com.serverstats;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serverstats.db.DatabaseWrapper;
import com.serverstats.db.EventListeners;

public class Serverstats implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("server-stats");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Connecting to firebase...");

		DatabaseWrapper.connect();

		LOGGER.info("Registering listeners for events...");

		ServerTickEvents.END_SERVER_TICK.register(EventListeners.onServerUptimeIncreased());
		ServerPlayConnectionEvents.JOIN.register(EventListeners.onPlayerJoined());
		ServerPlayConnectionEvents.DISCONNECT.register(EventListeners.onPlayerLeft());
		ServerMessageEvents.CHAT_MESSAGE.register(EventListeners.onChatMessage());
		ServerMessageEvents.GAME_MESSAGE.register(EventListeners.onSystemMessage());
		PlayerBlockBreakEvents.AFTER.register(EventListeners.onUserBrokeBlock());
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(EventListeners.onKill());
	}
}