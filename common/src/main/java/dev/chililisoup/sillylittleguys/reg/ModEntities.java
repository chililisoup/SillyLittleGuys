package dev.chililisoup.sillylittleguys.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

public class ModEntities {
    public static final Supplier<EntityType<CapuchinMonkey>> CAPUCHIN_MONKEY = register(
            EntityType.Builder.of(
                    CapuchinMonkey::new,
                    MobCategory.CREATURE
            ).sized(0.6F, 1F).eyeHeight(0.85F).clientTrackingRange(8),
            "capuchin_monkey",
            CapuchinMonkey::createAttributes,
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING,
            CapuchinMonkey::checkCapuchinMonkeySpawnRules,
            0x866945,
            0xECE1AC
    );

    @ExpectPlatform
    private static <T extends Mob> Supplier<EntityType<T>> register(
            EntityType.Builder<T> builder,
            String name,
            Supplier<AttributeSupplier.Builder> defaultAttributes,
            SpawnPlacementType spawnPlacementType,
            Heightmap.Types heightmapType,
            SpawnPlacements.SpawnPredicate<T> spawnPredicate,
            int backgroundColor,
            int highlightColor
    ) {
        throw new AssertionError();
    }

    public static void init() {}
}
