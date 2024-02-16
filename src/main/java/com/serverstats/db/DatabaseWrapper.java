package com.serverstats.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.serverstats.db.entities.ServerStatsEntity;
import com.serverstats.db.utils.PlayerPosition;

public final class DatabaseWrapper {

    public static final Logger LOGGER = LoggerFactory.getLogger("server-stats-database");

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final Map<String, PlayerPosition> lastKnownPositions = new HashMap<>();

    private static boolean connected = false;

    private static DatabaseReference ref;

    private static ServerStatsEntity inMemoryData;

    private DatabaseWrapper() {}

    public static synchronized void connect() {
        final Path credentialsPath = FabricLoader.getInstance().getConfigDir().resolve("firebase-credentials.json");

        if (!Files.exists(credentialsPath)) {
            LOGGER.error("Expected the file '{}' to exist, but it does not. Cannot collect server stats.", credentialsPath);
            return;
        }

        final Path propertiesPath = FabricLoader.getInstance().getConfigDir().resolve("server-stats.properties");

        if (!Files.exists(propertiesPath)) {
            LOGGER.error("Expected the file '{}' to exist, but it does not. Cannot collect server stats.", propertiesPath);
            return;
        }

        final Properties properties = new Properties();

        try {
            try (final InputStream is = Files.newInputStream(propertiesPath)) {
                properties.load(is);
            }

            final InputStream refreshToken = new FileInputStream(credentialsPath.toString());
            final FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .setDatabaseUrl(properties.getProperty("firebaseUrl"))
                    .build();

            FirebaseApp.initializeApp(options);
            ref = FirebaseDatabase.getInstance().getReference("minecraft");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    inMemoryData = snapshot.getValue(ServerStatsEntity.class);
                    if (inMemoryData == null) inMemoryData = new ServerStatsEntity();
                    connected = true;
                }

                @Override
                public void onCancelled(final DatabaseError error) {
                    LOGGER.error("Failed to read from database. Cannot collect server stats.");
                }

            });
        } catch (final Exception e) {
            LOGGER.error("Initialization of server stats mod failed. Cannot collect server stats.", e);
        }
    }

    public static synchronized PlayerPosition getLastKnownPosition(final PlayerEntity user) {
        return lastKnownPositions.get(user.getEntityName());
    }

    public static synchronized void setLastKnownPosition(final PlayerEntity user, final PlayerPosition pos) {
        lastKnownPositions.put(user.getEntityName(), pos);
    }

    public static synchronized ServerStatsEntity getInMemoryData() {
        return inMemoryData;
    }

    /**
     * Posts some data to minecraft/global/[path].
     */
    public static synchronized void postGlobalStatToDatabase(final String path, final Object value) {
        if (!connected) {
            LOGGER.debug("Not connected to database. Cannot read or write data.");
            return;
        }

        try {
            executor.submit(() -> ref.child("global").child(path).setValueAsync(value));
        } catch (final Exception e) {
            LOGGER.error("Failed to post data!", e);
        }
    }

    /**
     * Posts some data to minecraft/users/[user]/[path].
     */
    public static synchronized void postUserStatToDatabase(final PlayerEntity user, final String path, final Object value) {
        if (!connected) {
            LOGGER.debug("Not connected to database. Cannot read or write data.");
            return;
        }

        try {
            executor.submit(() -> ref.child("users").child(user.getEntityName()).child(path).setValueAsync(value));
        } catch (final Exception e) {
            LOGGER.error("Failed to post data!", e);
        }
    }

}
