package com.example.mansbestfriend.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;

/**
 * Color variants for the Ocarina, matching the 17 terracotta colors.
 * Includes 16 dyed colors plus natural (uncolored) terracotta.
 */
public enum OcarinaColor implements StringRepresentable {
    NATURAL("natural", null, 0xB55F38, Blocks.TERRACOTTA),
    WHITE("white", DyeColor.WHITE, 0xD1B1A1, Blocks.WHITE_TERRACOTTA),
    ORANGE("orange", DyeColor.ORANGE, 0xA25325, Blocks.ORANGE_TERRACOTTA),
    MAGENTA("magenta", DyeColor.MAGENTA, 0x95586C, Blocks.MAGENTA_TERRACOTTA),
    LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE, 0x706C8A, Blocks.LIGHT_BLUE_TERRACOTTA),
    YELLOW("yellow", DyeColor.YELLOW, 0xB98423, Blocks.YELLOW_TERRACOTTA),
    LIME("lime", DyeColor.LIME, 0x677535, Blocks.LIME_TERRACOTTA),
    PINK("pink", DyeColor.PINK, 0xA14E4E, Blocks.PINK_TERRACOTTA),
    GRAY("gray", DyeColor.GRAY, 0x3A2923, Blocks.GRAY_TERRACOTTA),
    LIGHT_GRAY("light_gray", DyeColor.LIGHT_GRAY, 0x876B62, Blocks.LIGHT_GRAY_TERRACOTTA),
    CYAN("cyan", DyeColor.CYAN, 0x565B5B, Blocks.CYAN_TERRACOTTA),
    PURPLE("purple", DyeColor.PURPLE, 0x764556, Blocks.PURPLE_TERRACOTTA),
    BLUE("blue", DyeColor.BLUE, 0x4A3B5B, Blocks.BLUE_TERRACOTTA),
    BROWN("brown", DyeColor.BROWN, 0x4D3224, Blocks.BROWN_TERRACOTTA),
    GREEN("green", DyeColor.GREEN, 0x4C532A, Blocks.GREEN_TERRACOTTA),
    RED("red", DyeColor.RED, 0x8E3C2E, Blocks.RED_TERRACOTTA),
    BLACK("black", DyeColor.BLACK, 0x251610, Blocks.BLACK_TERRACOTTA);

    public static final Codec<OcarinaColor> CODEC = StringRepresentable.fromEnum(OcarinaColor::values);
    public static final StreamCodec<ByteBuf, OcarinaColor> STREAM_CODEC = ByteBufCodecs.idMapper(
            i -> OcarinaColor.values()[i],
            OcarinaColor::ordinal
    );

    private final String name;
    @Nullable
    private final DyeColor dyeColor;
    private final int color;
    private final ItemLike terracottaBlock;

    OcarinaColor(String name, @Nullable DyeColor dyeColor, int color, ItemLike terracottaBlock) {
        this.name = name;
        this.dyeColor = dyeColor;
        this.color = color;
        this.terracottaBlock = terracottaBlock;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Nullable
    public DyeColor getDyeColor() {
        return dyeColor;
    }

    /**
     * Returns the RGB color value for this ocarina color.
     */
    public int getColor() {
        return color;
    }

    /**
     * Returns the terracotta block used in the crafting recipe.
     */
    public ItemLike getTerracottaBlock() {
        return terracottaBlock;
    }

    /**
     * Gets an OcarinaColor by its name.
     */
    @Nullable
    public static OcarinaColor byName(String name) {
        for (OcarinaColor color : values()) {
            if (color.name.equals(name)) {
                return color;
            }
        }
        return null;
    }
}
