package dev.chililisoup.sillylittleguys.client.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.client.renderer.CapuchinMonkeyRenderer;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public class ModEntityRenderers {
    public static void init() {
        register(ModEntities.CAPUCHIN_MONKEY, CapuchinMonkeyRenderer::new);
    }

    @ExpectPlatform
    private static <T extends Entity> void register(Supplier<EntityType<T>> entityType, EntityRendererProvider<T> renderer) {
        throw new AssertionError();
    }
}
