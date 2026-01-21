package io.github.brdominguez.mansbestfriend.attachment;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MansBestFriend.MODID);

    public static final Supplier<AttachmentType<ForeverPetData>> FOREVER_PET_DATA = ATTACHMENT_TYPES.register(
            "forever_pet_data",
            () -> AttachmentType.builder(() -> ForeverPetData.DEFAULT)
                    .serialize(ForeverPetData.CODEC)
                    .build()
    );

    public static final Supplier<AttachmentType<PlayerPetRosterData>> PLAYER_PET_ROSTER_DATA = ATTACHMENT_TYPES.register(
            "player_pet_roster_data",
            () -> AttachmentType.builder(() -> PlayerPetRosterData.DEFAULT)
                    .serialize(PlayerPetRosterData.CODEC)
                    .copyOnDeath()
                    .build()
    );
}
