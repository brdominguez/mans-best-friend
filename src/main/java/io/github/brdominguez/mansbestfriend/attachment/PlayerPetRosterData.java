package io.github.brdominguez.mansbestfriend.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Data attachment for players to track their Forever Pets.
 * Uses copyOnDeath() to persist across player deaths.
 */
public record PlayerPetRosterData(
        Set<UUID> petUuids,
        Optional<GlobalPos> defaultHomePos
) {
    public static final PlayerPetRosterData DEFAULT = new PlayerPetRosterData(new HashSet<>(), Optional.empty());

    public static final MapCodec<PlayerPetRosterData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).listOf()
                    .xmap(list -> (Set<UUID>) new HashSet<>(list), list -> new ArrayList<>(list))
                    .fieldOf("pet_uuids").forGetter(PlayerPetRosterData::petUuids),
            GlobalPos.CODEC.optionalFieldOf("default_home_pos").forGetter(PlayerPetRosterData::defaultHomePos)
    ).apply(instance, PlayerPetRosterData::new));

    public PlayerPetRosterData addPet(UUID petUuid) {
        Set<UUID> newSet = new HashSet<>(this.petUuids);
        newSet.add(petUuid);
        return new PlayerPetRosterData(newSet, this.defaultHomePos);
    }

    public PlayerPetRosterData removePet(UUID petUuid) {
        Set<UUID> newSet = new HashSet<>(this.petUuids);
        newSet.remove(petUuid);
        return new PlayerPetRosterData(newSet, this.defaultHomePos);
    }

    public PlayerPetRosterData withDefaultHomePos(GlobalPos pos) {
        return new PlayerPetRosterData(this.petUuids, Optional.of(pos));
    }

    public PlayerPetRosterData withDefaultHomePos(ResourceKey<Level> dimension, BlockPos pos) {
        return withDefaultHomePos(GlobalPos.of(dimension, pos));
    }

    public PlayerPetRosterData clearDefaultHomePos() {
        return new PlayerPetRosterData(this.petUuids, Optional.empty());
    }

    public boolean hasPet(UUID petUuid) {
        return this.petUuids.contains(petUuid);
    }

    public int getPetCount() {
        return this.petUuids.size();
    }
}
