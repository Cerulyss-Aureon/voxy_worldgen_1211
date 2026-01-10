package dev.iseeethan.voxyworldgen.mixin;

import com.mojang.logging.LogUtils;
import dev.iseeethan.voxyworldgen.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;

@Mixin(IOWorker.class)
public abstract class IOWorkerMixin {

    @Shadow
    @Final
    private RegionFileStorage storage;

    @Inject(method = "runStore", at = @At("HEAD"), cancellable = true)
    private void onRunStore(ChunkPos chunkPos, IOWorker.PendingStore pendingStore, CallbackInfo ci) {
        try {
            this.storage.write(chunkPos, pendingStore.data);
            pendingStore.result.complete(null);
        } catch (ConcurrentModificationException e) {
            Constants.LOG.info("Caught ChunkSaveException likely caused by PassivePregen. Chunk will be saved later");
            pendingStore.result.completeExceptionally(e);
        } catch (Exception exception) {
            LogUtils.getLogger().error("Failed to store chunk {}", chunkPos, exception);
            pendingStore.result.completeExceptionally(exception);
        }
        ci.cancel();
    }
}
