package dev.iseeethan.voxyworldgen;

import lombok.Getter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class VoxyWorldGenEvents
        implements ServerPlayConnectionEvents.Disconnect, ServerPlayConnectionEvents.Join, ServerTickEvents.EndTick {

    @Getter
    private static final VoxyWorldGenEvents INSTANCE = new VoxyWorldGenEvents();

    @Override
    public void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        VoxyWorldGenCommon.onPlayerLogoff(handler.getPlayer());
    }

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        VoxyWorldGenCommon.onPlayerLogin(handler.getPlayer());
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        VoxyWorldGenCommon.onServerTickPost();
    }
}
