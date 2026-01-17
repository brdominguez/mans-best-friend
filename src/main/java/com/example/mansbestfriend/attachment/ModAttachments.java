package com.example.mansbestfriend.attachment;

import com.example.mansbestfriend.MansBestFriend;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registration for data attachments (entity and player data).
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MansBestFriend.MOD_ID);

    /**
     * Attachment for Forever Pet data on tameable entities.
     * Stores whether the pet is a Forever Pet, owner UUID, and home location.
     */
    public static final Supplier<AttachmentType<ForeverPetData>> FOREVER_PET_DATA =
            ATTACHMENTS.register("forever_pet_data", () ->
                    AttachmentType.builder(() -> ForeverPetData.DEFAULT)
                            .serialize(ForeverPetData.CODEC)
                            .build()
            );

    /**
     * Attachment for player's pet roster.
     * Stores list of all Forever Pets owned by the player and default home.
     */
    public static final Supplier<AttachmentType<PlayerPetRosterData>> PLAYER_PET_ROSTER =
            ATTACHMENTS.register("player_pet_roster", () ->
                    AttachmentType.builder(() -> PlayerPetRosterData.DEFAULT)
                            .serialize(PlayerPetRosterData.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
