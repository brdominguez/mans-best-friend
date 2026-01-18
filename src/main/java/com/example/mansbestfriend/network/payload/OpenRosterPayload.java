package com.example.mansbestfriend.network.payload;

import com.example.mansbestfriend.MansBestFriend;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> Server packet to request roster data sync.
 * Sent when the player opens the Roster GUI via keybind.
 */
public record OpenRosterPayload() implements CustomPacketPayload {

    public static final OpenRosterPayload INSTANCE = new OpenRosterPayload();

    public static final Type<OpenRosterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MansBestFriend.MOD_ID, "open_roster")
    );

    public static final StreamCodec<ByteBuf, OpenRosterPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
