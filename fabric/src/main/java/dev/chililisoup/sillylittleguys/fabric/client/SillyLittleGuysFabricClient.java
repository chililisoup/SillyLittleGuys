package dev.chililisoup.sillylittleguys.fabric.client;

import com.mojang.datafixers.util.Pair;
import dev.chililisoup.sillylittleguys.client.SillyLittleGuysClient;
import dev.chililisoup.sillylittleguys.fabric.SillyLittleGuysFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public final class SillyLittleGuysFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SillyLittleGuysClient.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tab ->
                SillyLittleGuysFabric.SPAWN_EGGS.forEach(eggItem -> {
                    Pair<ItemStack, Boolean> eggPlacement = SillyLittleGuysClient.getEggPlacement(eggItem, tab.getDisplayStacks());
                    if (eggPlacement == null)
                        tab.accept(eggItem);
                    else if (eggPlacement.getSecond())
                        tab.addAfter(eggPlacement.getFirst(), eggItem);
                    else
                        tab.addBefore(eggPlacement.getFirst(), eggItem);
                })
        );
    }
}
