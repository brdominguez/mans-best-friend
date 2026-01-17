package com.example.mansbestfriend.entity.ai.goal;

import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.util.HomeLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * AI Goal that makes Forever Pets wander around their home location instead of sitting.
 * The pet will stay within a configurable radius of its home.
 */
public class WanderAroundHomeGoal extends Goal {
    private final TamableAnimal pet;
    private final double speedModifier;
    private final int wanderRadius;
    private final int interval;

    @Nullable
    private Vec3 targetPos;
    private int cooldown;

    public WanderAroundHomeGoal(TamableAnimal pet, double speedModifier, int wanderRadius) {
        this.pet = pet;
        this.speedModifier = speedModifier;
        this.wanderRadius = wanderRadius;
        this.interval = 120; // Ticks between wander attempts
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public WanderAroundHomeGoal(TamableAnimal pet) {
        this(pet, 1.0D, 10);
    }

    @Override
    public boolean canUse() {
        // Don't wander if following owner
        if (pet.isOrderedToSit()) {
            return false;
        }

        // Only wander if owner is not nearby (pet should follow owner when nearby)
        if (pet.getOwner() != null && pet.distanceToSqr(pet.getOwner()) < 144) { // 12 blocks
            return false;
        }

        // Check if pet has a home
        ForeverPetData petData = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!petData.isForeverPet() || petData.homeLocation() == null) {
            return false;
        }

        // Check if we're in the right dimension
        if (!pet.level().dimension().equals(petData.homeLocation().dimension())) {
            return false;
        }

        // Cooldown check
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        // Find a wander target within the home radius
        this.targetPos = findWanderTarget(petData.homeLocation());
        return this.targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !pet.getNavigation().isDone() && !pet.isOrderedToSit();
    }

    @Override
    public void start() {
        if (targetPos != null) {
            pet.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speedModifier);
        }
    }

    @Override
    public void stop() {
        pet.getNavigation().stop();
        this.cooldown = interval + pet.getRandom().nextInt(interval);
        this.targetPos = null;
    }

    @Nullable
    private Vec3 findWanderTarget(HomeLocation home) {
        BlockPos homePos = home.position();
        double currentDistSq = pet.blockPosition().distSqr(homePos);

        // If too far from home, try to move back toward home
        if (currentDistSq > wanderRadius * wanderRadius) {
            Vec3 homeVec = Vec3.atBottomCenterOf(homePos);
            return homeVec;
        }

        // Try to find a random position within the wander radius of home
        for (int attempts = 0; attempts < 10; attempts++) {
            Vec3 randomPos = DefaultRandomPos.getPos(pet, wanderRadius, 7);
            if (randomPos != null) {
                // Check if the random position is within range of home
                double distToHome = randomPos.distanceToSqr(homePos.getX(), homePos.getY(), homePos.getZ());
                if (distToHome <= wanderRadius * wanderRadius) {
                    return randomPos;
                }
            }
        }

        // Fall back to a random position near home
        int offsetX = pet.getRandom().nextInt(wanderRadius * 2) - wanderRadius;
        int offsetZ = pet.getRandom().nextInt(wanderRadius * 2) - wanderRadius;
        BlockPos targetPos = homePos.offset(offsetX, 0, offsetZ);

        // Find the ground level
        while (pet.level().isEmptyBlock(targetPos) && targetPos.getY() > pet.level().getMinBuildHeight()) {
            targetPos = targetPos.below();
        }
        targetPos = targetPos.above();

        // Make sure it's reachable
        Path path = pet.getNavigation().createPath(targetPos, 0);
        if (path != null && path.canReach()) {
            return Vec3.atBottomCenterOf(targetPos);
        }

        return null;
    }
}
