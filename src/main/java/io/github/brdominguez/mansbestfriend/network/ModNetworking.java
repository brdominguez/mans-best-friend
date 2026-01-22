package io.github.brdominguez.mansbestfriend.network;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.attachment.PlayerPetRosterData;
import io.github.brdominguez.mansbestfriend.util.TeleportHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ModNetworking {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MansBestFriend.MODID);

        // Server -> Client packets
        registrar.playToClient(
                OpenRosterScreenPayload.TYPE,
                OpenRosterScreenPayload.STREAM_CODEC,
                ModNetworking::handleOpenRosterScreen
        );

        registrar.playToClient(
                SyncRosterDataPayload.TYPE,
                SyncRosterDataPayload.STREAM_CODEC,
                ModNetworking::handleSyncRosterData
        );

        // Client -> Server packets
        registrar.playToServer(
                RequestRosterDataPayload.TYPE,
                RequestRosterDataPayload.STREAM_CODEC,
                ModNetworking::handleRequestRosterData
        );

        registrar.playToServer(
                PetActionPayload.TYPE,
                PetActionPayload.STREAM_CODEC,
                ModNetworking::handlePetAction
        );
    }

    // Client-side handlers
    private static void handleOpenRosterScreen(OpenRosterScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This will be handled by client-side code
            io.github.brdominguez.mansbestfriend.screen.RosterScreen.openFromPacket();
        });
    }

    private static void handleSyncRosterData(SyncRosterDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            io.github.brdominguez.mansbestfriend.screen.RosterScreen.updatePetData(payload.pets());
        });
    }

    // Server-side handlers
    private static void handleRequestRosterData(RequestRosterDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                sendRosterDataToPlayer(serverPlayer);
            }
        });
    }

    private static void handlePetAction(PetActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                performPetAction(serverPlayer, payload.petUuid(), payload.action());
            }
        });
    }

    /**
     * Sends roster data to a player.
     */
    public static void sendRosterDataToPlayer(ServerPlayer player) {
        PlayerPetRosterData rosterData = player.getData(ModAttachments.PLAYER_PET_ROSTER_DATA.get());
        List<SyncRosterDataPayload.PetInfo> petInfos = new ArrayList<>();
        MinecraftServer server = player.level().getServer();

        for (UUID petUuid : rosterData.petUuids()) {
            Entity entity = TeleportHelper.findEntityAcrossDimensions(server, petUuid);

            if (entity instanceof TamableAnimal tamable) {
                ForeverPetData petData = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
                String name = petData.petName().orElse(tamable.getDisplayName().getString());
                String type = tamable.getType().getDescription().getString();
                petInfos.add(new SyncRosterDataPayload.PetInfo(petUuid, name, type, true));
            } else {
                // Pet not loaded or doesn't exist
                petInfos.add(new SyncRosterDataPayload.PetInfo(petUuid, "Unknown", "Unknown", false));
            }
        }

        PacketDistributor.sendToPlayer(player, new SyncRosterDataPayload(petInfos));
    }

    /**
     * Performs an action on a pet.
     */
    private static void performPetAction(ServerPlayer player, UUID petUuid, PetActionPayload.Action action) {
        MinecraftServer server = player.level().getServer();
        Entity entity = TeleportHelper.findEntityAcrossDimensions(server, petUuid);

        if (!(entity instanceof TamableAnimal tamable)) {
            player.displayClientMessage(
                    Component.translatable("gui.mansbestfriend.roster.pet_not_found"),
                    true
            );
            return;
        }

        // Verify ownership
        if (!tamable.isOwnedBy(player)) {
            player.displayClientMessage(
                    Component.translatable("gui.mansbestfriend.roster.not_your_pet"),
                    true
            );
            return;
        }

        ForeverPetData petData = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());

        switch (action) {
            case SUMMON -> {
                GlobalPos playerPos = GlobalPos.of(player.level().dimension(), player.blockPosition());
                boolean success = TeleportHelper.teleportEntity(tamable, server, playerPos);
                if (success) {
                    player.displayClientMessage(
                            Component.translatable("gui.mansbestfriend.roster.summoned", tamable.getDisplayName()),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.translatable("gui.mansbestfriend.roster.teleport_failed"),
                            true
                    );
                }
            }
            case SEND_HOME -> {
                Optional<GlobalPos> homePos = petData.homePos();
                if (homePos.isEmpty()) {
                    player.displayClientMessage(
                            Component.translatable("gui.mansbestfriend.roster.no_home"),
                            true
                    );
                    return;
                }
                boolean success = TeleportHelper.teleportEntity(tamable, server, homePos.get());
                if (success) {
                    player.displayClientMessage(
                            Component.translatable("gui.mansbestfriend.roster.sent_home", tamable.getDisplayName()),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.translatable("gui.mansbestfriend.roster.teleport_failed"),
                            true
                    );
                }
            }
            case SET_HOME -> {
                GlobalPos newHome = GlobalPos.of(player.level().dimension(), player.blockPosition());
                ForeverPetData updatedData = petData.withHomePos(newHome);
                tamable.setData(ModAttachments.FOREVER_PET_DATA.get(), updatedData);
                player.displayClientMessage(
                        Component.translatable("gui.mansbestfriend.roster.home_set",
                                tamable.getDisplayName(),
                                player.blockPosition().getX(),
                                player.blockPosition().getY(),
                                player.blockPosition().getZ()),
                        true
                );
            }
        }
    }
}
