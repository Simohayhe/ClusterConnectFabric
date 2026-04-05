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

    @Inject(method = "onHello", at = @At("HEAD"), cancellable = true)
    private void clusterconnect$onHello(LoginHelloC2SPacket packet, CallbackInfo ci) {
        ServerLoginNetworkHandler self = (ServerLoginNetworkHandler) (Object) this;
        ServerLoginNetworking.getSender(self).sendPacket(VELOCITY_CHANNEL, PacketByteBufs.empty());
        ci.cancel();
    }
}
