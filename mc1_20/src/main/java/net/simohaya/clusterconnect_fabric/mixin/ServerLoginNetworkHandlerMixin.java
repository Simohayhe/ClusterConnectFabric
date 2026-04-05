package net.simohaya.clusterconnect_fabric.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    // 1.20.x: new Identifier() (Identifier.of() is 1.21+)
    private static final Identifier VELOCITY_CHANNEL = new Identifier("velocity", "player_info");

    @Inject(method = "onHello", at = @At("HEAD"), cancellable = true)
    private void clusterconnect$onHello(LoginHelloC2SPacket packet, CallbackInfo ci) {
        // 1.20.x: ServerLoginNetworking.getSender() does not exist yet.
        // Instead, access Fabric's ServerLoginNetworkAddon via NetworkHandlerExtensions,
        // which implements PacketSender and properly tracks the outgoing query ID so
        // that the response is routed back to our registerGlobalReceiver handler.
        PacketSender sender = (PacketSender) ((NetworkHandlerExtensions)(Object)this).getAddon();
        sender.sendPacket(VELOCITY_CHANNEL, PacketByteBufs.empty());
        ci.cancel();
    }
}
