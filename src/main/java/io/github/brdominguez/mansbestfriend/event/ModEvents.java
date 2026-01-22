package io.github.brdominguez.mansbestfriend.event;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.attachment.PlayerPetRosterData;
import io.github.brdominguez.mansbestfriend.entity.ai.goal.WanderAroundHomeGoal;
import io.github.brdominguez.mansbestfriend.item.ModItems;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Game event handlers for Forever Pet functionality.
 */
@EventBusSubscriber(modid = MansBestFriend.MODID)
public class ModEvents {

    /**
     * Handles collar application via entity interaction event.
     * This runs at high priority to intercept before the wolf's sit/stand behavior.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        // Only handle collar items
        if (!stack.is(ModItems.COLLAR.get())) {
            return;
        }

        // Only handle tamable animals
        if (!(event.getTarget() instanceof TamableAnimal tamable)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.not_tamable"),
                        true
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        // Must be tamed and owned by the player
        if (!tamable.isTame() || !tamable.isOwnedBy(player)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.not_your_pet"),
                        true
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        // Check if already a Forever Pet
        ForeverPetData existingData = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (existingData.isForeverPet()) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.already_forever"),
                        true
                );
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        // Apply the collar (server-side only)
        if (!player.level().isClientSide()) {
            // Use pet's current position as initial home (can be changed via Pet Roster)
            GlobalPos homePos = GlobalPos.of(tamable.level().dimension(), tamable.blockPosition());

            // Create Forever Pet data
            ForeverPetData petData = new ForeverPetData(
                    true,
                    java.util.Optional.of(homePos),
                    java.util.Optional.of(player.getUUID()),
                    tamable.hasCustomName() ? java.util.Optional.of(tamable.getCustomName().getString()) : java.util.Optional.empty()
            );

            // Apply to pet
            tamable.setData(ModAttachments.FOREVER_PET_DATA.get(), petData);

            // Make the pet stand up (not sitting)
            tamable.setOrderedToSit(false);

            // Add to player's roster
            PlayerPetRosterData rosterData = player.getData(ModAttachments.PLAYER_PET_ROSTER_DATA.get());
            player.setData(ModAttachments.PLAYER_PET_ROSTER_DATA.get(), rosterData.addPet(tamable.getUUID()));

            // Consume the collar
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.displayClientMessage(
                    Component.translatable("item.mansbestfriend.collar.applied",
                            tamable.getDisplayName()),
                    true
            );

            // Play a special sound
            player.level().playSound(null, tamable.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.0f);

            MansBestFriend.LOGGER.info("Made {} a Forever Pet for player {}",
                    tamable.getDisplayName().getString(), player.getName().getString());
        }

        // Cancel the event to prevent sit/stand behavior
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

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

            // Remove follow owner goal (we want them to stay at home, not follow/teleport to owner)
            pathfinderMob.goalSelector.getAvailableGoals().removeIf(
                    goal -> goal.getGoal() instanceof FollowOwnerGoal
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
