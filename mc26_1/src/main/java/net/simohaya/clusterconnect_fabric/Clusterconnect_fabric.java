package net.simohaya.clusterconnect_fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.simohaya.clusterconnect_fabric.mixin.ServerLoginNetworkHandlerAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Clusterconnect_fabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("clusterconnect_fabric");

    // 26.1.x: Identifier moved to net.minecraft.resources
    private static final Identifier VELOCITY_CHANNEL = Identifier.fromNamespaceAndPath("velocity", "player_info");

    @Override
    public void onInitialize() {
        ClusterConnectConfig.load();

        ServerLoginNetworking.registerGlobalReceiver(VELOCITY_CHANNEL,
            (server, loginHandler, responded, buf, synchronizer, responseSender) -> {

                if (!responded) {
                    loginHandler.disconnect(
                        Component.literal(
                            "This server is behind a Velocity proxy. Direct connections are not allowed."
                        )
                    );
                    return;
                }

                String secretKey = ClusterConnectConfig.getInstance().secret_key;
                if (secretKey == null || secretKey.isEmpty()) {
                    loginHandler.disconnect(
                        Component.literal(
                            "[ClusterConnect] Server misconfiguration: secret_key is not set in config/clusterconnect.json."
                        )
                    );
                    return;
                }

                try {
                    PlayerData data = VelocityForwardingHandler.verifyAndExtract(buf, secretKey);

                    // 26.1.x: authlib record-style, PropertyMap wraps ImmutableMultimap
                    GameProfile profile = new GameProfile(data.uuid, data.username, new PropertyMap(data.properties));

                    ((ServerLoginNetworkHandlerAccessor) loginHandler).callStartClientVerification(profile);

                    LOGGER.info("[ClusterConnect] Accepted forwarded profile: {} ({})",
                        profile.name(), profile.id());

                } catch (IOException e) {
                    LOGGER.warn("[ClusterConnect] Forwarding verification failed: {}", e.getMessage());
                    loginHandler.disconnect(
                        Component.literal(
                            "Velocity forwarding verification failed: " + e.getMessage()
                        )
                    );
                }
            }
        );

        LOGGER.info("[ClusterConnect] Velocity Modern Forwarding handler initialized.");
    }
}
