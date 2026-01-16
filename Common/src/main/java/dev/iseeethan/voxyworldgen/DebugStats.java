package dev.iseeethan.voxyworldgen;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks debug statistics for the F3 debug overlay.
 * This class provides thread-safe access to chunk generation stats.
 */
public class DebugStats {
    
    private static final DebugStats INSTANCE = new DebugStats();
    
    // Total chunks ever completed (persists across sessions until world change)
    private final AtomicInteger totalChunksCompleted = new AtomicInteger(0);
    
    // Current queue size for the active area
    private final AtomicInteger chunksRemainingInCurrentArea = new AtomicInteger(0);
    
    // Total chunks in the current area (queue size when started)
    private final AtomicInteger totalChunksInCurrentArea = new AtomicInteger(0);
    
    // Is generation enabled
    private volatile boolean generationEnabled = false;
    
    // Current generation style
    private volatile String currentStyle = "";
    
    private DebugStats() {}
    
    public static DebugStats getInstance() {
        return INSTANCE;
    }
    
    /**
     * Increment the total chunks completed counter.
     * Called when a chunk finishes generating.
     */
    public void incrementCompleted() {
        totalChunksCompleted.incrementAndGet();
    }
    
    /**
     * Get the total number of chunks ever completed in this session.
     */
    public int getTotalChunksCompleted() {
        return totalChunksCompleted.get();
    }
    
    /**
     * Update the current area stats.
     * @param remaining Chunks remaining to generate
     * @param total Total chunks in the current area
     */
    public void updateCurrentAreaStats(int remaining, int total) {
        chunksRemainingInCurrentArea.set(remaining);
        totalChunksInCurrentArea.set(total);
    }
    
    /**
     * Get the number of chunks remaining in the current area.
     */
    public int getChunksRemainingInCurrentArea() {
        return chunksRemainingInCurrentArea.get();
    }
    
    /**
     * Get the total number of chunks in the current area.
     */
    public int getTotalChunksInCurrentArea() {
        return totalChunksInCurrentArea.get();
    }
    
    /**
     * Set whether generation is currently enabled.
     */
    public void setGenerationEnabled(boolean enabled) {
        this.generationEnabled = enabled;
    }
    
    /**
     * Check if generation is enabled.
     */
    public boolean isGenerationEnabled() {
        return generationEnabled;
    }
    
    /**
     * Set the current generation style.
     */
    public void setCurrentStyle(String style) {
        this.currentStyle = style;
    }
    
    /**
     * Get the current generation style.
     */
    public String getCurrentStyle() {
        return currentStyle;
    }
    
    /**
     * Reset all stats - called on world change.
     */
    public void reset() {
        totalChunksCompleted.set(0);
        chunksRemainingInCurrentArea.set(0);
        totalChunksInCurrentArea.set(0);
        generationEnabled = false;
        currentStyle = "";
    }
}
