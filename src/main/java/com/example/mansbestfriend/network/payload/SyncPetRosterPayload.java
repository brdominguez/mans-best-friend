package com.example.mansbestfriend.network.payload;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.util.HomeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Server -> Client packet to sync the player's pet roster data.
 * Sent when the player opens the Roster GUI or when roster data changes.
 */
public record SyncPetRosterPayload(
        List<PlayerPetRosterData.PetEntry> pets,
        @Nullable HomeLocation defaultHome
) implements CustomPacketPayload {

    public static final Type<SyncPetRosterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MansBestFriend.MOD_ID, "sync_pet_roster")
    );

    public static final StreamCodec<ByteBuf, SyncPetRosterPayload> STREAM_CODEC = StreamCodec.composite(
            PlayerPetRosterData.PetEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncPetRosterPayload::pets,
            ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), payload -> Optional.ofNullable(payload.defaultHome),
            (pets, homeOpt) -> new SyncPetRosterPayload(pets, homeOpt.orElse(null))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
