package dev.chililisoup.sillylittleguys.client.reg.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class ModEntityRenderersImpl {
    public static <T extends Entity> void register(Supplier<EntityType<T>> entityType, EntityRendererProvider<T> renderer) {
        EntityRendererRegistry.register(entityType.get(), renderer);
    }
}
