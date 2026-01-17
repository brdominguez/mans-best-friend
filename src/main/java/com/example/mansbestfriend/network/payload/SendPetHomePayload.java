package com.example.mansbestfriend.network.payload;

import com.example.mansbestfriend.MansBestFriend;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Client -> Server packet to send a pet home.
 * Sent from the Roster GUI when the "Send Home" button is clicked.
 */
public record SendPetHomePayload(UUID petUUID) implements CustomPacketPayload {

    public static final Type<SendPetHomePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MansBestFriend.MOD_ID, "send_pet_home")
    );

    public static final StreamCodec<ByteBuf, SendPetHomePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendPetHomePayload::petUUID,
            SendPetHomePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
