package com.example.mansbestfriend.item;

import com.example.mansbestfriend.attachment.ModAttachments;
import com.example.mansbestfriend.attachment.PlayerPetRosterData;
import com.example.mansbestfriend.network.ModPayloads;
import com.example.mansbestfriend.network.payload.SyncPetRosterPayload;
import com.example.mansbestfriend.screen.RosterScreen;
import com.example.mansbestfriend.util.HomeLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The Pet Roster item.
 * - Right-click to open the roster GUI
 * - Sneak+Right-click on a block to set the default home location
 */
public class RosterItem extends Item {

    public RosterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Sneak+Right-click on block to set default home
        if (player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                HomeLocation home = new HomeLocation(level.dimension(), pos.above());
                PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
                player.setData(ModAttachments.PLAYER_PET_ROSTER.get(), roster.withDefaultHome(home));

                player.displayClientMessage(
                        Component.translatable("item.mans_best_friend.roster.default_home_set",
                                pos.getX(), pos.getY(), pos.getZ())
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Sneak+use in air does nothing (need to click on a block)
            return InteractionResultHolder.pass(stack);
        }

        // Open the roster GUI
        if (level.isClientSide) {
            openRosterScreen(player);
        } else if (player instanceof ServerPlayer serverPlayer) {
            // Sync roster data to client
            syncRosterToClient(serverPlayer);
        }

        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private void openRosterScreen(Player player) {
        // This is called on client side
        Minecraft.getInstance().setScreen(new RosterScreen());
    }

    private void syncRosterToClient(ServerPlayer player) {
        PlayerPetRosterData roster = player.getData(ModAttachments.PLAYER_PET_ROSTER.get());
        PacketDistributor.sendToPlayer(player, new SyncPetRosterPayload(roster.pets(), roster.defaultHome()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.mans_best_friend.roster.tooltip.hint_open")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.mans_best_friend.roster.tooltip.hint_home")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
