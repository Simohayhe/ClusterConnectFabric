package net.simohaya.clusterconnect_fabric;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.authlib.properties.Property;

import java.util.UUID;

/**
 * Version-neutral holder for data parsed from the Velocity forwarding payload.
 * Each MC-version subproject assembles the final GameProfile from this.
 */
public class PlayerData {
    public final UUID uuid;
    public final String username;
    public final ArrayListMultimap<String, Property> properties;

    public PlayerData(UUID uuid, String username, ArrayListMultimap<String, Property> properties) {
        this.uuid = uuid;
        this.username = username;
        this.properties = properties;
    }
}
