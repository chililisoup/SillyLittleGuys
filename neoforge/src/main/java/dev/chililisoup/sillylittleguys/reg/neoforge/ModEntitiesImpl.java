package dev.chililisoup.sillylittleguys.reg.neoforge;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

public class ModEntitiesImpl {
    public static <T extends Mob> Supplier<EntityType<T>> register(
            EntityType.Builder<T> builder,
            String name,
            Supplier<AttributeSupplier.Builder> defaultAttributes,
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

        return entityType;
    }
}
