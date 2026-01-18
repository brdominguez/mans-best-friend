package com.example.mansbestfriend.item;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.ForeverPetData;
import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.component.CollarData;
import com.example.mansbestfriend.component.ModDataComponents;
import com.example.mansbestfriend.util.HomeLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Friendship Collar item.
 * - Sneak+Right-click on a block to set the home location on the collar
 * - Right-click on a tamed pet to apply the collar and make it a Forever Pet
 */
public class CollarItem extends Item {

    public CollarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Sneak+Right-click on block to set home location
        if (player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();

            // Set the home location on the collar
            HomeLocation home = new HomeLocation(level.dimension(), pos.above());
            stack.set(ModDataComponents.COLLAR_DATA.get(), new CollarData(home));

            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.mans_best_friend.collar.home_set",
                                pos.getX(), pos.getY(), pos.getZ()),
                        true
                );
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // Right-click on a tamed pet to apply the collar
        if (entity instanceof TamableAnimal pet) {
            // Check if the pet is tamed and owned by the player
            if (!pet.isTame()) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("item.mans_best_friend.collar.not_tamed")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }

            if (!pet.isOwnedBy(player)) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("item.mans_best_friend.collar.not_owner")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }

            // Check if already a Forever Pet
            ForeverPetData petData = pet.getData(ModAttachments.FOREVER_PET_DATA.get());
            if (petData.isForeverPet()) {
                if (!player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("item.mans_best_friend.collar.already_forever")
                                    .withStyle(ChatFormatting.YELLOW),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }

            if (!player.level().isClientSide) {
                // Get the home location from the collar
                CollarData collarData = stack.getOrDefault(ModDataComponents.COLLAR_DATA.get(), CollarData.EMPTY);
                HomeLocation home = collarData.boundHome();

                // Apply Forever Pet status
                ForeverPetData newPetData = new ForeverPetData(true, player.getUUID(), home);
                pet.setData(ModAttachments.FOREVER_PET_DATA.get(), newPetData);

                // Make the pet not sit (so it can wander)
                pet.setOrderedToSit(false);

                // Add to player's roster
                PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
                String petName = pet.hasCustomName() ? pet.getCustomName().getString() : pet.getType().getDescription().getString();
                PlayerPetRosterData.PetEntry entry = new PlayerPetRosterData.PetEntry(
                        pet.getUUID(),
                        petName,
                        BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType()),
                        new HomeLocation(pet.level().dimension(), pet.blockPosition()),
                        home,
                        true
                );
                player.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withAddedPet(entry));

                // Consume the collar
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                // Play success sound
                player.level().playSound(null, pet.getX(), pet.getY(), pet.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 1.0F, 1.0F);

                player.displayClientMessage(
                        Component.translatable("item.mans_best_friend.collar.applied", petName)
                                .withStyle(ChatFormatting.GREEN),
                        true
                );

                MansBestFriend.LOGGER.info("Applied collar to pet {} (UUID: {})", petName, pet.getUUID());
            }

            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CollarData data = stack.get(ModDataComponents.COLLAR_DATA.get());
        if (data != null && data.hasHome()) {
            HomeLocation home = data.boundHome();
            tooltipComponents.add(Component.translatable("item.mans_best_friend.collar.tooltip.home",
                            home.position().getX(), home.position().getY(), home.position().getZ())
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.mans_best_friend.collar.tooltip.dimension",
                            home.dimension().location().toString())
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable("item.mans_best_friend.collar.tooltip.no_home")
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.translatable("item.mans_best_friend.collar.tooltip.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
