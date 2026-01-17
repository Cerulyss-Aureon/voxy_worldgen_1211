package dev.iseeethan.voxyworldgen.platform.services;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;

import java.io.File;
import java.util.concurrent.Executor;

public interface IPlatformHelper {

    MinecraftServer getCurrentServer();

    Executor getChunkGenExecutor(ServerLevel level);

    boolean isChunkExecutorWorking(ServerLevel level);

    File playerDirectoryFromPlayerList(PlayerList list);

    int getPlayerLoadDistance();

    int getSpawnLoadDistance();
    
    // Config accessors for Common module
    boolean isChunkGenerationEnabled();
    
    boolean shouldPrioritizeNearPlayer();
    
    /**
     * Get the generation style as a string identifier.
     * Values: "SPIRAL_OUT", "SPIRAL_IN", "CONCENTRIC", "ORIGINAL", "RANDOM"
     */
    String getGenerationStyle();

    int getChunksPerTick();
}
