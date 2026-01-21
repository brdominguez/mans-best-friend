package io.github.brdominguez.mansbestfriend.network;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

/**
 * Packet sent from server to client to sync pet roster data.
 */
public record SyncRosterDataPayload(List<PetInfo> pets) implements CustomPacketPayload {
    public static final Type<SyncRosterDataPayload> TYPE = new Type<>(
            MansBestFriend.location("sync_roster_data")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRosterDataPayload> STREAM_CODEC = StreamCodec.composite(
            PetInfo.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SyncRosterDataPayload::pets,
            SyncRosterDataPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Information about a pet for the roster GUI.
     */
    public record PetInfo(UUID uuid, String name, String type, boolean isLoaded) {
        public static final StreamCodec<RegistryFriendlyByteBuf, PetInfo> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                PetInfo::uuid,
                ByteBufCodecs.STRING_UTF8,
                PetInfo::name,
                ByteBufCodecs.STRING_UTF8,
                PetInfo::type,
                ByteBufCodecs.BOOL,
                PetInfo::isLoaded,
                PetInfo::new
        );
    }
}
