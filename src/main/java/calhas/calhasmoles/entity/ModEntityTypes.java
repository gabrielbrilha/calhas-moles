package calhas.calhasmoles.entity;

import calhas.calhasmoles.CalhasMoles;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModEntityTypes {

    private ModEntityTypes() {}

    public static final EntityType<MoleEntity> MOLE = register(
            "mole",
            FabricEntityType.Builder.createMob(
                            MoleEntity::new,
                            MobCategory.CREATURE,
                            mob -> mob.defaultAttributes(MoleEntity::createMoleAttributes)
                    )
                    .sized(0.4f, 0.5f)
    );

    // Only living entities use this helper, so attributes can be registered safely.
    private static <T extends LivingEntity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(CalhasMoles.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        SpawnPlacements.register(
                MOLE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MoleEntity::canSpawn
        );
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE, MOLE, 12, 1, 3);
    }

    public static void registerAttributes() {
        // Attributes are already provided in the MOLE builder
    }

}
