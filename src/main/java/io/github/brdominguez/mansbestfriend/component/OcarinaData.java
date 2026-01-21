package io.github.brdominguez.mansbestfriend.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * Data component for Ocarina items - stores the bound pet's UUID.
 */
public record OcarinaData(Optional<UUID> boundPetUuid) {
    public static final OcarinaData EMPTY = new OcarinaData(Optional.empty());

    public static final Codec<OcarinaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString)
                    .optionalFieldOf("bound_pet_uuid")
                    .forGetter(OcarinaData::boundPetUuid)
    ).apply(instance, OcarinaData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OcarinaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            OcarinaData::boundPetUuid,
            OcarinaData::new
    );

    public OcarinaData withBoundPet(UUID petUuid) {
        return new OcarinaData(Optional.of(petUuid));
    }

    public boolean hasBoundPet() {
        return boundPetUuid.isPresent();
    }

    public OcarinaData clearBoundPet() {
        return new OcarinaData(Optional.empty());
    }
}
