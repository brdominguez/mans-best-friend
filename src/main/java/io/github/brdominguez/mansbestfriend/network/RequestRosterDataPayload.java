package io.github.brdominguez.mansbestfriend.network;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Packet sent from client to server to request roster data.
 */
public record RequestRosterDataPayload() implements CustomPacketPayload {
    public static final Type<RequestRosterDataPayload> TYPE = new Type<>(
            MansBestFriend.location("request_roster_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestRosterDataPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestRosterDataPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
