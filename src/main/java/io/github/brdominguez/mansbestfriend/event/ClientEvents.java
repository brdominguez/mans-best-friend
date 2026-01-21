package io.github.brdominguez.mansbestfriend.event;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.network.RequestRosterDataPayload;
import io.github.brdominguez.mansbestfriend.screen.RosterScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Client-side event handlers for keybindings.
 */
@EventBusSubscriber(modid = MansBestFriend.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        if (ModKeybinds.OPEN_ROSTER_KEY.consumeClick()) {
            // Request roster data from server
            ClientPacketDistributor.sendToServer(new RequestRosterDataPayload());
            // Open the screen
            mc.setScreen(new RosterScreen());
        }
    }
}
