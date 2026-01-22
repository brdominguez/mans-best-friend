package io.github.brdominguez.mansbestfriend.component;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MansBestFriend.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CollarData>> COLLAR_DATA =
            DATA_COMPONENT_TYPES.register("collar_data", () -> DataComponentType.<CollarData>builder()
                    .persistent(CollarData.CODEC)
                    .networkSynchronized(CollarData.STREAM_CODEC)
                    .build());
}
