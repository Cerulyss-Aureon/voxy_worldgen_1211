package dev.iseeethan.voxyworldgen;

import dev.iseeethan.voxyworldgen.config.VoxyWorldGenConfig;
import dev.iseeethan.voxyworldgen.platform.FabricPlatformHelper;
import dev.iseeethan.voxyworldgen.platform.Services;
import me.cortex.voxy.common.world.service.VoxelIngestService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.level.chunk.LevelChunk;

public class VoxyWorldGenFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        VoxyWorldGenConfig.create();
        ServerLifecycleEvents.SERVER_STARTED.register((FabricPlatformHelper) Services.PLATFORM);
        ServerLifecycleEvents.SERVER_STOPPED.register((FabricPlatformHelper) Services.PLATFORM);
        ServerPlayConnectionEvents.JOIN.register(VoxyWorldGenEvents.getINSTANCE());
        ServerPlayConnectionEvents.DISCONNECT.register(VoxyWorldGenEvents.getINSTANCE());
        ServerTickEvents.END_SERVER_TICK.register(VoxyWorldGenEvents.getINSTANCE());

        // Register Voxy LOD integration callback
        VoxyWorldGenWorker.setChunkGeneratedCallback(chunk -> {
            if (chunk instanceof LevelChunk levelChunk) {
                VoxelIngestService.tryAutoIngestChunk(levelChunk);
            }
        });
    }
}
