package com.example.mansbestfriend.component;

import com.example.mansbestfriend.MansBestFriend;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registration for data components (item data).
 */
public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MansBestFriend.MOD_ID);

    /**
     * Component for Collar items storing the bound home location.
     */
    public static final Supplier<DataComponentType<CollarData>> COLLAR_DATA =
            COMPONENTS.registerComponentType("collar_data", builder ->
                    builder.persistent(CollarData.CODEC)
                            .networkSynchronized(CollarData.STREAM_CODEC)
            );

    /**
     * Component for Ocarina items storing the bound pet UUID and color.
     */
    public static final Supplier<DataComponentType<OcarinaData>> OCARINA_DATA =
            COMPONENTS.registerComponentType("ocarina_data", builder ->
                    builder.persistent(OcarinaData.CODEC)
                            .networkSynchronized(OcarinaData.STREAM_CODEC)
            );
}
