package com.example.mansbestfriend.item;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.component.ModDataComponents;
import com.example.mansbestfriend.component.OcarinaColor;
import com.example.mansbestfriend.component.OcarinaData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

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

    // Ocarinas - one for each color
    public static final Map<OcarinaColor, DeferredHolder<Item, OcarinaItem>> OCARINAS = new EnumMap<>(OcarinaColor.class);

    static {
        for (OcarinaColor color : OcarinaColor.values()) {
            OCARINAS.put(color, ITEMS.register("ocarina_" + color.getSerializedName(),
                    () -> new OcarinaItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .component(ModDataComponents.OCARINA_DATA.get(), OcarinaData.unbound(color)),
                            color
                    )));
        }
    }

    /**
     * Gets the ocarina item for a specific color.
     */
    public static OcarinaItem getOcarina(OcarinaColor color) {
        return OCARINAS.get(color).get();
    }

    // Creative Mode Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mans_best_friend"))
                    .icon(() -> new ItemStack(COLLAR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(COLLAR.get());
                        output.accept(ROSTER.get());
                        for (OcarinaColor color : OcarinaColor.values()) {
                            output.accept(OCARINAS.get(color).get());
                        }
                    })
                    .build()
    );
}
