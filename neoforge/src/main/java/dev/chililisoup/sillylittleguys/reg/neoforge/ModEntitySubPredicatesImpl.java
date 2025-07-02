package dev.chililisoup.sillylittleguys.reg.neoforge;

import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.advancements.critereon.EntitySubPredicates;

public class ModEntitySubPredicatesImpl {
    public static <V> EntitySubPredicates.EntityVariantPredicateType<V> register(String name, EntitySubPredicates.EntityVariantPredicateType<V> predicateType) {
        SillyLittleGuysNeoForge.ENTITY_PREDICATES.register(name, () -> predicateType.codec);
        return predicateType;
    }
}
