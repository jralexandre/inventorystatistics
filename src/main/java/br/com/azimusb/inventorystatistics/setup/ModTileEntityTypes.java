package br.com.azimusb.inventorystatistics.setup;

import br.com.azimusb.inventorystatistics.tileentity.ItemSpeedAltarTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModTileEntityTypes {

    public static RegistryObject<TileEntityType<ItemSpeedAltarTileEntity>> ITEM_SPEED_ALTAR = register("item_speed_altar", ItemSpeedAltarTileEntity::new, ModBlocks.ITEM_SPEED_ALTAR_BLOCK);

    static void register() {
    }

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factory, Block block) {
        return Registration.TILE_ENTITIES.register(name, () -> {
            //noinspection ConstantConditions - null in build
            return TileEntityType.Builder.create(factory, block).build(null);
        });
    }
}
