package dev.chililisoup.sillylittleguys.client.reg.neoforge;

import dev.chililisoup.sillylittleguys.neoforge.client.SillyLittleGuysClientNeoForge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class ModEntityRenderersImpl {
    public static <T extends Entity> void register(Supplier<EntityType<T>> entityType, EntityRendererProvider<T> renderer) {
        SillyLittleGuysClientNeoForge.ENTITY_RENDERERS.add(new SillyLittleGuysClientNeoForge.EntityRendererEntry<>(entityType, renderer));
    }
}
