package br.com.azimusb.inventorystatistics.setup;

import appeng.api.util.AEColor;
import appeng.core.Api;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.PartModel;
import br.com.azimusb.inventorystatistics.parts.ItemSpeedMonitorPart;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;

public class ModItems {
    public static final RegistryObject<Item> ITEM_SPEED_MONITOR = Registration.ITEMS.register("item_speed_monitor", () ->
            new ColoredPartItem<>(new Item.Properties().group(ItemGroup.MISC), ItemSpeedMonitorPart::new, AEColor.PURPLE));

    static void register() {
    }

    @SubscribeEvent
    public static void onHandleColors(ColorHandlerEvent.Item event){
        event.getItemColors().register((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), ITEM_SPEED_MONITOR::get);
    }
}
