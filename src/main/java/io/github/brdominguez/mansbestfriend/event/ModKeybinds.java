package io.github.brdominguez.mansbestfriend.event;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Keybind registration for the mod.
 */
@EventBusSubscriber(modid = MansBestFriend.MODID, value = Dist.CLIENT)
public class ModKeybinds {

    // Custom category for the mod
    public static final KeyMapping.Category MOD_CATEGORY = new KeyMapping.Category(
            MansBestFriend.location("main")
    );

    public static final KeyMapping OPEN_ROSTER_KEY = new KeyMapping(
            "key.mansbestfriend.open_roster",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            MOD_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(MOD_CATEGORY);
        event.register(OPEN_ROSTER_KEY);
        MansBestFriend.LOGGER.info("Registered keybindings");
    }
}
