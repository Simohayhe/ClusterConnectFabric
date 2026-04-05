package net.simohaya.clusterconnect_fabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerLoginNetworkHandler.class)
public interface ServerLoginNetworkHandlerAccessor {

    /**
     * Exposes the package-private {@code startVerify(GameProfile)} method.
     * Sets this.profile and advances state to VERIFYING so the normal tick
     * loop can proceed with ban/whitelist checks and login success.
     */
    @Invoker("startVerify")
    void callStartVerify(GameProfile profile);
}
