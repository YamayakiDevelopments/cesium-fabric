package de.yamayaki.cesium.common;

import de.yamayaki.cesium.api.io.ICompressor;
import de.yamayaki.cesium.common.zstd.ZSTDCompressor;
import org.jetbrains.annotations.NotNull;

public class DefaultCompressors {
    public static final ICompressor NONE = new ICompressor() {
        @Override
        public byte @NotNull [] compress(byte @NotNull [] input) {
            return input;
        }

        @Override
        public byte @NotNull [] decompress(byte @NotNull [] input) {
            return input;
        }
    };

    public static final ICompressor ZSTD = new ZSTDCompressor();
}
