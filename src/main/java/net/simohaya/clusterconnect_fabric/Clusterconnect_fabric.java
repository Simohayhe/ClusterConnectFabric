package net.simohaya.clusterconnect_fabric;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.util.Identifier;
import net.simohaya.clusterconnect_fabric.mixin.ServerLoginNetworkHandlerAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Clusterconnect_fabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("clusterconnect_fabric");

    private static final Identifier VELOCITY_CHANNEL = Identifier.of("velocity", "player_info");

    @Override
    public void onInitialize() {
        ClusterConnectConfig.load();

        // Register the Velocity Modern Forwarding receiver.
        //
        // When a player connects through Velocity, the mixin in
        // ServerLoginNetworkHandlerMixin sends a plugin-channel query on
        // velocity:player_info and cancels the normal offline-mode login flow.
        //
        // Velocity responds with a signed payload containing the player's real
        // UUID, username, and skin properties (textures). Fabric API intercepts
        // the raw bytes *before* vanilla would discard them, then calls this
        // handler.
        //
        // We verify the HMAC-SHA256 signature and call startVerify() with the
        // fully-populated GameProfile (including textures), which lets the normal
        // tick loop proceed to WAITING_FOR_DUPE_DISCONNECT → sendSuccessPacket.
        ServerLoginNetworking.registerGlobalReceiver(VELOCITY_CHANNEL,
            (server, loginHandler, responded, buf, synchronizer, responseSender) -> {

                if (!responded) {
                    // Vanilla client / direct connection — no forwarding data.
                    loginHandler.disconnect(
                        net.minecraft.text.Text.literal(
                            "This server is behind a Velocity proxy. Direct connections are not allowed."
                        )
                    );
                    return;
                }

                String secretKey = ClusterConnectConfig.getInstance().secret_key;
                if (secretKey == null || secretKey.isEmpty()) {
                    loginHandler.disconnect(
                        net.minecraft.text.Text.literal(
                            "[ClusterConnect] Server misconfiguration: secret_key is not set in config/clusterconnect.json."
                        )
                    );
                    return;
                }

                try {
                    GameProfile profile = VelocityForwardingHandler.verifyAndExtract(buf, secretKey);

                    // Advance the login state machine with the real profile.
                    // startVerify() sets this.profile and state = VERIFYING, after
                    // which tick() calls tickVerify() → sendSuccessPacket().
                    ((ServerLoginNetworkHandlerAccessor) loginHandler).callStartVerify(profile);

                    LOGGER.info("[ClusterConnect] Accepted forwarded profile: {} ({})",
                        profile.name(), profile.id());

                } catch (IOException e) {
                    LOGGER.warn("[ClusterConnect] Forwarding verification failed: {}", e.getMessage());
                    loginHandler.disconnect(
                        net.minecraft.text.Text.literal(
                            "Velocity forwarding verification failed: " + e.getMessage()
                        )
                    );
                }
            }
        );

        LOGGER.info("[ClusterConnect] Velocity Modern Forwarding handler initialized.");
    }
}
