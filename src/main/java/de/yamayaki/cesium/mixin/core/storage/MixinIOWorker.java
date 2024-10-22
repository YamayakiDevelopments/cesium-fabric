package de.yamayaki.cesium.mixin.core.storage;

import de.yamayaki.cesium.api.accessor.DatabaseSetter;
import de.yamayaki.cesium.api.accessor.SpecificationSetter;
import de.yamayaki.cesium.api.database.DatabaseSpec;
import de.yamayaki.cesium.api.database.IDBInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(IOWorker.class)
public abstract class MixinIOWorker implements DatabaseSetter, SpecificationSetter {
    @Shadow
    @Final
    private RegionFileStorage storage;

    @Unique
    private IDBInstance database;

    @Unique
    private DatabaseSpec<ChunkPos, CompoundTag> databaseSpec;

    @Unique
    private boolean isCesium = false;

    /**
     * @author Yamayaki
     * @see IOWorker#loadAsync(ChunkPos)
     */
    @Redirect(
            method = "method_27943",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;read(Lnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private CompoundTag cesium$read(RegionFileStorage instance, ChunkPos chunkPos) throws IOException {
        if (this.isCesium) {
            return this.database
                    .getDatabase(this.databaseSpec)
                    .getValue(chunkPos);
        } else {
            return instance.read(chunkPos);
        }
    }

    /**
     * @author Yamayaki
     * @see IOWorker#scanChunk(ChunkPos, StreamTagVisitor)
     */
    @Redirect(
            method = "method_39801",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;scanChunk(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/StreamTagVisitor;)V"
            )
    )
    private void cesium$scanChunk(RegionFileStorage instance, ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) throws IOException {
        if (this.isCesium) {
            this.database
                    .getDatabase(this.databaseSpec)
                    .scan(chunkPos, streamTagVisitor);
        } else {
            instance.scanChunk(chunkPos, streamTagVisitor);
        }
    }

    /**
     * @author Yamayaki
     * @see IOWorker#synchronize(boolean)
     */
    @Redirect(
            method = "method_27946",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;flush()V"
            )
    )
    private void cesium$flush(RegionFileStorage instance) throws IOException {
        if (!this.isCesium) {
            instance.flush();
        }
    }

    @Redirect(
            method = "runStore",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;write(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    private void cesium$write(RegionFileStorage instance, ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        if (this.isCesium) {
            this.database
                    .getTransaction(this.databaseSpec)
                    .add(chunkPos, compoundTag);
        } else {
            instance.write(chunkPos, compoundTag);
        }
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/storage/RegionFileStorage;close()V"))
    private void cesium$close(RegionFileStorage instance) throws IOException {
        if (!this.isCesium) {
            instance.close();
        }
    }

    @Override
    public void cesium$setStorage(IDBInstance dbInstance) {
        this.isCesium = true;
        this.database = dbInstance;

        try {
            this.storage.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void cesium$setSpec(DatabaseSpec<?, ?> databaseSpec) {
        this.databaseSpec = (DatabaseSpec<ChunkPos, CompoundTag>) databaseSpec;
    }
}
