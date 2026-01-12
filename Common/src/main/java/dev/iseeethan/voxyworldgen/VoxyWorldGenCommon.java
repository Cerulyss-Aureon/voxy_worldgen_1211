package dev.iseeethan.voxyworldgen;

import dev.iseeethan.voxyworldgen.levelpos.DynamicPlayerLevelPos;
import dev.iseeethan.voxyworldgen.levelpos.ILevelPos;
import dev.iseeethan.voxyworldgen.levelpos.StaticIdentifiableLevelPos;
import dev.iseeethan.voxyworldgen.levelpos.StaticLevelPos;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VoxyWorldGenCommon {

    @Getter
    private static List<ILevelPos> playerPos = new ArrayList<>();

    @Getter
    private static ILevelPos spawnPoint = null;

    public static void onPlayerLogin(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        @Nullable
        ILevelPos offlinePlayerPos = null;
        for (ILevelPos levelPos : playerPos) {
            if (levelPos instanceof StaticIdentifiableLevelPos identifiableLevelPos) {
                if (identifiableLevelPos.getUuid().equals(player.getUUID())) {
                    offlinePlayerPos = identifiableLevelPos;
                    break;
                }
            }
        }
        if (offlinePlayerPos != null) {
            playerPos.remove(offlinePlayerPos);
        }
        playerPos.add(new DynamicPlayerLevelPos(serverPlayer));
    }

    public static void onPlayerLogoff(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        @Nullable
        ILevelPos unloadablePlayerPos = null;
        for (ILevelPos levelPos : playerPos) {
            if (levelPos instanceof DynamicPlayerLevelPos dynamicPlayerLevelPos
                    && dynamicPlayerLevelPos.getPlayer() == serverPlayer) {

                unloadablePlayerPos = dynamicPlayerLevelPos;
                break;
            }
        }
        if (unloadablePlayerPos != null) {
            playerPos.remove(unloadablePlayerPos);
        }
        playerPos.add(new StaticIdentifiableLevelPos(serverPlayer));
    }

    public static void onServerStart(MinecraftServer server) {
        BlockPos spawnPos = server.overworld().getLevelData().getRespawnData().pos();
        spawnPoint = new StaticLevelPos(Level.OVERWORLD, spawnPos.getX(), spawnPos.getZ());
    }

    public static void onServerStop() {
        spawnPoint = null;
    }

    private static VoxyWorldGenWorker worker = null;

    public static void onServerTickPost() {
        if (worker == null)
            worker = new VoxyWorldGenWorker();
        worker.doWork();
    }
}
