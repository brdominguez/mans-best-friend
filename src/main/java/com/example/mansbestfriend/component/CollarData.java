package com.example.mansbestfriend.component;

import com.example.mansbestfriend.util.HomeLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Data component for Collar items.
 * Stores the home location that will be set on pets when the collar is applied.
 */
public record CollarData(@Nullable HomeLocation boundHome) {

    public static final CollarData EMPTY = new CollarData(null);

    public static final Codec<CollarData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    HomeLocation.CODEC.optionalFieldOf("bound_home").forGetter(data -> Optional.ofNullable(data.boundHome))
            ).apply(instance, opt -> new CollarData(opt.orElse(null)))
    );

    public static final StreamCodec<ByteBuf, CollarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), data -> Optional.ofNullable(data.boundHome),
            opt -> new CollarData(opt.orElse(null))
    );

    /**
     * Returns true if this collar has a home location set.
     */
    public boolean hasHome() {
        return boundHome != null;
    }

    /**
     * Creates a new CollarData with the given home location.
     */
    public CollarData withHome(HomeLocation home) {
        return new CollarData(home);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollarData that = (CollarData) o;
        if (boundHome == null) return that.boundHome == null;
        return boundHome.equals(that.boundHome);
    }

    @Override
    public int hashCode() {
        return boundHome != null ? boundHome.hashCode() : 0;
    }
}
