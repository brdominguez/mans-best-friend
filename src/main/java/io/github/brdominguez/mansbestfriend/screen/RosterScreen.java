package io.github.brdominguez.mansbestfriend.screen;

import io.github.brdominguez.mansbestfriend.MansBestFriend;
import io.github.brdominguez.mansbestfriend.network.PetActionPayload;
import io.github.brdominguez.mansbestfriend.network.RequestRosterDataPayload;
import io.github.brdominguez.mansbestfriend.network.SyncRosterDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for managing Forever Pets.
 */
public class RosterScreen extends Screen {
    private static final int ENTRY_HEIGHT = 30;
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;

    private static List<SyncRosterDataPayload.PetInfo> cachedPets = new ArrayList<>();
    private List<SyncRosterDataPayload.PetInfo> pets = new ArrayList<>();
    private int scrollOffset = 0;

    public RosterScreen() {
        super(Component.translatable("gui.mansbestfriend.roster.title"));
    }

    /**
     * Called from packet handler to open the screen.
     */
    public static void openFromPacket() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            ClientPacketDistributor.sendToServer(new RequestRosterDataPayload());
            mc.setScreen(new RosterScreen());
        });
    }

    /**
     * Called from packet handler to update pet data.
     */
    public static void updatePetData(List<SyncRosterDataPayload.PetInfo> newPets) {
        cachedPets = new ArrayList<>(newPets);
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RosterScreen rosterScreen) {
            mc.execute(() -> rosterScreen.refreshPetList());
        }
    }

    private void refreshPetList() {
        this.pets = new ArrayList<>(cachedPets);
        rebuildWidgetsList();
    }

    @Override
    protected void init() {
        super.init();
        this.pets = new ArrayList<>(cachedPets);

        // Request fresh data from server
        ClientPacketDistributor.sendToServer(new RequestRosterDataPayload());

        rebuildWidgetsList();
    }

    private void rebuildWidgetsList() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int startY = 50;
        int listWidth = 300;

        // Close button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.mansbestfriend.roster.close"),
                button -> this.onClose()
        ).bounds(centerX - 50, this.height - 30, 100, 20).build());

        // Pet entries
        int visibleEntries = Math.min(pets.size(), (this.height - 100) / ENTRY_HEIGHT);
        for (int i = 0; i < visibleEntries; i++) {
            int index = i + scrollOffset;
            if (index >= pets.size()) break;

            SyncRosterDataPayload.PetInfo pet = pets.get(index);
            int entryY = startY + i * ENTRY_HEIGHT;

            // Summon button
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.mansbestfriend.roster.summon"),
                    button -> summonPet(pet)
            ).bounds(centerX + listWidth / 2 - BUTTON_WIDTH * 2 - 5, entryY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

            // Send Home button
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.mansbestfriend.roster.send_home"),
                    button -> sendPetHome(pet)
            ).bounds(centerX + listWidth / 2 - BUTTON_WIDTH, entryY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 50;
        int listWidth = 300;

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, centerX, 20, 0xFFFFFF);

        // Pet count
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.mansbestfriend.roster.count", pets.size()),
                centerX, 35, 0xAAAAAA);

        // Pet entries
        int visibleEntries = Math.min(pets.size(), (this.height - 100) / ENTRY_HEIGHT);
        for (int i = 0; i < visibleEntries; i++) {
            int index = i + scrollOffset;
            if (index >= pets.size()) break;

            SyncRosterDataPayload.PetInfo pet = pets.get(index);
            int entryY = startY + i * ENTRY_HEIGHT;

            // Background
            guiGraphics.fill(
                    centerX - listWidth / 2, entryY - 2,
                    centerX + listWidth / 2, entryY + ENTRY_HEIGHT - 4,
                    0x80000000
            );

            // Pet name and type
            String displayName = pet.name();
            if (!pet.isLoaded()) {
                displayName += " (?)";
            }
            guiGraphics.drawString(this.font, displayName,
                    centerX - listWidth / 2 + 5, entryY + 2, 0xFFFFFF);
            guiGraphics.drawString(this.font, pet.type(),
                    centerX - listWidth / 2 + 5, entryY + 12, 0xAAAAAA);
        }

        // No pets message
        if (pets.isEmpty()) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.mansbestfriend.roster.no_pets"),
                    centerX, this.height / 2, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleEntries = (this.height - 100) / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, pets.size() - visibleEntries);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
        rebuildWidgetsList();
        return true;
    }

    private void summonPet(SyncRosterDataPayload.PetInfo pet) {
        ClientPacketDistributor.sendToServer(new PetActionPayload(pet.uuid(), PetActionPayload.Action.SUMMON));
    }

    private void sendPetHome(SyncRosterDataPayload.PetInfo pet) {
        ClientPacketDistributor.sendToServer(new PetActionPayload(pet.uuid(), PetActionPayload.Action.SEND_HOME));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
