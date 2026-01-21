package io.github.brdominguez.mansbestfriend.item;

import io.github.brdominguez.mansbestfriend.attachment.ModAttachments;
import io.github.brdominguez.mansbestfriend.attachment.PlayerPetRosterData;
import io.github.brdominguez.mansbestfriend.network.OpenRosterScreenPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Roster item for managing Forever Pets.
 * - Right-click (in air): Open pet management GUI
 * - Sneak+Right-click block: Set default home for all pets
 */
public class RosterItem extends Item {

    public RosterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Sneak + right-click block to set default home
        if (player.isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();

            if (!level.isClientSide()) {
                GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
                PlayerPetRosterData rosterData = player.getData(ModAttachments.PLAYER_PET_ROSTER_DATA.get());
                player.setData(ModAttachments.PLAYER_PET_ROSTER_DATA.get(), rosterData.withDefaultHomePos(globalPos));

                player.displayClientMessage(
                        Component.translatable("item.mansbestfriend.roster.default_home_set",
                                pos.getX(), pos.getY(), pos.getZ()),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                // Send packet to open GUI on client
                PacketDistributor.sendToPlayer(serverPlayer, new OpenRosterScreenPayload());
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
