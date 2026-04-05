package net.simohaya.clusterconnect_fabric.mixin;

import net.fabricmc.fabric.api.networking.v1.LoginPacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginNetworkHandlerMixin {

    // 26.1.x: Identifier moved to net.minecraft.resources
    private static final Identifier VELOCITY_CHANNEL = Identifier.fromNamespaceAndPath("velocity", "player_info");

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void clusterconnect$handleHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        ServerLoginPacketListenerImpl self = (ServerLoginPacketListenerImpl)(Object)this;
        LoginPacketSender sender = ServerLoginNetworking.getSender(self);
        sender.sendPacket(VELOCITY_CHANNEL, new FriendlyByteBuf(io.netty.buffer.Unpooled.EMPTY_BUFFER));
        ci.cancel();
    }
}
