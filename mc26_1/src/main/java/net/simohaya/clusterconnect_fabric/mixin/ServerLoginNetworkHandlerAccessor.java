package net.simohaya.clusterconnect_fabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerLoginPacketListenerImpl.class)
public interface ServerLoginNetworkHandlerAccessor {

    // 26.1.x: startVerify was renamed to startClientVerification
    @Invoker("startClientVerification")
    void callStartClientVerification(GameProfile profile);
}
