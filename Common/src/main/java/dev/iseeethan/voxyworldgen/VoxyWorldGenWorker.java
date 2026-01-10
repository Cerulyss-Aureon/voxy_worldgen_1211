package dev.iseeethan.voxyworldgen;

import dev.iseeethan.voxyworldgen.levelpos.ILevelPos;
import dev.iseeethan.voxyworldgen.levelpos.StaticLevelPos;
import dev.iseeethan.voxyworldgen.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VoxyWorldGenWorker {

    private Map<ServerLevel, List<ChunkPos>> generated = new HashMap<>();
    public AtomicInteger doneGenerating = new AtomicInteger();

    // Hook for external chunk processing (e.g., Voxy LOD ingestion)
    private static Consumer<ChunkAccess> chunkGeneratedCallback = null;

    public static void setChunkGeneratedCallback(Consumer<ChunkAccess> callback) {
        chunkGeneratedCallback = callback;
    }

    public void doWork() {
        var levelPositions = new ArrayList<>(VoxyWorldGenCommon.getPlayerPos());
        levelPositions.add(VoxyWorldGenCommon.getSpawnPoint());
        Collections.shuffle(levelPositions);
        for (ILevelPos levelPos : levelPositions) {
            checkPos(levelPos);
        }
    }

    private void checkPos(ILevelPos levelPos) {
        if (levelPos.isCompleted())
            return;
        ServerLevel level = levelPos.getServerLevel();
        if (Services.PLATFORM.isChunkExecutorWorking(level))
            return;
        for (int dx = 0; dx < levelPos.loadDistance(); dx++) {
            for (int dy = 0; dy < levelPos.loadDistance(); dy++) {
                for (boolean invertX : new boolean[] { true, false }) {
                    for (boolean invertY : new boolean[] { true, false }) {
                        if ((dx == 0 && !invertX) || (dy == 0 && !invertY))
                            continue;
                        ChunkPos pos = move(levelPos.getPos(), invertX ? -dx : dx, invertY ? -dy : dy);
                        if (!generated.computeIfAbsent(level, l -> new ArrayList<>()).contains(pos)) {
                            if (!level.hasChunk(pos.x, pos.z)) {
                                ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, true);
                                if (!chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
                                    generated.get(level).add(pos);
                                    CompletableFuture.supplyAsync(() -> {
                                        ChunkAccess generatedChunk = level.getChunkSource().getChunk(pos.x, pos.z,
                                                ChunkStatus.FULL, true);
                                        // Call the chunk generated callback if set
                                        if (chunkGeneratedCallback != null) {
                                            chunkGeneratedCallback.accept(generatedChunk);
                                        }
                                        doneGenerating.getAndIncrement();
                                        return null;
                                    }, Services.PLATFORM.getChunkGenExecutor(level));
                                    return;
                                }
                            }
                            generated.get(level).add(pos);
                        }
                    }
                }
            }
        }
        if (levelPos instanceof StaticLevelPos staticLevelPos) {
            staticLevelPos.setCompleted(true);
        }
    }

    private static ChunkPos move(ChunkPos pos, int x, int z) {
        return new ChunkPos(pos.x + x, pos.z + z);
    }
}
