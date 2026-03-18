package com.reimonsworkshop.namcm.item;

import com.reimonsworkshop.namcm.NAMCMMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class Items {

    public static void initialize() {
        registerItem("chunk_probe", ChunkProbeItem::new, new Item.Properties());
        registerItem("chunk_depleter", ChunkDepleterItem::new, new Item.Properties());
    }

    public static <T extends Item> void registerItem(
            final String name,
            final Function<Item.Properties, T> itemFactory,
            final Item.Properties settings
    ) {
        final ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(NAMCMMod.MOD_ID, name)
        );

        final T item = itemFactory.apply(settings.setId(itemKey));

        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }
}
