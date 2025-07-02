package dev.chililisoup.sillylittleguys.neoforge.client;

import dev.chililisoup.sillylittleguys.client.SillyLittleGuysClient;
import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SillyLittleGuysClientNeoForge {
    public static final ArrayList<EntityRendererEntry<? extends Entity>> ENTITY_RENDERERS = new ArrayList<>();

    public static void init(IEventBus eventBus) {
        SillyLittleGuysClient.init();

        eventBus.addListener(SillyLittleGuysClientNeoForge::registerEntityRenderers);
        eventBus.addListener(SillyLittleGuysClientNeoForge::addToTabs);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ENTITY_RENDERERS.forEach(entry -> entry.register(event));
    }

    public record EntityRendererEntry<T extends Entity>(Supplier<EntityType<T>> entityType, EntityRendererProvider<T> renderer) {
        public void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(this.entityType.get(), this.renderer);
        }
    }

    public static void addToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
            SillyLittleGuysNeoForge.SPAWN_EGGS.forEach(egg -> event.accept(egg.get()));
    }
}
