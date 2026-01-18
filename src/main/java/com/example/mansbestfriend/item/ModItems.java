package com.example.mansbestfriend.item;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.component.ModDataComponents;
import com.example.mansbestfriend.component.OcarinaData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for all mod items.
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MansBestFriend.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MansBestFriend.MOD_ID);

    // Collar
    public static final DeferredHolder<Item, CollarItem> COLLAR = ITEMS.register("collar",
            () -> new CollarItem(new Item.Properties().stacksTo(16)));

    // Roster
    public static final DeferredHolder<Item, RosterItem> ROSTER = ITEMS.register("roster",
            () -> new RosterItem(new Item.Properties().stacksTo(1)));

    // Ocarina
    public static final DeferredHolder<Item, OcarinaItem> OCARINA = ITEMS.register("ocarina",
            () -> new OcarinaItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .component(ModDataComponents.OCARINA_DATA.get(), OcarinaData.unbound())
            ));

    // Creative Mode Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mans_best_friend"))
                    .icon(() -> new ItemStack(COLLAR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(COLLAR.get());
                        output.accept(ROSTER.get());
                        output.accept(OCARINA.get());
                    })
                    .build()
    );
}
