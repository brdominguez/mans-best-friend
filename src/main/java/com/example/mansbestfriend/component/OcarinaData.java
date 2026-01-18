package com.example.mansbestfriend.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Data component for Ocarina items.
 * Stores the UUID of the bound pet.
 */
public record OcarinaData(@Nullable UUID boundPetUUID) {

    public static final Codec<OcarinaData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("bound_pet_uuid").forGetter(data -> Optional.ofNullable(data.boundPetUUID))
            ).apply(instance, uuidOpt -> new OcarinaData(uuidOpt.orElse(null)))
    );

    public static final StreamCodec<ByteBuf, OcarinaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.boundPetUUID),
            uuidOpt -> new OcarinaData(uuidOpt.orElse(null))
    );

    /**
     * Creates an unbound ocarina.
     */
    public static OcarinaData unbound() {
        return new OcarinaData(null);
    }

    /**
     * Returns true if this ocarina is bound to a pet.
     */
    public boolean isBound() {
        return boundPetUUID != null;
    }

    /**
     * Creates a new OcarinaData bound to the given pet.
     */
    public OcarinaData withBoundPet(UUID petUUID) {
        return new OcarinaData(petUUID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OcarinaData that = (OcarinaData) o;
        return Objects.equals(boundPetUUID, that.boundPetUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundPetUUID);
    }
}
