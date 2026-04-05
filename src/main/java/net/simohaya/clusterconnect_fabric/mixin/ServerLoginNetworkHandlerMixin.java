package net.simohaya.clusterconnect_fabric.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    private static final Identifier VELOCITY_CHANNEL = Identifier.of("velocity", "player_info");

    /**
     * Intercept the login-start packet.
     *
     * Instead of proceeding with the normal (offline-mode) login, we ask Velocity
     * for the player's real UUID / username / textures via a plugin-channel query.
     *
     * Fabric API's ServerLoginNetworking infrastructure then:
     *   1. Tracks the outgoing query ID → velocity:player_info mapping.
     *   2. Intercepts the response packet and preserves the raw bytes.
     *   3. Routes the response to the global receiver registered in onInitialize.
     */
    @Inject(method = "onHello", at = @At("HEAD"), cancellable = true)
    private void clusterconnect$onHello(LoginHelloC2SPacket packet, CallbackInfo ci) {
        ServerLoginNetworkHandler self = (ServerLoginNetworkHandler) (Object) this;

        // Send an empty plugin-channel query on velocity:player_info.
        // Fabric API's LoginPacketSender assigns the query ID and registers it
        // internally so the response can be routed to our global receiver.
        ServerLoginNetworking.getSender(self).sendPacket(VELOCITY_CHANNEL, PacketByteBufs.empty());

        // Cancel normal offline-mode login; we resume in the global receiver.
        ci.cancel();
    }
}
