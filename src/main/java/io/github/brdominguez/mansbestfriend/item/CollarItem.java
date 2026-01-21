package io.github.brdominguez.mansbestfriend.item;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.attachment.PlayerPetRosterData;
import io.github.brdominguez.mansbestfriend.component.CollarData;
import io.github.brdominguez.mansbestfriend.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Collar item that can be applied to tamed pets to make them "Forever Pets".
 * - Sneak+Right-click block: Set home location on the collar
 * - Right-click tamed pet: Apply collar, making it a Forever Pet
 */
public class CollarItem extends Item {

    public CollarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Sneak + right-click block to set home location
        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();
            ItemStack stack = context.getItemInHand();

            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
            CollarData newData = new CollarData(java.util.Optional.of(globalPos));
            stack.set(ModDataComponents.COLLAR_DATA.get(), newData);

            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.home_set",
                                pos.getX(), pos.getY(), pos.getZ()),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TamableAnimal tamable)) {
            return InteractionResult.PASS;
        }

        // Must be tamed and owned by the player
        if (!tamable.isTame() || !tamable.isOwnedBy(player)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.not_your_pet"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        // Check if already a Forever Pet
        ForeverPetData existingData = target.getData(ModAttachments.FOREVER_PET_DATA.get());
        if (existingData.isForeverPet()) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.collar.already_forever"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            // Get home position from collar or use current position
            CollarData collarData = stack.getOrDefault(ModDataComponents.COLLAR_DATA.get(), CollarData.EMPTY);
            GlobalPos homePos = collarData.homePos()
                    .orElse(GlobalPos.of(target.level().dimension(), target.blockPosition()));

            // Create Forever Pet data
            ForeverPetData petData = new ForeverPetData(
                    true,
                    java.util.Optional.of(homePos),
                    java.util.Optional.of(player.getUUID()),
                    target.hasCustomName() ? java.util.Optional.of(target.getCustomName().getString()) : java.util.Optional.empty()
            );

            // Apply to pet
            target.setData(ModAttachments.FOREVER_PET_DATA.get(), petData);

            // Make the pet stand up (not sitting)
            tamable.setOrderedToSit(false);

            // Add to player's roster
            PlayerPetRosterData rosterData = player.getData(ModAttachments.PLAYER_PET_ROSTER_DATA.get());
            player.setData(ModAttachments.PLAYER_PET_ROSTER_DATA.get(), rosterData.addPet(target.getUUID()));

            // Consume the collar
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.displayClientMessage(
                    Component.translatable("item.mansbestfriend.collar.applied",
                            target.getDisplayName()),
                    true
            );

            MansBestFriend.LOGGER.info("Made {} a Forever Pet for player {}",
                    target.getDisplayName().getString(), player.getName().getString());
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);

        CollarData data = stack.getOrDefault(ModDataComponents.COLLAR_DATA.get(), CollarData.EMPTY);
        if (data.hasHomePos()) {
            GlobalPos pos = data.homePos().get();
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.home",
                    pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()));
        } else {
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.no_home"));
        }
    }
}
