package io.github.brdominguez.mansbestfriend.item;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.component.CollarData;
import io.github.brdominguez.mansbestfriend.component.ModDataComponents;
import io.github.brdominguez.mansbestfriend.component.OcarinaData;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MansBestFriend.MODID);

    public static final DeferredItem<CollarItem> COLLAR = ITEMS.registerItem("collar",
            props -> new CollarItem(props
                    .stacksTo(16)
                    .component(ModDataComponents.COLLAR_DATA.get(), CollarData.EMPTY)));

    public static final DeferredItem<OcarinaItem> OCARINA = ITEMS.registerItem("ocarina",
            props -> new OcarinaItem(props
                    .stacksTo(1)
                    .component(ModDataComponents.OCARINA_DATA.get(), OcarinaData.EMPTY)));

    public static final DeferredItem<RosterItem> ROSTER = ITEMS.registerItem("roster",
            props -> new RosterItem(props
                    .stacksTo(1)));
}
