package com.example.mansbestfriend.network.payload;

import com.example.mansbestfriend.MansBestFriend;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Client -> Server packet to summon a pet to the player's location.
 * Sent from the Roster GUI when the "Summon" button is clicked.
 */
public record SummonPetPayload(UUID petUUID) implements CustomPacketPayload {

    public static final Type<SummonPetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MansBestFriend.MOD_ID, "summon_pet")
    );

    public static final StreamCodec<ByteBuf, SummonPetPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SummonPetPayload::petUUID,
            SummonPetPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
