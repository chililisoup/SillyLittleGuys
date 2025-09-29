package dev.chililisoup.sillylittleguys.reg.fabric;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.fabric.SillyLittleGuysFabric;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

public class ModEntitiesImpl {
    public static <T extends Mob> Supplier<EntityType<T>> register(
            EntityType.Builder<T> builder,
            String name,
            Supplier<AttributeSupplier.Builder> defaultAttributes,
            SpawnPlacementType spawnPlacementType,
            Heightmap.Types heightmapType,
            SpawnPlacements.SpawnPredicate<T> spawnPredicate,
            int backgroundColor,
            int highlightColor
    ) {
        EntityType<T> entityType = Registry.register(BuiltInRegistries.ENTITY_TYPE, SillyLittleGuys.loc(name), builder.build());

        FabricDefaultAttributeRegistry.register(entityType, defaultAttributes.get());

        SillyLittleGuysFabric.SPAWN_EGGS.add(
                Registry.register(BuiltInRegistries.ITEM, SillyLittleGuys.loc(name + "_spawn_egg"), new SpawnEggItem(
                        entityType, backgroundColor, highlightColor, new Item.Properties()
                ))
        );

        SpawnPlacements.register(entityType, spawnPlacementType, heightmapType, spawnPredicate);

        return () -> entityType;
    }
}
