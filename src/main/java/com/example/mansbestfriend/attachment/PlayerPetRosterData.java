package com.example.mansbestfriend.attachment;

import com.example.mansbestfriend.util.HomeLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data attached to players tracking all their Forever Pets.
 */
public record PlayerPetRosterData(
        List<PetEntry> pets,
        @Nullable HomeLocation defaultHome
) {

    public static final PlayerPetRosterData DEFAULT = new PlayerPetRosterData(List.of(), null);

    public static final Codec<PlayerPetRosterData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PetEntry.CODEC.listOf().fieldOf("pets").forGetter(PlayerPetRosterData::pets),
                    HomeLocation.CODEC.optionalFieldOf("default_home").forGetter(data -> Optional.ofNullable(data.defaultHome))
            ).apply(instance, (pets, homeOpt) -> new PlayerPetRosterData(pets, homeOpt.orElse(null)))
    );

    public static final StreamCodec<ByteBuf, PlayerPetRosterData> STREAM_CODEC = StreamCodec.composite(
            PetEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerPetRosterData::pets,
            ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), data -> Optional.ofNullable(data.defaultHome),
            (pets, homeOpt) -> new PlayerPetRosterData(pets, homeOpt.orElse(null))
    );

    /**
     * Adds a pet to the roster.
     */
    public PlayerPetRosterData withAddedPet(PetEntry pet) {
        List<PetEntry> newPets = new ArrayList<>(pets);
        // Remove existing entry for this pet if present
        newPets.removeIf(p -> p.petUUID().equals(pet.petUUID()));
        newPets.add(pet);
        return new PlayerPetRosterData(newPets, defaultHome);
    }

    /**
     * Removes a pet from the roster.
     */
    public PlayerPetRosterData withRemovedPet(UUID petUUID) {
        List<PetEntry> newPets = new ArrayList<>(pets);
        newPets.removeIf(p -> p.petUUID().equals(petUUID));
        return new PlayerPetRosterData(newPets, defaultHome);
    }

    /**
     * Updates a pet entry in the roster.
     */
    public PlayerPetRosterData withUpdatedPet(PetEntry pet) {
        return withAddedPet(pet);
    }

    /**
     * Sets the default home location.
     */
    public PlayerPetRosterData withDefaultHome(@Nullable HomeLocation home) {
        return new PlayerPetRosterData(pets, home);
    }

    /**
     * Finds a pet by UUID.
     */
    @Nullable
    public PetEntry findPet(UUID petUUID) {
        return pets.stream()
                .filter(p -> p.petUUID().equals(petUUID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Entry for a single pet in the roster.
     */
    public record PetEntry(
            UUID petUUID,
            String petName,
            ResourceLocation entityType,
            @Nullable HomeLocation lastKnownLocation,
            @Nullable HomeLocation homeLocation,
            boolean isAlive
    ) {
        public static final Codec<PetEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("pet_uuid").forGetter(PetEntry::petUUID),
                        Codec.STRING.fieldOf("pet_name").forGetter(PetEntry::petName),
                        ResourceLocation.CODEC.fieldOf("entity_type").forGetter(PetEntry::entityType),
                        HomeLocation.CODEC.optionalFieldOf("last_known_location").forGetter(e -> Optional.ofNullable(e.lastKnownLocation)),
                        HomeLocation.CODEC.optionalFieldOf("home_location").forGetter(e -> Optional.ofNullable(e.homeLocation)),
                        Codec.BOOL.fieldOf("is_alive").forGetter(PetEntry::isAlive)
                ).apply(instance, (uuid, name, type, lastKnown, home, alive) ->
                        new PetEntry(uuid, name, type, lastKnown.orElse(null), home.orElse(null), alive))
        );

        public static final StreamCodec<ByteBuf, PetEntry> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, PetEntry::petUUID,
                ByteBufCodecs.STRING_UTF8, PetEntry::petName,
                ResourceLocation.STREAM_CODEC, PetEntry::entityType,
                ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), e -> Optional.ofNullable(e.lastKnownLocation),
                ByteBufCodecs.optional(HomeLocation.STREAM_CODEC), e -> Optional.ofNullable(e.homeLocation),
                ByteBufCodecs.BOOL, PetEntry::isAlive,
                (uuid, name, type, lastKnown, home, alive) ->
                        new PetEntry(uuid, name, type, lastKnown.orElse(null), home.orElse(null), alive)
        );

        /**
         * Creates an updated entry with new location info.
         */
        public PetEntry withLocation(HomeLocation location, boolean alive) {
            return new PetEntry(petUUID, petName, entityType, location, homeLocation, alive);
        }

        /**
         * Creates an updated entry with a new home location.
         */
        public PetEntry withHomeLocation(@Nullable HomeLocation home) {
            return new PetEntry(petUUID, petName, entityType, lastKnownLocation, home, isAlive);
        }
    }
}
