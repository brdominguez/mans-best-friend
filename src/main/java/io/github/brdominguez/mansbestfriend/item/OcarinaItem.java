package io.github.brdominguez.mansbestfriend.item;

import io.github.brdominguez.mansbestfriend.attachment.ForeverPetData;
import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.component.ModDataComponents;
import io.github.brdominguez.mansbestfriend.component.OcarinaData;
import io.github.brdominguez.mansbestfriend.util.TeleportHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ocarina item for summoning and sending home Forever Pets.
 * - Right-click Forever Pet: Bind the pet to this ocarina (handled by ModEvents)
 * - Right-click (in air): Summon bound pet to player
 * - Sneak+Right-click (in air): Send bound pet home
 */
public class OcarinaItem extends Item {

    public OcarinaItem(Properties properties) {
        super(properties);
    }

    // Note: Entity interaction (binding) is handled by ModEvents.onOcarinaEntityInteract
    // to properly intercept before the wolf's sit/stand behavior

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        OcarinaData data = stack.getOrDefault(ModDataComponents.OCARINA_DATA.get(), OcarinaData.EMPTY);

        if (!data.hasBoundPet()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.no_pet_bound"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        UUID petUuid = data.boundPetUuid().get();

        // Find the pet across all dimensions
        Entity pet = TeleportHelper.findEntityAcrossDimensions(serverLevel.getServer(), petUuid);

        if (pet == null) {
            player.displayClientMessage(
                    Component.translatable("item.mansbestfriend.ocarina.pet_not_found"),
                    true
            );
            return InteractionResult.FAIL;
        }

        if (!(pet instanceof TamableAnimal tamable)) {
            player.displayClientMessage(
                    Component.translatable("item.mansbestfriend.ocarina.invalid_pet"),
                    true
            );
            return InteractionResult.FAIL;
        }

        ForeverPetData petData = pet.getData(ModAttachments.FOREVER_PET_DATA.get());

        if (player.isShiftKeyDown()) {
            // Send pet home
            Optional<GlobalPos> homePos = petData.homePos();
            if (homePos.isEmpty()) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.no_home"),
                        true
                );
                return InteractionResult.FAIL;
            }

            GlobalPos home = homePos.get();
            boolean success = TeleportHelper.teleportEntity(tamable, serverLevel.getServer(), home);

            if (success) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.sent_home",
                                pet.getDisplayName()),
                        true
                );
                // Add cooldown
                player.getCooldowns().addCooldown(stack, 20);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.teleport_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
        } else {
            // Summon pet to player
            GlobalPos playerPos = GlobalPos.of(serverLevel.dimension(), player.blockPosition());
            boolean success = TeleportHelper.teleportEntity(tamable, serverLevel.getServer(), playerPos);

            if (success) {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.summoned",
                                pet.getDisplayName()),
                        true
                );
                // Add cooldown
                player.getCooldowns().addCooldown(stack, 20);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.ocarina.teleport_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);

        OcarinaData data = stack.getOrDefault(ModDataComponents.OCARINA_DATA.get(), OcarinaData.EMPTY);
        if (data.hasBoundPet()) {
            String petName = data.boundPetName().orElse("Unknown");
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.ocarina.tooltip.bound_to", petName));
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.ocarina.tooltip.summon_hint"));
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.ocarina.tooltip.home_hint"));
        } else {
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.ocarina.tooltip.unbound"));
        }
    }
}
