package com.example.mansbestfriend.attachment;

import com.example.mansbestfriend.util.HomeLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Data attached to entities that have been given a Friendship Collar.
 * Stores whether they are a "Forever Pet" (invulnerable), their home location, and owner.
 */
public record ForeverPetData(
        boolean isForeverPet,
        @Nullable UUID ownerUUID,
        @Nullable HomeLocation homeLocation
) {

    public static final ForeverPetData DEFAULT = new ForeverPetData(false, null, null);

    public static final Codec<ForeverPetData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("is_forever_pet").forGetter(ForeverPetData::isForeverPet),
                    UUIDUtil.CODEC.optionalFieldOf("owner_uuid").forGetter(data -> Optional.ofNullable(data.ownerUUID)),
                    HomeLocation.CODEC.optionalFieldOf("home_location").forGetter(data -> Optional.ofNullable(data.homeLocation))
            ).apply(instance, (isForever, ownerOpt, homeOpt) ->
                    new ForeverPetData(isForever, ownerOpt.orElse(null), homeOpt.orElse(null)))
    );

    public static final StreamCodec<ByteBuf, ForeverPetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ForeverPetData::isForeverPet,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), data -> Optional.ofNullable(data.ownerUUID),
            ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), data -> Optional.ofNullable(data.homeLocation),
            (isForever, ownerOpt, homeOpt) -> new ForeverPetData(isForever, ownerOpt.orElse(null), homeOpt.orElse(null))
    );

    /**
     * Creates a new ForeverPetData marking the pet as a Forever Pet.
     */
    public ForeverPetData withForeverPet(UUID owner, @Nullable HomeLocation home) {
        return new ForeverPetData(true, owner, home);
    }

    /**
     * Creates a copy with a new home location.
     */
    public ForeverPetData withHomeLocation(@Nullable HomeLocation home) {
        return new ForeverPetData(this.isForeverPet, this.ownerUUID, home);
    }
}
