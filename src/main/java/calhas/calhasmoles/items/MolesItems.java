package calhas.calhasmoles.items;

import calhas.calhasmoles.CalhasMoles;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import java.util.function.Function;

/**
 * Create itemKey
 * Instance item
 * Register item in registries
 */
public class MolesItems {
    // Poison effect from eating poisonous food that lasts around 2 seconds with a probability of 80%
    // of proccing
    public static final Consumable poisonFoodComponent = Consumables.defaultFood().onConsume(
            new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 2*20, 1), 0.80f)).build();
    // Worms are a food item that have a chance of poisoning the player upon being eaten. This food
    // item fills half a hunger icon.
    public static final Item WORM = registerItem("worm", Item::new, new Item.Properties().
            food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build(), poisonFoodComponent));

    // Cooked worm, does not proc poison. Fills a full hunger icon
    public static final Item COOKED_WORM = registerItem("cooked_worm", Item::new, new Item.Properties().food(
            new FoodProperties.Builder().nutrition(2).saturationModifier(0.5f).build()));

    // Attributes for custom tab creation
    public static final ResourceKey<CreativeModeTab> customCreativeTabKey =
            ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(CalhasMoles.MOD_ID, "creative_tab"));
    public static final CreativeModeTab customCreativeTab = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(MolesItems.WORM))
            .title(Component.translatable("creativeModeTab.MolesTab"))
            .displayItems((items, output) -> {

                output.accept(MolesItems.WORM);
                output.accept(MolesItems.COOKED_WORM);
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
