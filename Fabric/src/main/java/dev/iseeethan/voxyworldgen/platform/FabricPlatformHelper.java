package dev.iseeethan.voxyworldgen.platform;

import dev.iseeethan.voxyworldgen.VoxyWorldGenCommon;
import dev.iseeethan.voxyworldgen.config.VoxyWorldGenConfig;
import dev.iseeethan.voxyworldgen.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;

import java.io.File;
import java.util.concurrent.Executor;

public class FabricPlatformHelper
        implements IPlatformHelper, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopped {

    MinecraftServer server;

    @Override
    public MinecraftServer getCurrentServer() {
        return server;
    }

    @Override
    public Executor getChunkGenExecutor(ServerLevel level) {
        return level.getChunkSource().mainThreadProcessor;
    }

    @Override
    public boolean isChunkExecutorWorking(ServerLevel level) {
        return level.getChunkSource().mainThreadProcessor.getPendingTasksCount() > 0;
    }

    @Override
    public File playerDirectoryFromPlayerList(PlayerList list) {
        return list.playerIo.playerDir;
    }

    @Override
    public int getPlayerLoadDistance() {
        return VoxyWorldGenConfig.getPlayerDistance();
    }

    @Override
    public int getSpawnLoadDistance() {
        return VoxyWorldGenConfig.getSpawnDistance();
    }
    
    @Override
    public boolean isChunkGenerationEnabled() {
        return VoxyWorldGenConfig.isEnabled();
    }
    
    @Override
    public boolean shouldPrioritizeNearPlayer() {
        return VoxyWorldGenConfig.shouldPrioritizeNearPlayer();
    }
    
    @Override
    public String getGenerationStyle() {
        return VoxyWorldGenConfig.getGenerationStyle().name();
    }

    @Override
    public int getChunksPerTick() {
        return VoxyWorldGenConfig.getChunksPerTick();
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        this.server = server;
        VoxyWorldGenCommon.onServerStart(server);
    }

    @Override
    public void onServerStopped(MinecraftServer server) {
        this.server = null;
        VoxyWorldGenCommon.onServerStop();
    }

}
