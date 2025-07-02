package dev.chililisoup.sillylittleguys.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Supplier;

public class ModEntities {
    public static final Supplier<EntityType<CapuchinMonkey>> CAPUCHIN_MONKEY = register(
            EntityType.Builder.of(
                    CapuchinMonkey::new,
                    MobCategory.CREATURE
            ).sized(0.6F, 1F).eyeHeight(0.85F).clientTrackingRange(8),
            "capuchin_monkey",
            CapuchinMonkey::createAttributes,
            0x866945,
            0xECE1AC
    );

    @ExpectPlatform
    private static <T extends Mob> Supplier<EntityType<T>> register(
            EntityType.Builder<T> builder,
            String name,
            Supplier<AttributeSupplier.Builder> defaultAttributes,
            int backgroundColor,
            int highlightColor
    ) {
        throw new AssertionError();
    }

    public static void init() {}
}
