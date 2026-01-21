package io.github.brdominguez.mansbestfriend.event;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.attachment.PlayerPetRosterData;
import io.github.brdominguez.mansbestfriend.entity.ai.goal.WanderAroundHomeGoal;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Game event handlers for Forever Pet functionality.
 */
@EventBusSubscriber(modid = MansBestFriend.MODID)
public class ModEvents {

    /**
     * Prevents damage to Forever Pets (except void damage and /kill command).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof TamableAnimal tamable)) {
            return;
        }

        ForeverPetData data = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return;
        }

        // Allow void damage (falling out of world)
        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }

        // Allow /kill command (generic kill)
        if (event.getSource().is(DamageTypes.GENERIC_KILL)) {
            return;
        }

        // Cancel all other damage
        event.setNewDamage(0);
    }

    /**
     * Injects custom AI goals into Forever Pets when they join the world.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal tamable)) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        ForeverPetData data = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return;
        }

        // Add wandering AI goal
        if (tamable instanceof PathfinderMob pathfinderMob) {
            // Remove sitting goal behavior (we want them to wander, not sit)
            pathfinderMob.goalSelector.getAvailableGoals().removeIf(
                    goal -> goal.getGoal() instanceof SitWhenOrderedToGoal
            );

            // Add our custom wandering goal with high priority
            pathfinderMob.goalSelector.addGoal(2, new WanderAroundHomeGoal(
                    pathfinderMob,
                    1.0D,  // speed
                    10,    // wander radius
                    32     // max distance from home
            ));

            // Make sure they're not sitting
            tamable.setOrderedToSit(false);
        }

        MansBestFriend.LOGGER.debug("Injected AI goals for Forever Pet: {}", tamable.getDisplayName().getString());
    }

    /**
     * Cleans up roster when a Forever Pet is removed from the world permanently.
     */
    @SubscribeEvent
    public static void onEntityRemoved(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal tamable)) {
            return;
        }

        ForeverPetData data = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return;
        }

        // This shouldn't normally happen since Forever Pets are invulnerable
        // But handle it just in case (void damage, /kill, etc.)
        data.ownerUuid().ifPresent(ownerUuid -> {
            Player owner = tamable.level().getPlayerByUUID(ownerUuid);
            if (owner != null) {
                PlayerPetRosterData rosterData = owner.getData(ModAttachments.PLAYER_PET_ROSTER_DATA.get());
                owner.setData(ModAttachments.PLAYER_PET_ROSTER_DATA.get(),
                        rosterData.removePet(tamable.getUUID()));
                MansBestFriend.LOGGER.info("Removed {} from {}'s roster due to death",
                        tamable.getDisplayName().getString(), owner.getName().getString());
            }
        });
    }
}
