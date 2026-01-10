package dev.iseeethan.voxyworldgen.levelpos;

import com.mojang.serialization.Dynamic;
import dev.iseeethan.voxyworldgen.Constants;
import dev.iseeethan.voxyworldgen.platform.Services;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import static dev.iseeethan.voxyworldgen.levelpos.ILevelPos.*;

import java.util.UUID;

@Getter
public class StaticIdentifiableLevelPos extends StaticLevelPos {
    private final UUID uuid;

    public StaticIdentifiableLevelPos(ServerPlayer player) {
        this(player.getUUID(),
                player.level().dimension(),
                new ChunkPos(
                        chunkPosCoord(player.getX()),
                        chunkPosCoord(player.getZ())));
    }

    public StaticIdentifiableLevelPos(UUID uuid, CompoundTag tag) {
        this(uuid,
                Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("Dimension"))
                        .resultOrPartial(Constants.LOG::error)
                        .orElse(Level.OVERWORLD),
                tag.getList("Pos")
                        .map(list -> new ChunkPos(chunkPosCoord(list.getDouble(0).orElse(0.0)),
                                chunkPosCoord(list.getDouble(2).orElse(0.0))))
                        .orElse(new ChunkPos(0, 0)));
    }

    public StaticIdentifiableLevelPos(UUID uuid, ResourceKey<Level> level, ChunkPos pos) {
        super(level, pos);
        this.uuid = uuid;
    }

    public int loadDistance() {
        return Services.PLATFORM.getPlayerLoadDistance();
    }
}
