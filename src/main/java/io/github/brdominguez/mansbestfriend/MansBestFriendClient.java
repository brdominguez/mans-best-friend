package io.github.brdominguez.mansbestfriend;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-side initialization for Man's Best Friend mod.
 */
@Mod(value = MansBestFriend.MODID, dist = Dist.CLIENT)
public class MansBestFriendClient {
    public MansBestFriendClient(ModContainer container) {
        // Register config screen
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        MansBestFriend.LOGGER.info("Man's Best Friend client initialized!");
    }
}
