package io.github.brdominguez.mansbestfriend.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Collar item that can be applied to tamed pets to make them "Forever Pets".
 * - Right-click tamed pet: Apply collar (handled by ModEvents.onEntityInteract)
 */
public class CollarItem extends Item {

    public CollarItem(Properties properties) {
        super(properties);
    }

    // Note: Entity interaction is handled by ModEvents.onEntityInteract to properly
    // intercept before the wolf's sit/stand behavior

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.mansbestfriend.collar.tooltip.apply_hint"));
    }
}
