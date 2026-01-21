package io.github.brdominguez.mansbestfriend.util;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

/**
 * Helper class for teleporting entities, including cross-dimension teleportation.
 */
public class TeleportHelper {

    /**
     * Teleports an entity to a GlobalPos, handling cross-dimension teleportation.
     *
     * @param entity The entity to teleport
     * @param server The Minecraft server
     * @param destination The destination GlobalPos
     * @return true if teleportation was successful
     */
    public static boolean teleportEntity(Entity entity, MinecraftServer server, GlobalPos destination) {
        if (entity == null || server == null || destination == null) {
            return false;
        }

        ResourceKey<Level> targetDimension = destination.dimension();
        BlockPos targetPos = destination.pos();
        ServerLevel targetLevel = server.getLevel(targetDimension);

        if (targetLevel == null) {
            MansBestFriend.LOGGER.warn("Could not find dimension {} for teleportation", targetDimension);
            return false;
        }

        // Find a safe position
        BlockPos safePos = findSafePosition(targetLevel, targetPos);
        double x = safePos.getX() + 0.5;
        double y = safePos.getY();
        double z = safePos.getZ() + 0.5;

        // For tamable animals, we need to handle them specially
        if (entity instanceof TamableAnimal tamable) {
            // Make sure they're not sitting when teleported
            tamable.setOrderedToSit(false);
        }

        try {
            // Create a TeleportTransition for the teleport
            TeleportTransition transition = new TeleportTransition(
                    targetLevel,
                    new Vec3(x, y, z),
                    Vec3.ZERO,
                    entity.getYRot(),
                    entity.getXRot(),
                    Set.of(),
                    TeleportTransition.DO_NOTHING
            );

            // Perform the teleport
            Entity result = entity.teleport(transition);
            return result != null;
        } catch (Exception e) {
            MansBestFriend.LOGGER.error("Failed to teleport entity", e);
            return false;
        }
    }

    /**
     * Finds a safe position near the target position.
     */
    private static BlockPos findSafePosition(ServerLevel level, BlockPos targetPos) {
        // Check if target position is safe
        if (isSafePosition(level, targetPos)) {
            return targetPos;
        }

        // Search in a small area for a safe spot
        for (int xOffset = -2; xOffset <= 2; xOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
                for (int yOffset = -2; yOffset <= 2; yOffset++) {
                    BlockPos checkPos = targetPos.offset(xOffset, yOffset, zOffset);
                    if (isSafePosition(level, checkPos)) {
                        return checkPos;
                    }
                }
            }
        }

        // If no safe position found, return target anyway
        return targetPos.above();
    }

    /**
     * Checks if a position is safe for teleportation.
     */
    private static boolean isSafePosition(ServerLevel level, BlockPos pos) {
        // Check that the block at feet level and head level are passable
        boolean feetClear = level.getBlockState(pos).isAir() || !level.getBlockState(pos).isSolid();
        boolean headClear = level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above()).isSolid();
        boolean groundSolid = level.getBlockState(pos.below()).isSolid();

        return feetClear && headClear && groundSolid;
    }

    /**
     * Finds an entity by UUID across all dimensions.
     *
     * @param server The Minecraft server
     * @param entityUuid The UUID of the entity to find
     * @return The entity, or null if not found
     */
    public static Entity findEntityAcrossDimensions(MinecraftServer server, UUID entityUuid) {
        if (server == null || entityUuid == null) {
            return null;
        }

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityUuid);
            if (entity != null) {
                return entity;
            }
        }

        return null;
    }
}
