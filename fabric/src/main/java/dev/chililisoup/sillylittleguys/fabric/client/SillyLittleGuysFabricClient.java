package dev.chililisoup.sillylittleguys.fabric.client;

import dev.chililisoup.sillylittleguys.client.SillyLittleGuysClient;
import dev.chililisoup.sillylittleguys.fabric.SillyLittleGuysFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public final class SillyLittleGuysFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SillyLittleGuysClient.init();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(tab ->
                SillyLittleGuysFabric.SPAWN_EGGS.forEach(tab::accept)
        );
    }
}
