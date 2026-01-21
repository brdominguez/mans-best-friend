package io.github.brdominguez.mansbestfriend.entity.ai.goal;

import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

/**
 * AI goal that makes Forever Pets wander around their home position
 * instead of sitting still or following the player.
 */
public class WanderAroundHomeGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final int wanderRadius;
    private final int maxDistanceFromHome;

    private double targetX;
    private double targetY;
    private double targetZ;

    public WanderAroundHomeGoal(PathfinderMob mob, double speed, int wanderRadius, int maxDistanceFromHome) {
        this.mob = mob;
        this.speed = speed;
        this.wanderRadius = wanderRadius;
        this.maxDistanceFromHome = maxDistanceFromHome;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only use if this is a Forever Pet with a home
        ForeverPetData data = mob.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return false;
        }

        Optional<GlobalPos> homePos = data.homePos();
        if (homePos.isEmpty()) {
            return false;
        }

        // Only wander if in the same dimension as home
        GlobalPos home = homePos.get();
        if (!mob.level().dimension().equals(home.dimension())) {
            return false;
        }

        // Check if we need to return home (too far away)
        BlockPos homeBlockPos = home.pos();
        double distanceToHome = mob.blockPosition().distSqr(homeBlockPos);

        if (distanceToHome > maxDistanceFromHome * maxDistanceFromHome) {
            // Need to go back home
            targetX = homeBlockPos.getX() + 0.5;
            targetY = homeBlockPos.getY();
            targetZ = homeBlockPos.getZ() + 0.5;
            return true;
        }

        // Random chance to wander
        if (mob.getRandom().nextInt(120) != 0) {
            return false;
        }

        // Find a random position near home
        Vec3 wanderTarget = DefaultRandomPos.getPosTowards(
                mob,
                wanderRadius,
                7,
                new Vec3(homeBlockPos.getX(), homeBlockPos.getY(), homeBlockPos.getZ()),
                Math.PI / 2
        );

        if (wanderTarget == null) {
            return false;
        }

        targetX = wanderTarget.x;
        targetY = wanderTarget.y;
        targetZ = wanderTarget.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
