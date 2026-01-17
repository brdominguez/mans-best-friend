package com.example.mansbestfriend;

import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.component.ModDataComponents;
import com.example.mansbestfriend.event.ModGameEvents;
import com.example.mansbestfriend.item.ModItems;
import com.example.mansbestfriend.network.ModPayloads;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MansBestFriend.MOD_ID)
public class MansBestFriend {
    public static final String MOD_ID = "mans_best_friend";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MansBestFriend(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Man's Best Friend mod");

        // Register deferred registers to the mod event bus
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);

        // Register payload handlers
        modEventBus.addListener(ModPayloads::register);

        // Register game events
        NeoForge.EVENT_BUS.register(ModGameEvents.class);
    }
}
