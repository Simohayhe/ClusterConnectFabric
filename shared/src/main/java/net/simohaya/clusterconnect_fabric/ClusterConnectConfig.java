package net.simohaya.clusterconnect_fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ClusterConnectConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClusterConnectConfig instance;

    public String secret_key = "";

    public static ClusterConnectConfig getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("clusterconnect.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                instance = GSON.fromJson(reader, ClusterConnectConfig.class);
                if (instance == null) {
                    instance = new ClusterConnectConfig();
                }
            } catch (IOException e) {
                Clusterconnect_fabric.LOGGER.error("[ClusterConnect] Failed to load config", e);
                instance = new ClusterConnectConfig();
            }
        } else {
            instance = new ClusterConnectConfig();
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(instance, writer);
                Clusterconnect_fabric.LOGGER.info("[ClusterConnect] Generated default config at {}", configPath);
            } catch (IOException e) {
                Clusterconnect_fabric.LOGGER.error("[ClusterConnect] Failed to write default config", e);
            }
        }

        if (instance.secret_key == null || instance.secret_key.isEmpty()) {
            Clusterconnect_fabric.LOGGER.warn(
                "[ClusterConnect] secret_key is not set in config/clusterconnect.json! " +
                "All connections will be rejected until this is configured."
            );
        }
    }
}
