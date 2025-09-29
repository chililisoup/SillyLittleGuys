package dev.chililisoup.sillylittleguys.reg.neoforge;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

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
        Supplier<EntityType<T>> entityType = SillyLittleGuysNeoForge.ENTITY_TYPES.register(
                name,
                () -> builder.build(SillyLittleGuys.loc(name).toString())
        );

        SillyLittleGuysNeoForge.ENTITY_ATTRIBUTES.add(
                new SillyLittleGuysNeoForge.EntityAttributesEntry<>(entityType, defaultAttributes)
        );

        SillyLittleGuysNeoForge.SPAWN_EGGS.add(SillyLittleGuysNeoForge.ITEMS.register(
                name + "_spawn_egg",
                () -> new DeferredSpawnEggItem(
                        entityType,
                        backgroundColor,
                        highlightColor,
                        new Item.Properties()
                )
        ));

        SillyLittleGuysNeoForge.SPAWN_PLACEMENTS.add(
                new SillyLittleGuysNeoForge.SpawnPlacementEntry<>(
                        entityType,
                        spawnPlacementType,
                        heightmapType,
                        spawnPredicate
                )
        );

        return entityType;
    }
}
