package com.example.mansbestfriend.event;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.entity.ai.goal.WanderAroundHomeGoal;
import com.example.mansbestfriend.util.HomeLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Game event handlers for the mod.
 * Registered to the NeoForge event bus.
 */
public class ModGameEvents {

    /**
     * Prevents damage to Forever Pets (except void and creative mode attacks).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal pet)) {
            return;
        }

        if (!pet.hasData(ModAttachments.FOREVER_PET_DATA.get())) {
            return;
        }

        ForeverPetData data = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return;
        }

        // Allow void damage
        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            MansBestFriend.LOGGER.debug("Forever Pet taking void damage - allowed");
            return;
        }

        // Allow creative mode player damage
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player && player.isCreative()) {
            MansBestFriend.LOGGER.debug("Forever Pet taking creative mode damage - allowed");
            return;
        }

        // Cancel all other damage
        event.setCanceled(true);
        MansBestFriend.LOGGER.debug("Cancelled damage to Forever Pet: {} from {}",
                pet.getName().getString(), event.getSource().type());
    }

    /**
     * Injects the WanderAroundHomeGoal into Forever Pets when they join the world.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof TamableAnimal pet)) {
            return;
        }

        if (!pet.hasData(ModAttachments.FOREVER_PET_DATA.get())) {
            return;
        }

        ForeverPetData data = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet()) {
            return;
        }

        // Check if we already have the wander goal
        boolean hasWanderGoal = pet.goalSelector.getAvailableGoals().stream()
                .anyMatch(g -> g.getGoal() instanceof WanderAroundHomeGoal);

        if (!hasWanderGoal) {
            // Add the wander goal with appropriate priority
            pet.goalSelector.addGoal(8, new WanderAroundHomeGoal(pet));
            MansBestFriend.LOGGER.debug("Added WanderAroundHomeGoal to Forever Pet: {}", pet.getName().getString());
        }
    }

    /**
     * Ensures roster data is properly copied when player respawns.
     * The copyOnDeath() flag should handle this, but this is a safety check.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player clone = event.getEntity();

            // The attachment system should handle this via copyOnDeath(),
            // but let's verify and copy if needed
            if (original.hasData(ModAttachments.PLAYER_PET_ROSTER.get())) {
                if (!clone.hasData(ModAttachments.PLAYER_PET_ROSTER.get()) ||
                        clone.getData(ModAttachments.PLAYER_PET_ROSTER.get()).equals(PlayerPetRosterData.DEFAULT)) {

                    PlayerPetRosterData originalRoster = original.getData(ModAttachments.PLAYER_PET_ROSTER.get());
                    clone.setData(ModAttachments.PLAYER_PET_ROSTER.get(), originalRoster);
                    MansBestFriend.LOGGER.debug("Manually copied roster data on player respawn");
                }
            }
        }
    }

    /**
     * Updates pet locations in the roster when a pet dies.
     */
    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof TamableAnimal pet)) {
            return;
        }

        if (!pet.hasData(ModAttachments.FOREVER_PET_DATA.get())) {
            return;
        }

        ForeverPetData data = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (!data.isForeverPet() || data.ownerUUID() == null) {
            return;
        }

        // Find the owner and update their roster
        if (pet.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Player owner = serverLevel.getServer().getPlayerList().getPlayer(data.ownerUUID());
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerPetRosterData roster = serverPlayer.getData(ModAttachments.PLAYER_PET_ROSTER.get());
                PlayerPetRosterData.PetEntry existing = roster.findPet(pet.getUUID());
                if (existing != null) {
                    HomeLocation lastLocation = new HomeLocation(pet.level().dimension(), pet.blockPosition());
                    PlayerPetRosterData.PetEntry updated = existing.withLocation(lastLocation, false);
                    serverPlayer.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withUpdatedPet(updated));
                    MansBestFriend.LOGGER.info("Updated roster: Forever Pet {} has died", pet.getName().getString());
                }
            }
        }
    }
}
