package dev.iseeethan.voxyworldgen;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkPatternGenerator {
    
    public static List<ChunkPos> generateSpiralOut(ChunkPos center, int radius) {
        int expectedSize = (2 * radius + 1) * (2 * radius + 1);
        List<ChunkPos> positions = new ArrayList<>(expectedSize);
        Set<Long> seen = new HashSet<>(expectedSize);
        
        positions.add(center);
        seen.add(center.toLong());
        
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxSteps = expectedSize;
        
        for (int i = 0; i < maxSteps; i++) {
            if (-radius <= x && x <= radius && -radius <= z && z <= radius) {
                ChunkPos pos = new ChunkPos(center.x + x, center.z + z);
                long posLong = pos.toLong();
                if (!seen.contains(posLong)) {
                    positions.add(pos);
                    seen.add(posLong);
                }
            }
            
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            
            x += dx;
            z += dz;
        }
        
        return positions;
    }
    
    public static List<ChunkPos> generateSpiralIn(ChunkPos center, int radius) {
        List<ChunkPos> positions = generateSpiralOut(center, radius);
        Collections.reverse(positions);
        return positions;
    }
    
    public static List<ChunkPos> generateConcentric(ChunkPos center, int radius) {
        List<ChunkPos> positions = new ArrayList<>();
        
        positions.add(center);
        
        for (int ring = 1; ring <= radius; ring++) {
            for (int x = -ring; x <= ring; x++) {
                positions.add(new ChunkPos(center.x + x, center.z - ring));
            }
            for (int z = -ring + 1; z <= ring; z++) {
                positions.add(new ChunkPos(center.x + ring, center.z + z));
            }
            for (int x = ring - 1; x >= -ring; x--) {
                positions.add(new ChunkPos(center.x + x, center.z + ring));
            }
            for (int z = ring - 1; z >= -ring + 1; z--) {
                positions.add(new ChunkPos(center.x - ring, center.z + z));
            }
        }
        
        return positions;
    }
    
    public static List<ChunkPos> generateOriginal(ChunkPos center, int radius) {
        List<ChunkPos> positions = new ArrayList<>();
        
        for (int dx = 0; dx < radius; dx++) {
            for (int dz = 0; dz < radius; dz++) {
                for (boolean invertX : new boolean[]{true, false}) {
                    for (boolean invertZ : new boolean[]{true, false}) {
                        if ((dx == 0 && !invertX) || (dz == 0 && !invertZ))
                            continue;
                        int x = invertX ? -dx : dx;
                        int z = invertZ ? -dz : dz;
                        positions.add(new ChunkPos(center.x + x, center.z + z));
                    }
                }
            }
        }
        
        return positions;
    }
    
    public static List<ChunkPos> generateRandom(ChunkPos center, int radius) {
        List<ChunkPos> positions = new ArrayList<>();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                positions.add(new ChunkPos(center.x + x, center.z + z));
            }
        }
        
        Collections.shuffle(positions);
        return positions;
    }
}
