package com.example.mansbestfriend.screen;

import com.example.mansbestfriend.MansBestFriend;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.network.payload.SendPetHomePayload;
import com.example.mansbestfriend.network.payload.SummonPetPayload;
import com.example.mansbestfriend.util.HomeLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Pet Roster GUI screen.
 * Displays all Forever Pets and allows summoning/sending home.
 */
public class RosterScreen extends Screen {

    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 200;
    private static final int ENTRY_HEIGHT = 24;
    private static final int MAX_VISIBLE_ENTRIES = 6;

    // Client-side roster data (synced from server)
    private static List<PlayerPetRosterData.PetEntry> clientPets = new ArrayList<>();
    @Nullable
    private static HomeLocation clientDefaultHome = null;

    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;

    public RosterScreen() {
        super(Component.translatable("screen.mans_best_friend.roster.title"));
    }

    /**
     * Called by the network handler to update client-side roster data.
     */
    public static void setClientRosterData(List<PlayerPetRosterData.PetEntry> pets, @Nullable HomeLocation defaultHome) {
        clientPets = new ArrayList<>(pets);
        clientDefaultHome = defaultHome;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WINDOW_WIDTH) / 2;
        this.topPos = (this.height - WINDOW_HEIGHT) / 2;

        rebuildButtons();
    }

    private void rebuildButtons() {
        this.clearWidgets();

        int listTop = topPos + 30;
        int visibleCount = Math.min(MAX_VISIBLE_ENTRIES, clientPets.size() - scrollOffset);

        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= clientPets.size()) break;

            PlayerPetRosterData.PetEntry pet = clientPets.get(index);
            int entryY = listTop + (i * ENTRY_HEIGHT);

            // Summon button
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.mans_best_friend.roster.summon"),
                            btn -> summonPet(pet)
                    )
                    .pos(leftPos + WINDOW_WIDTH - 90, entryY)
                    .size(40, 20)
                    .build());

            // Send Home button
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.mans_best_friend.roster.home"),
                            btn -> sendPetHome(pet)
                    )
                    .pos(leftPos + WINDOW_WIDTH - 45, entryY)
                    .size(40, 20)
                    .build());
        }

        // Scroll buttons if needed
        if (clientPets.size() > MAX_VISIBLE_ENTRIES) {
            // Scroll up
            this.addRenderableWidget(Button.builder(
                            Component.literal("▲"),
                            btn -> scroll(-1)
                    )
                    .pos(leftPos + WINDOW_WIDTH - 20, topPos + 5)
                    .size(15, 15)
                    .build());

            // Scroll down
            this.addRenderableWidget(Button.builder(
                            Component.literal("▼"),
                            btn -> scroll(1)
                    )
                    .pos(leftPos + WINDOW_WIDTH - 20, topPos + WINDOW_HEIGHT - 25)
                    .size(15, 15)
                    .build());
        }

        // Close button
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        btn -> this.onClose()
                )
                .pos(leftPos + (WINDOW_WIDTH - 60) / 2, topPos + WINDOW_HEIGHT - 25)
                .size(60, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Background
        guiGraphics.fill(leftPos, topPos, leftPos + WINDOW_WIDTH, topPos + WINDOW_HEIGHT, 0xCC000000);
        guiGraphics.fill(leftPos + 2, topPos + 2, leftPos + WINDOW_WIDTH - 2, topPos + WINDOW_HEIGHT - 2, 0xCC333333);

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, leftPos + WINDOW_WIDTH / 2, topPos + 8, 0xFFFFFF);

        // Pet count
        String countText = "Pets: " + clientPets.size();
        guiGraphics.drawString(this.font, countText, leftPos + 8, topPos + 8, 0xAAAAAA);

        // Pet entries
        int listTop = topPos + 30;
        int visibleCount = Math.min(MAX_VISIBLE_ENTRIES, clientPets.size() - scrollOffset);

        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= clientPets.size()) break;

            PlayerPetRosterData.PetEntry pet = clientPets.get(index);
            int entryY = listTop + (i * ENTRY_HEIGHT);

            // Entry background
            int bgColor = (index % 2 == 0) ? 0x40444444 : 0x40555555;
            guiGraphics.fill(leftPos + 5, entryY - 2, leftPos + WINDOW_WIDTH - 95, entryY + ENTRY_HEIGHT - 4, bgColor);

            // Pet name
            String displayName = pet.petName();
            if (displayName.length() > 15) {
                displayName = displayName.substring(0, 12) + "...";
            }
            int nameColor = pet.isAlive() ? 0xFFFFFF : 0xFF5555;
            guiGraphics.drawString(this.font, displayName, leftPos + 10, entryY + 2, nameColor);

            // Location info
            if (pet.lastKnownLocation() != null) {
                HomeLocation loc = pet.lastKnownLocation();
                String locText = String.format("%d, %d, %d", loc.position().getX(), loc.position().getY(), loc.position().getZ());
                guiGraphics.drawString(this.font, locText, leftPos + 10, entryY + 12, 0x888888);
            }

            // Status indicator
            String status = pet.isAlive() ? "●" : "✗";
            int statusColor = pet.isAlive() ? 0x55FF55 : 0xFF5555;
            guiGraphics.drawString(this.font, status, leftPos + WINDOW_WIDTH - 100, entryY + 6, statusColor);
        }

        // Default home info at bottom
        if (clientDefaultHome != null) {
            String homeText = String.format("Default Home: %d, %d, %d",
                    clientDefaultHome.position().getX(),
                    clientDefaultHome.position().getY(),
                    clientDefaultHome.position().getZ());
            guiGraphics.drawString(this.font, homeText, leftPos + 8, topPos + WINDOW_HEIGHT - 40, 0x88AAFF);
        }

        // Empty list message
        if (clientPets.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("screen.mans_best_friend.roster.empty"),
                    leftPos + WINDOW_WIDTH / 2, topPos + 80, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            scroll(-1);
        } else if (scrollY < 0) {
            scroll(1);
        }
        return true;
    }

    private void scroll(int direction) {
        int maxScroll = Math.max(0, clientPets.size() - MAX_VISIBLE_ENTRIES);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + direction));
        rebuildButtons();
    }

    private void summonPet(PlayerPetRosterData.PetEntry pet) {
        PacketDistributor.sendToServer(new SummonPetPayload(pet.petUUID()));
        MansBestFriend.LOGGER.debug("Sent summon request for pet: {}", pet.petName());
    }

    private void sendPetHome(PlayerPetRosterData.PetEntry pet) {
        PacketDistributor.sendToServer(new SendPetHomePayload(pet.petUUID()));
        MansBestFriend.LOGGER.debug("Sent send-home request for pet: {}", pet.petName());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
