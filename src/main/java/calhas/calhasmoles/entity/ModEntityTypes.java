package calhas.calhasmoles.entity;

import calhas.calhasmoles.CalhasMoles;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {
    public static final EntityType<MoleEntity> MOLE = register(
            "mole",
            EntityType.Builder.of(MoleEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.5f)

    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(CalhasMoles.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        CalhasMoles.LOGGER.info("Registering EntityTypes for " + CalhasMoles.MOD_ID);
    }

    private ModEntityTypes() {}

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(MOLE, MoleEntity.createCubeAttributes().build());
    }

}
