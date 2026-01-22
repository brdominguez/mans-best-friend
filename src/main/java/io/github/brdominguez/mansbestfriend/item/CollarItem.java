package io.github.brdominguez.mansbestfriend.item;

import io.github.brdominguez.mansbestfriend.component.CollarData;
import io.github.brdominguez.mansbestfriend.component.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.function.Consumer;

/**
 * Collar item that can be applied to tamed pets to make them "Forever Pets".
 * - Sneak+Right-click block: Set home location on the collar
 * - Right-click tamed pet: Apply collar (handled by ModEvents.onEntityInteract)
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
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
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
                // Play a confirmation sound
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // Note: Entity interaction is handled by ModEvents.onEntityInteract to properly
    // intercept before the wolf's sit/stand behavior

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);

        CollarData data = stack.getOrDefault(ModDataComponents.COLLAR_DATA.get(), CollarData.EMPTY);
        if (data.hasHomePos()) {
            GlobalPos pos = data.homePos().get();
            // Format dimension name nicely (e.g., "ResourceKey[minecraft:dimension / minecraft:overworld]" -> "Overworld")
            String rawDimension = pos.dimension().toString();
            int lastSlash = rawDimension.lastIndexOf('/');
            int lastBracket = rawDimension.lastIndexOf(']');
            String dimPart = lastSlash >= 0 && lastBracket > lastSlash
                    ? rawDimension.substring(lastSlash + 1, lastBracket).trim()
                    : rawDimension;
            int colon = dimPart.lastIndexOf(':');
            String baseName = colon >= 0 ? dimPart.substring(colon + 1) : dimPart;
            String dimensionName = baseName.substring(0, 1).toUpperCase() + baseName.substring(1).replace("_", " ");
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.home",
                    pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()));
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.dimension", dimensionName));
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.apply_hint"));
        } else {
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.no_home"));
            tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.apply_hint"));
        }
    }
}
