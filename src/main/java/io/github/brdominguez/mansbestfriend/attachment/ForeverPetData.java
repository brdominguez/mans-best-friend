package io.github.brdominguez.mansbestfriend.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * Data attachment for "Forever Pets" - tamed animals that have been made invulnerable with a collar.
 * Attached to TamableAnimal entities.
 */
public record ForeverPetData(
        boolean isForeverPet,
        Optional<GlobalPos> homePos,
        Optional<UUID> ownerUuid,
        Optional<String> petName
) {
    public static final ForeverPetData DEFAULT = new ForeverPetData(false, Optional.empty(), Optional.empty(), Optional.empty());

    public static final MapCodec<ForeverPetData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("is_forever_pet").forGetter(ForeverPetData::isForeverPet),
            GlobalPos.CODEC.optionalFieldOf("home_pos").forGetter(ForeverPetData::homePos),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("owner_uuid").forGetter(ForeverPetData::ownerUuid),
            Codec.STRING.optionalFieldOf("pet_name").forGetter(ForeverPetData::petName)
    ).apply(instance, ForeverPetData::new));

    public ForeverPetData withForeverPet(boolean isForeverPet) {
        return new ForeverPetData(isForeverPet, this.homePos, this.ownerUuid, this.petName);
    }

    public ForeverPetData withHomePos(GlobalPos pos) {
        return new ForeverPetData(this.isForeverPet, Optional.of(pos), this.ownerUuid, this.petName);
    }

    public ForeverPetData withHomePos(ResourceKey<Level> dimension, BlockPos pos) {
        return withHomePos(GlobalPos.of(dimension, pos));
    }

    public ForeverPetData withOwner(UUID owner) {
        return new ForeverPetData(this.isForeverPet, this.homePos, Optional.of(owner), this.petName);
    }

    public ForeverPetData withPetName(String name) {
        return new ForeverPetData(this.isForeverPet, this.homePos, this.ownerUuid, Optional.of(name));
    }

    public ForeverPetData clearHomePos() {
        return new ForeverPetData(this.isForeverPet, Optional.empty(), this.ownerUuid, this.petName);
    }
}
