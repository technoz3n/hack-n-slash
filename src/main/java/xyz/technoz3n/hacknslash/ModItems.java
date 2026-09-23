package xyz.technoz3n.hacknslash;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.item.v1.FabricItem;

import java.util.function.Function;

public class ModItems {

    public static final Item SCYTHE = register(
        "scythe",
        properties -> new ScytheItem(ToolMaterials.NETHERITE, properties),
        new Item.Properties().stacksTo(1)
    );

    private static Item register(String id, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("hack-n-slash", id));
        Item item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(SCYTHE);
        });
    }
}