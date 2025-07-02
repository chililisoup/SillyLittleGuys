package dev.chililisoup.sillylittleguys.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.advancements.critereon.EntitySubPredicates;

import java.util.Optional;

public class ModEntitySubPredicates {
    public static final EntitySubPredicates.EntityVariantPredicateType<CapuchinMonkey.Variant> CAPUCHIN_MONKEY = register(
            "parrot",
            EntitySubPredicates.EntityVariantPredicateType.create(
                    CapuchinMonkey.Variant.CODEC, entity -> entity instanceof CapuchinMonkey monkey ? Optional.of(monkey.getVariant()) : Optional.empty()
            )
    );

    @ExpectPlatform
    private static <V> EntitySubPredicates.EntityVariantPredicateType<V> register(String name, EntitySubPredicates.EntityVariantPredicateType<V> predicateType) {
        throw new AssertionError();
    }

    public static void init() {}
}
