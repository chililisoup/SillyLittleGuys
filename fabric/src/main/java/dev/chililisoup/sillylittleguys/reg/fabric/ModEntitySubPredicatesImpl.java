package dev.chililisoup.sillylittleguys.reg.fabric;

import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModEntitySubPredicatesImpl {
    public static <V> EntitySubPredicates.EntityVariantPredicateType<V> register(String name, EntitySubPredicates.EntityVariantPredicateType<V> predicateType) {
        Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, name, predicateType.codec);
        return predicateType;
    }
}
