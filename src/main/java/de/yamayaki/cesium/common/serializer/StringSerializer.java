package de.yamayaki.cesium.common.serializer;

import de.yamayaki.cesium.api.io.ISerializer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class StringSerializer implements ISerializer<String> {
    @Override
    public byte @NotNull [] serialize(@NotNull String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public @NotNull String deserialize(byte @NotNull [] input) {
        return new String(input, StandardCharsets.UTF_8);
    }
}
