package dev.chililisoup.sillylittleguys.fabric;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;

public final class SillyLittleGuysFabric implements ModInitializer {
    public static final ArrayList<SpawnEggItem> SPAWN_EGGS = new ArrayList<>();

    @Override
    public void onInitialize() {
        SillyLittleGuys.init();

        BiomeModifications.addSpawn(
                BiomeSelectors.tag(BiomeTags.IS_JUNGLE),
                MobCategory.CREATURE,
                ModEntities.CAPUCHIN_MONKEY.get(),
                15,
                1,
                4
        );
    }
}
