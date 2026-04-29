package calhas.calhasmoles.items;

import calhas.calhasmoles.CalhasMoles;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


import java.util.function.Function;

/**
 * Create itemKey
 * Instance item
 * Register item in registries
 */
public class MolesItems {

    public static final Item WORM = registerItem("worm", Item::new, new Item.Properties());
    public static final ResourceKey<CreativeModeTab> customCreativeTabKey =
            ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(CalhasMoles.MOD_ID, "creative_tab"));

    public static final CreativeModeTab customCreativeTab = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(MolesItems.WORM))
            .title(Component.translatable("creativeModeTab.MolesTab"))
            .displayItems((items, output) -> {

                output.accept(MolesItems.WORM);
            }).build();

    public static <T extends Item> T registerItem(String itemName, Function<Item.Properties, T> itemFactory, Item.Properties settings){

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CalhasMoles.MOD_ID, itemName));

        T item = itemFactory.apply(settings.setId(itemKey));

        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }


    // Mock initialize method
    public static void initialize(){
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, customCreativeTabKey, customCreativeTab);

    }
}
