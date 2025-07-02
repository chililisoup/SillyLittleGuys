package dev.chililisoup.sillylittleguys.fabric;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;

public final class SillyLittleGuysFabric implements ModInitializer {
    public static final ArrayList<SpawnEggItem> SPAWN_EGGS = new ArrayList<>();

    @Override
    public void onInitialize() {
        SillyLittleGuys.init();
    }
}
