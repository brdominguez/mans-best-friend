package com.example.mansbestfriend.network;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.network.payload.*;
import com.example.mansbestfriend.screen.RosterScreen;
import com.example.mansbestfriend.util.HomeLocation;
import com.example.mansbestfriend.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Registration and handling for network payloads.
 */
public class ModPayloads {

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MansBestFriend.MOD_ID);

        // Client -> Server
        registrar.playToServer(
                SummonPetPayload.TYPE,
                SummonPetPayload.STREAM_CODEC,
                ModPayloads::handleSummonPet
        );

        registrar.playToServer(
                SendPetHomePayload.TYPE,
                SendPetHomePayload.STREAM_CODEC,
                ModPayloads::handleSendPetHome
        );

        registrar.playToServer(
                SetDefaultHomePayload.TYPE,
                SetDefaultHomePayload.STREAM_CODEC,
                ModPayloads::handleSetDefaultHome
        );

        registrar.playToServer(
                OpenRosterPayload.TYPE,
                OpenRosterPayload.STREAM_CODEC,
                ModPayloads::handleOpenRoster
        );

        // Server -> Client
        registrar.playToClient(
                SyncPetRosterPayload.TYPE,
                SyncPetRosterPayload.STREAM_CODEC,
                ModPayloads::handleSyncPetRoster
        );
    }

    // === Server-side handlers ===

    private static void handleSummonPet(SummonPetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Entity pet = TeleportHelper.findPetInAllDimensions(player.server, payload.petUUID());

            if (pet == null) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.pet_not_found")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (!(pet instanceof TamableAnimal tamable)) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.invalid_pet")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (!tamable.isOwnedBy(player)) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.not_your_pet")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            TeleportHelper.teleportEntityToPlayer(tamable, player);
            tamable.setOrderedToSit(false);

            String petName = tamable.hasCustomName() ? tamable.getCustomName().getString() : tamable.getType().getDescription().getString();
            player.displayClientMessage(
                    Component.translatable("command.mans_best_friend.summoned", petName)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            // Update roster and sync
            updatePetInRoster(player, tamable);
            syncRosterToClient(player);
        });
    }

    private static void handleSendPetHome(SendPetHomePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            Entity pet = TeleportHelper.findPetInAllDimensions(player.server, payload.petUUID());

            if (pet == null) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.pet_not_found")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (!(pet instanceof TamableAnimal tamable)) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.invalid_pet")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            if (!tamable.isOwnedBy(player)) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.not_your_pet")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            HomeLocation home = TeleportHelper.getEffectiveHome(tamable, player);
            if (home == null) {
                player.displayClientMessage(
                        Component.translatable("command.mans_best_friend.no_home")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            TeleportHelper.teleportEntityToLocation(tamable, player.server, home);

            String petName = tamable.hasCustomName() ? tamable.getCustomName().getString() : tamable.getType().getDescription().getString();
            player.displayClientMessage(
                    Component.translatable("command.mans_best_friend.sent_home", petName)
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            // Update roster and sync
            updatePetInRoster(player, tamable);
            syncRosterToClient(player);
        });
    }

    private static void handleSetDefaultHome(SetDefaultHomePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
            player.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withDefaultHome(payload.home()));

            player.displayClientMessage(
                    Component.translatable("command.mans_best_friend.default_home_set",
                            payload.home().position().getX(),
                            payload.home().position().getY(),
                            payload.home().position().getZ())
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            syncRosterToClient(player);
        });
    }

    private static void handleOpenRoster(OpenRosterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            syncRosterToClient(player);
        });
    }

    // === Client-side handlers ===

    private static void handleSyncPetRoster(SyncPetRosterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the roster data client-side for the GUI
            RosterScreen.setClientRosterData(payload.pets(), payload.defaultHome());
        });
    }

    // === Helper methods ===

    private static void updatePetInRoster(ServerPlayer player, TamableAnimal pet) {
        PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
        PlayerPetRosterData.PetEntry existing = roster.findPet(pet.getUUID());
        if (existing != null) {
            HomeLocation currentLocation = new HomeLocation(pet.level().dimension(), pet.blockPosition());
            PlayerPetRosterData.PetEntry updated = existing.withLocation(currentLocation, pet.isAlive());
            player.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withUpdatedPet(updated));
        }
    }

    public static void syncRosterToClient(ServerPlayer player) {
        PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
        PacketDistributor.sendToPlayer(player, new SyncPetRosterPayload(roster.pets(), roster.defaultHome()));
    }
}
