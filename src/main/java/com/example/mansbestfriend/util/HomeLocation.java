package com.example.mansbestfriend.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Represents a location in a specific dimension.
 * Used for pet home locations and teleportation destinations.
 */
public record HomeLocation(ResourceKey<Level> dimension, BlockPos position) {

    public static final Codec<HomeLocation> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(HomeLocation::dimension),
                    BlockPos.CODEC.fieldOf("position").forGetter(HomeLocation::position)
            ).apply(instance, HomeLocation::new)
    );

    public static final StreamCodec<ByteBuf, HomeLocation> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), HomeLocation::dimension,
            BlockPos.STREAM_CODEC, HomeLocation::position,
            HomeLocation::new
    );

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeLocation that = (HomeLocation) o;
        return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, position);
    }

    @Override
    public String toString() {
        return "HomeLocation{dimension=" + dimension.location() + ", position=" + position + "}";
    }
}
