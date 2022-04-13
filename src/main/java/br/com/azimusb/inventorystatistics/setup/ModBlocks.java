package br.com.azimusb.inventorystatistics.setup;

import br.com.azimusb.inventorystatistics.block.ItemSpeedAltarBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final ItemSpeedAltarBlock ITEM_SPEED_ALTAR_BLOCK = new ItemSpeedAltarBlock(
            AbstractBlock.Properties.create(Material.IRON)
                    .hardnessAndResistance(1.0F)
                    .sound(SoundType.METAL));

    public static final RegistryObject<ItemSpeedAltarBlock> ITEM_SPEED_ALTAR = register("item_speed_altar", () ->
            ITEM_SPEED_ALTAR_BLOCK);

    static void register() {}

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block) {
        return Registration.BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
        return ret;
    }
}