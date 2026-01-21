package io.github.brdominguez.mansbestfriend.network;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Packet sent from server to client to open the roster GUI screen.
 */
public record OpenRosterScreenPayload() implements CustomPacketPayload {
    public static final Type<OpenRosterScreenPayload> TYPE = new Type<>(
            MansBestFriend.location("open_roster_screen")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRosterScreenPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenRosterScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
