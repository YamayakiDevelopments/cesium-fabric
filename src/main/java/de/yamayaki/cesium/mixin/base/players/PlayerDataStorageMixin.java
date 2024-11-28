package de.yamayaki.cesium.mixin.base.players;

import com.llamalad7.mixinextras.sugar.Local;
import de.yamayaki.cesium.api.accessor.IStorageSetter;
import de.yamayaki.cesium.storage.IComponentStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin implements IStorageSetter<UUID, CompoundTag> {
    /* Our own storage*/
    @Unique private IComponentStorage<UUID, CompoundTag> storage;

    @Override
    public void cesium$setStorage(final @NotNull IComponentStorage<UUID, CompoundTag> storage) {
        this.storage = storage;
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;mkdirs()Z"
            )
    )
    private boolean disableMkdirs(File file) {
        return true;
    }

    @Redirect(
            method = "load(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;exists()Z"
            )
    )
    public boolean redirectFileExists(File instance) {
        return true;
    }

    @Redirect(
            method = "load(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;isFile()Z"
            )
    )
    public boolean redirectFileIsFile(File instance) {
        return true;
    }

    @Redirect(
            method = "load(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtIo;readCompressed(Ljava/nio/file/Path;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    public CompoundTag redirectPlayerLoad(Path path, NbtAccounter nbtAccounter, @Local(argsOnly = true) Player player) {
        return this.storage.getValue(player.getUUID());
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;createTempFile(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"
            )
    )
    public Path disableFileCreation(Path path, String a, String b, FileAttribute<?>[] fileAttributes) {
        return null;
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"
            )
    )
    public void redirectWrite(CompoundTag compoundTag, Path path, @Local(argsOnly = true) Player player) {
        this.storage.putValue(player.getUUID(), compoundTag);
    }

    @Redirect(
            method = "save",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V"
            )
    )
    public void disableFileMove(Path path, Path path2, Path path3) {
        // Do nothing
    }
}
