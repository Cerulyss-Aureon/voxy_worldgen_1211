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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class VoxyWorldGenWorker {

    private static final int MAX_CHECKS_PER_TICK = 4096;
    private static final long CHECK_BUDGET_NANOS = 5_000_000L; // 5ms
    private static final int POSITION_CHANGE_THRESHOLD = 8;

    private Map<ServerLevel, Set<ChunkPos>> generated = new HashMap<>();
    private Map<ILevelPos, List<ChunkPos>> positionQueues = new ConcurrentHashMap<>();
    private Map<ILevelPos, Integer> queueIndices = new ConcurrentHashMap<>();
    private Map<ILevelPos, ChunkPos> lastKnownPositions = new ConcurrentHashMap<>();
    private Set<ILevelPos> pendingQueueGeneration = ConcurrentHashMap.newKeySet();
    private String lastGenerationStyle = null;
    public AtomicInteger doneGenerating = new AtomicInteger();

    private static Consumer<ChunkAccess> chunkGeneratedCallback = null;

    public static void setChunkGeneratedCallback(Consumer<ChunkAccess> callback) {
        chunkGeneratedCallback = callback;
    }

    public void doWork() {
        boolean enabled = Services.PLATFORM.isChunkGenerationEnabled();
        DebugStats.getInstance().setGenerationEnabled(enabled);
        
        if (!enabled) {
            return;
        }
        
        String currentStyle = Services.PLATFORM.getGenerationStyle();
        DebugStats.getInstance().setCurrentStyle(currentStyle);
        if (!currentStyle.equals(lastGenerationStyle)) {
            clearCache();
            lastGenerationStyle = currentStyle;
        }
        
        var levelPositions = new ArrayList<>(VoxyWorldGenCommon.getPlayerPos());
        levelPositions.add(VoxyWorldGenCommon.getSpawnPoint());
        
        if (Services.PLATFORM.shouldPrioritizeNearPlayer()) {
            levelPositions.sort((a, b) -> {
                boolean aPlayer = !(a instanceof StaticLevelPos);
                boolean bPlayer = !(b instanceof StaticLevelPos);
                if (aPlayer && !bPlayer) return -1;
                if (!aPlayer && bPlayer) return 1;
                return 0;
            });
        } else {
            Collections.shuffle(levelPositions);
        }

        int maxGenerations = Services.PLATFORM.getChunksPerTick();
        int totalScheduled = 0;

        for (ILevelPos levelPos : levelPositions) {
            if (totalScheduled >= maxGenerations) break;
            totalScheduled += checkPos(levelPos, maxGenerations - totalScheduled);
        }
    }

    private boolean hasPositionChanged(ILevelPos levelPos) {
        ChunkPos currentPos = levelPos.getPos();
        ChunkPos lastPos = lastKnownPositions.get(levelPos);
        
        if (lastPos == null) {
            return true;
        }
        
        int dx = Math.abs(currentPos.x - lastPos.x);
        int dz = Math.abs(currentPos.z - lastPos.z);
        
        return dx > POSITION_CHANGE_THRESHOLD || dz > POSITION_CHANGE_THRESHOLD;
    }

    private List<ChunkPos> getPositionQueue(ILevelPos levelPos) {
        if (hasPositionChanged(levelPos) && !pendingQueueGeneration.contains(levelPos)) {
            positionQueues.remove(levelPos);
            queueIndices.remove(levelPos);
        }
        
        if (positionQueues.containsKey(levelPos)) {
            return positionQueues.get(levelPos);
        }
        
        if (pendingQueueGeneration.contains(levelPos)) {
            return null;
        }
        
        pendingQueueGeneration.add(levelPos);
        ChunkPos center = levelPos.getPos();
        int radius = levelPos.loadDistance();
        String style = Services.PLATFORM.getGenerationStyle();
        
        CompletableFuture.runAsync(() -> {
            List<ChunkPos> queue = switch (style) {
                case "SPIRAL_OUT" -> ChunkPatternGenerator.generateSpiralOut(center, radius);
                case "SPIRAL_IN" -> ChunkPatternGenerator.generateSpiralIn(center, radius);
                case "CONCENTRIC" -> ChunkPatternGenerator.generateConcentric(center, radius);
                case "ORIGINAL" -> ChunkPatternGenerator.generateOriginal(center, radius);
                case "RANDOM" -> ChunkPatternGenerator.generateRandom(center, radius);
                default -> ChunkPatternGenerator.generateSpiralOut(center, radius);
            };
            
            positionQueues.put(levelPos, queue);
            queueIndices.put(levelPos, 0);
            lastKnownPositions.put(levelPos, center);
            pendingQueueGeneration.remove(levelPos);
        });
        
        return null;
    }

    private int checkPos(ILevelPos levelPos, int generationBudget) {
        if (levelPos == null || levelPos.isCompleted())
            return 0;
            
        ServerLevel level = levelPos.getServerLevel();
        if (level == null || Services.PLATFORM.isChunkExecutorWorking(level))
            return 0;
        
        List<ChunkPos> queue = getPositionQueue(levelPos);
        
        if (queue == null) {
            DebugStats.getInstance().updateCurrentAreaStats(0, 0);
            return 0;
        }
        
        int currentIndex = queueIndices.getOrDefault(levelPos, 0);
        Set<ChunkPos> levelGenerated = generated.computeIfAbsent(level, l -> new HashSet<>());
        
        int remaining = Math.max(0, queue.size() - currentIndex);
        DebugStats.getInstance().updateCurrentAreaStats(remaining, queue.size());
        
        int checkedThisTick = 0;
        int scheduledInThisCall = 0;
        long startTime = System.nanoTime();
        
        while (currentIndex < queue.size() && scheduledInThisCall < generationBudget && checkedThisTick < MAX_CHECKS_PER_TICK) {
            if ((checkedThisTick & 15) == 0 && checkedThisTick > 0 && (System.nanoTime() - startTime) > CHECK_BUDGET_NANOS) {
                break;
            }
            
            ChunkPos pos = queue.get(currentIndex);
            currentIndex++;
            queueIndices.put(levelPos, currentIndex);
            
            if (levelGenerated.contains(pos)) {
                continue;
            }
            
            if (!level.hasChunk(pos.x, pos.z)) {
                checkedThisTick++;
                ChunkAccess chunk = level.getChunk(pos.x, pos.z, ChunkStatus.EMPTY, true);
                if (!chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
                    levelGenerated.add(pos);
                    scheduledInThisCall++;
                    
                    CompletableFuture.supplyAsync(() -> {
                        ChunkAccess generatedChunk = level.getChunkSource().getChunk(pos.x, pos.z,
                                ChunkStatus.FULL, true);
                        if (chunkGeneratedCallback != null) {
                            chunkGeneratedCallback.accept(generatedChunk);
                        }
                        doneGenerating.getAndIncrement();
                        DebugStats.getInstance().incrementCompleted();
                        return null;
                    }, Services.PLATFORM.getChunkGenExecutor(level));
                    
                    continue;
                }
            }
            levelGenerated.add(pos);
        }
        
        if (currentIndex >= queue.size()) {
            if (levelPos instanceof StaticLevelPos staticLevelPos) {
                staticLevelPos.setCompleted(true);
            }
            positionQueues.remove(levelPos);
            queueIndices.remove(levelPos);
        }

        return scheduledInThisCall;
    }

    public void clearCache() {
        positionQueues.clear();
        queueIndices.clear();
        lastKnownPositions.clear();
        pendingQueueGeneration.clear();
    }
}
