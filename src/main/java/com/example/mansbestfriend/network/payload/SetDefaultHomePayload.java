package com.example.mansbestfriend.network.payload;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.util.HomeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> Server packet to set the player's default home location for all pets.
 * Sent from the Roster GUI when the "Set Default Home" button is clicked.
 */
public record SetDefaultHomePayload(HomeLocation home) implements CustomPacketPayload {

    public static final Type<SetDefaultHomePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MansBestFriend.MOD_ID, "set_default_home")
    );

    public static final StreamCodec<ByteBuf, SetDefaultHomePayload> STREAM_CODEC = StreamCodec.composite(
            HomeLocation.STREAM_CODEC, SetDefaultHomePayload::home,
            SetDefaultHomePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
