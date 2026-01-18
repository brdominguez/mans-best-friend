package com.example.mansbestfriend.util;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Helper class for teleporting pets across dimensions.
 */
public class TeleportHelper {

    /**
     * Finds a pet by UUID across all loaded dimensions.
     */
    @Nullable
    public static Entity findPetInAllDimensions(MinecraftServer server, UUID petUUID) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(petUUID);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Teleports an entity to a player's location, handling cross-dimension travel.
     */
    public static void teleportEntityToPlayer(Entity entity, ServerPlayer player) {
        ServerLevel targetLevel = player.serverLevel();
        BlockPos targetPos = player.blockPosition();
        teleportEntity(entity, targetLevel, Vec3.atBottomCenterOf(targetPos));
    }

    /**
     * Teleports an entity to a specific home location, handling cross-dimension travel.
     */
    public static void teleportEntityToLocation(Entity entity, MinecraftServer server, HomeLocation home) {
        ServerLevel targetLevel = server.getLevel(home.dimension());
        if (targetLevel == null) {
            MansBestFriend.LOGGER.warn("Target dimension {} not found for teleport", home.dimension().location());
            return;
        }
        teleportEntity(entity, targetLevel, Vec3.atBottomCenterOf(home.position()));
    }

    /**
     * Core teleport logic that handles same-dimension and cross-dimension travel.
     */
    private static void teleportEntity(Entity entity, ServerLevel targetLevel, Vec3 targetPos) {
        if (entity.level() == targetLevel) {
            // Same dimension - simple teleport
            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            entity.setDeltaMovement(Vec3.ZERO);
        } else {
            // Cross-dimension teleport
            DimensionTransition transition = new DimensionTransition(
                    targetLevel,
                    targetPos,
                    Vec3.ZERO,
                    entity.getYRot(),
                    entity.getXRot(),
                    DimensionTransition.DO_NOTHING
            );

            // Store the attachment data before dimension change
            ForeverPetData petData = null;
            if (entity.hasData(ModAttachments.FOREVER_PET_DATA.get())) {
                petData = entity.getData(ModAttachments.FOREVER_PET_DATA.get());
            }

            Entity newEntity = entity.changeDimension(transition);

            // Copy attachment data to the new entity if it was recreated
            if (newEntity != null && newEntity != entity && petData != null) {
                newEntity.setData(ModAttachments.FOREVER_PET_DATA.get(), petData);
                MansBestFriend.LOGGER.debug("Copied pet data to new entity after dimension change");
            }
        }
    }

    /**
     * Gets the player's respawn location as a HomeLocation.
     */
    @Nullable
    public static HomeLocation getPlayerRespawnLocation(ServerPlayer player) {
        BlockPos respawnPos = player.getRespawnPosition();
        ResourceKey<Level> respawnDim = player.getRespawnDimension();

        if (respawnPos != null) {
            return new HomeLocation(respawnDim, respawnPos);
        }

        // Fall back to world spawn
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            BlockPos worldSpawn = overworld.getSharedSpawnPos();
            return new HomeLocation(Level.OVERWORLD, worldSpawn);
        }

        return null;
    }

    /**
     * Gets the effective home location for a pet, checking pet data, default home, and respawn.
     */
    @Nullable
    public static HomeLocation getEffectiveHome(TamableAnimal pet, ServerPlayer owner) {
        // First, check pet's own home
        ForeverPetData petData = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (petData.homeLocation() != null) {
            return petData.homeLocation();
        }

        // Fall back to owner's default home
        var roster = owner.getData(ModAttachments.PLAYER_PET_ROSTER.get());
        if (roster.defaultHome() != null) {
            return roster.defaultHome();
        }

        // Fall back to owner's respawn point
        return getPlayerRespawnLocation(owner);
    }
}
