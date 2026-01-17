package com.example.mansbestfriend.event;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.network.payload.OpenRosterPayload;
import com.example.mansbestfriend.screen.RosterScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side event handlers for keybindings and GUI.
 */
@EventBusSubscriber(modid = MansBestFriend.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    public static final KeyMapping OPEN_ROSTER_KEY = new KeyMapping(
            "key.mans_best_friend.open_roster",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.mans_best_friend"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ROSTER_KEY);
        MansBestFriend.LOGGER.info("Registered keybindings");
    }

    /**
     * Separate class for GAME bus events (not MOD bus).
     */
    @EventBusSubscriber(modid = MansBestFriend.MOD_ID, value = Dist.CLIENT)
    public static class GameEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            // Only process keybinds when in-game
            if (mc.player == null || mc.screen != null) {
                return;
            }

            // Check for roster keybind
            while (OPEN_ROSTER_KEY.consumeClick()) {
                // Request roster data from server
                PacketDistributor.sendToServer(OpenRosterPayload.INSTANCE);
                // Open the screen
                mc.setScreen(new RosterScreen());
            }
        }
    }
}
