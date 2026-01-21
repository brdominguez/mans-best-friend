package io.github.brdominguez.mansbestfriend.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Data component for Collar items - stores the home location set on the collar.
 */
public record CollarData(Optional<GlobalPos> homePos) {
    public static final CollarData EMPTY = new CollarData(Optional.empty());

    public static final Codec<CollarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("home_pos").forGetter(CollarData::homePos)
    ).apply(instance, CollarData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CollarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(GlobalPos.STREAM_CODEC),
            CollarData::homePos,
            CollarData::new
    );

    public CollarData withHomePos(GlobalPos pos) {
        return new CollarData(Optional.of(pos));
    }

    public boolean hasHomePos() {
        return homePos.isPresent();
    }
}
