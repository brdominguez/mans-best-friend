package io.github.brdominguez.mansbestfriend;

import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.component.ModDataComponents;
import io.github.brdominguez.mansbestfriend.item.ModItems;
import io.github.brdominguez.mansbestfriend.network.ModNetworking;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Man's Best Friend - A Minecraft mod for protecting and managing tamed pets.
 *
 * Features:
 * - Friendship Collar: Makes tamed pets invulnerable "Forever Pets"
 * - Pet Roster: GUI for managing all your Forever Pets
 */
@Mod(MansBestFriend.MODID)
public class MansBestFriend {
    public static final String MODID = "mansbestfriend";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Creative Mode Tab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mansbestfriend"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.COLLAR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.COLLAR.get());
                        output.accept(ModItems.ROSTER.get());
                    }).build());

    public MansBestFriend(IEventBus modEventBus, ModContainer modContainer) {
        // Register data components (must be first, items depend on them)
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);

        // Register items
        ModItems.ITEMS.register(modEventBus);

        // Register creative mode tabs
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register data attachments
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Register network payloads
        modEventBus.addListener((net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) -> ModNetworking.registerPayloads(event));

        LOGGER.info("Man's Best Friend mod initialized!");
    }

    /**
     * Helper method to create an Identifier with the mod's namespace.
     */
    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
