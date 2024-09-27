package de.yamayaki.cesium.maintenance.storage.anvil;

import com.google.common.collect.ImmutableList;
import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.api.accessor.RawAccess;
import de.yamayaki.cesium.maintenance.storage.IChunkStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnvilChunkStorage implements IChunkStorage {
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");

    private final Path basePath;

    private final RegionFileStorage chunkData;
    private final RegionFileStorage poiData;
    private final RegionFileStorage entityData;

    public AnvilChunkStorage(final Path basePath) {
        this.basePath = basePath;

        this.chunkData = new RegionFileStorage(new RegionStorageInfo("cesium", null, "region"), basePath.resolve("region"), false);
        this.poiData = new RegionFileStorage(new RegionStorageInfo("cesium", null, "poi"), basePath.resolve("poi"), false);
        this.entityData = new RegionFileStorage(new RegionStorageInfo("cesium", null, "entities"), basePath.resolve("entities"), false);
    }

    @Override
    public List<Region> getAllRegions() {
        final File regionsFolder = new File(this.basePath.toFile(), "region");
        final File[] files = regionsFolder.listFiles((filex, string) -> string.endsWith(".mca"));

        if (files == null) {
            return ImmutableList.of();
        }

        final List<Region> regionList = new ArrayList<>();

        for (final File regionFile : files) {
            final Matcher matcher = REGEX.matcher(regionFile.getName());

            if (matcher.matches()) {
                final int regionX = Integer.parseInt(matcher.group(1));
                final int regionZ = Integer.parseInt(matcher.group(2));

                final Region region = Region.create(regionX, regionZ);

                for (int chunkX = 0; chunkX < 32; ++chunkX) {
                    for (int chunkZ = 0; chunkZ < 32; ++chunkZ) {
                        region.addChunk(new ChunkPos(chunkX + (regionX << 5), chunkZ + (regionZ << 5)));
                    }
                }

                regionList.add(region);
            }
        }

        return regionList;
    }

    @Override
    public void flush() {
        try {
            this.chunkData.flush();
            this.poiData.flush();
            this.chunkData.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.flush();

        try {
            this.chunkData.close();
            this.poiData.close();
            this.entityData.close();
        } catch (IOException exception) {
            CesiumMod.logger().warn("[ANVIL] Failed to close chunk storage", exception);
        }
    }

    @Override
    public synchronized void setChunkData(final ChunkPos chunkPos, final byte[] bytes) {
        try {
            ((RawAccess) (Object) this.chunkData).cesium$putBytes(chunkPos, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized byte[] getChunkData(final ChunkPos chunkPos) {
        try {
            return ((RawAccess) (Object) this.chunkData).cesium$getBytes(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void setPOIData(final ChunkPos chunkPos, final byte[] bytes) {
        try {
            ((RawAccess) (Object) this.poiData).cesium$putBytes(chunkPos, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized byte[] getPOIData(final ChunkPos chunkPos) {
        try {
            return ((RawAccess) (Object) this.poiData).cesium$getBytes(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void setEntityData(final ChunkPos chunkPos, final byte[] bytes) {
        try {
            ((RawAccess) (Object) this.entityData).cesium$putBytes(chunkPos, bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized byte[] getEntityData(final ChunkPos chunkPos) {
        try {
            return ((RawAccess) (Object) this.entityData).cesium$getBytes(chunkPos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
