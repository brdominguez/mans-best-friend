package com.example.mansbestfriend.item;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.component.ModDataComponents;
import com.example.mansbestfriend.component.OcarinaColor;
import com.example.mansbestfriend.component.OcarinaData;
import com.example.mansbestfriend.util.HomeLocation;
import com.example.mansbestfriend.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * The Ocarina item.
 * - Sneak+Right-click on a Forever Pet to bind the ocarina to it
 * - Right-click (use) to summon the bound pet to the player
 * - Sneak+Right-click in air to send the bound pet home
 */
public class OcarinaItem extends Item {
    private final OcarinaColor color;

    public OcarinaItem(Properties properties, OcarinaColor color) {
        super(properties);
        this.color = color;
    }

    public OcarinaColor getColor() {
        return color;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        OcarinaData data = stack.getOrDefault(ModDataComponents.OCARINA_DATA.get(), OcarinaData.unbound(color));

        if (!data.isBound()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.mans_best_friend.ocarina.not_bound")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            UUID petUUID = data.boundPetUUID();

            if (player.isShiftKeyDown()) {
                // Sneak+Use: Send pet home
                sendPetHome(serverPlayer, petUUID, stack);
            } else {
                // Normal use: Summon pet to player
                summonPet(serverPlayer, petUUID, stack);
            }
        }

        // Play ocarina sound
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // Sneak+Right-click on a Forever Pet to bind
        if (player.isShiftKeyDown() && entity instanceof TamableAnimal pet) {
            if (!pet.isTame() || !pet.isOwnedBy(player)) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("item.mans_best_friend.ocarina.not_your_pet")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }

            ForeverPetData petData = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
            if (!petData.isForeverPet()) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("item.mans_best_friend.ocarina.not_forever_pet")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }

            if (!player.level().isClientSide) {
                // Bind the ocarina to this pet
                OcarinaData newData = new OcarinaData(pet.getUUID(), color);
                stack.set(ModDataComponents.OCARINA_DATA.get(), newData);

                String petName = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getType().getDescription().getString();
                player.displayClientMessage(
                        Component.translatable("item.mans_best_friend.ocarina.bound", petName)
                                .withStyle(ChatFormatting.GREEN),
                        true
                );

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.2F);

                MansBestFriend.LOGGER.info("Bound ocarina to pet {} (UUID: {})", petName, pet.getUUID());
            }

            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    private void summonPet(ServerPlayer player, UUID petUUID, ItemStack stack) {
        Entity pet = TeleportHelper.findPetInAllDimensions(player.server, petUUID);

        if (pet == null) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.pet_not_found")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        if (!(pet instanceof TamableAnimal tamable)) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.invalid_pet")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Verify ownership
        if (!tamable.isOwnedBy(player)) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.not_your_pet")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Teleport the pet to the player
        TeleportHelper.teleportEntityToPlayer(tamable, player);

        // Make the pet follow the player
        tamable.setOrderedToSit(false);

        String petName = tamable.hasCustomName() ? tamable.getCustomName().getString() : tamable.getType().getDescription().getString();
        player.displayClientMessage(
                Component.translatable("item.mans_best_friend.ocarina.summoned", petName)
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        // Update the pet's location in the roster
        updatePetLocationInRoster(player, tamable);

        MansBestFriend.LOGGER.info("Summoned pet {} to player {}", petName, player.getName().getString());
    }

    private void sendPetHome(ServerPlayer player, UUID petUUID, ItemStack stack) {
        Entity pet = TeleportHelper.findPetInAllDimensions(player.server, petUUID);

        if (pet == null) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.pet_not_found")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        if (!(pet instanceof TamableAnimal tamable)) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.invalid_pet")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Verify ownership
        if (!tamable.isOwnedBy(player)) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.not_your_pet")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Get home location
        ForeverPetData petData = tamable.getData(ModAttachments.FOREVER_PET_DATA.get());
        HomeLocation home = petData.homeLocation();

        // Fall back to player's default home
        if (home == null) {
            PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
            home = roster.defaultHome();
        }

        // Fall back to respawn point
        if (home == null) {
            home = TeleportHelper.getPlayerRespawnLocation(player);
        }

        if (home == null) {
            player.displayClientMessage(
                    Component.translatable("item.mans_best_friend.ocarina.no_home")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Teleport the pet home
        TeleportHelper.teleportEntityToLocation(tamable, player.server, home);

        String petName = tamable.hasCustomName() ? tamable.getCustomName().getString() : tamable.getType().getDescription().getString();
        player.displayClientMessage(
                Component.translatable("item.mans_best_friend.ocarina.sent_home", petName)
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        // Update the pet's location in the roster
        updatePetLocationInRoster(player, tamable);

        MansBestFriend.LOGGER.info("Sent pet {} home to {}", petName, home);
    }

    private void updatePetLocationInRoster(ServerPlayer player, TamableAnimal pet) {
        PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
        PlayerPetRosterData.PetEntry existing = roster.findPet(pet.getUUID());
        if (existing != null) {
            HomeLocation currentLocation = new HomeLocation(pet.level().dimension(), pet.blockPosition());
            PlayerPetRosterData.PetEntry updated = existing.withLocation(currentLocation, pet.isAlive());
            player.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withUpdatedPet(updated));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        OcarinaData data = stack.get(ModDataComponents.OCARINA_DATA.get());
        if (data != null && data.isBound()) {
            tooltipComponents.add(Component.translatable("item.mans_best_friend.ocarina.tooltip.bound")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.mans_best_friend.ocarina.tooltip.unbound")
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.translatable("item.mans_best_friend.ocarina.tooltip.hint_bind")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.add(Component.translatable("item.mans_best_friend.ocarina.tooltip.hint_use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
