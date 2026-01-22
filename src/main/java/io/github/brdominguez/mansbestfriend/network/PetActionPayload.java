package io.github.brdominguez.mansbestfriend.network;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

/**
 * Packet sent from client to server to perform actions on pets (summon, send home).
 */
public record PetActionPayload(UUID petUuid, Action action) implements CustomPacketPayload {
    public static final Type<PetActionPayload> TYPE = new Type<>(
            MansBestFriend.location("pet_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PetActionPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PetActionPayload::petUuid,
            ByteBufCodecs.VAR_INT.map(Action::fromId, Action::getId),
            PetActionPayload::action,
            PetActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        SUMMON(0),
        SEND_HOME(1),
        SET_HOME(2);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Action fromId(int id) {
            return switch (id) {
                case 0 -> SUMMON;
                case 1 -> SEND_HOME;
                case 2 -> SET_HOME;
                default -> SUMMON;
            };
        }
    }
}
