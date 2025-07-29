package dev.chililisoup.sillylittleguys.neoforge;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.neoforge.client.SillyLittleGuysClientNeoForge;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.Supplier;

@Mod(SillyLittleGuys.MOD_ID)
public final class SillyLittleGuysNeoForge {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SillyLittleGuys.MOD_ID);
    public static final ArrayList<DeferredItem<DeferredSpawnEggItem>> SPAWN_EGGS = new ArrayList<>();
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE,
            SillyLittleGuys.MOD_ID
    );
    public static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_PREDICATES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE,
            SillyLittleGuys.MOD_ID
    );
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(
            BuiltInRegistries.MEMORY_MODULE_TYPE,
            SillyLittleGuys.MOD_ID
    );
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(
            BuiltInRegistries.SENSOR_TYPE,
            SillyLittleGuys.MOD_ID
    );
    public static final ArrayList<EntityAttributesEntry<? extends LivingEntity>> ENTITY_ATTRIBUTES = new ArrayList<>();

    public SillyLittleGuysNeoForge(IEventBus eventBus) {
        SillyLittleGuys.init();

        ITEMS.register(eventBus);
        ENTITY_TYPES.register(eventBus);
        MEMORY_MODULE_TYPES.register(eventBus);
        SENSOR_TYPES.register(eventBus);

        eventBus.addListener(SillyLittleGuysNeoForge::createDefaultAttributes);

        if (FMLEnvironment.dist == Dist.CLIENT)
            SillyLittleGuysClientNeoForge.init(eventBus);
    }

    public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
        ENTITY_ATTRIBUTES.forEach(entry -> entry.register(event));
    }

    public record EntityAttributesEntry<T extends LivingEntity>(Supplier<EntityType<T>> entityType, Supplier<AttributeSupplier.Builder> defaultAttributes) {
        public void register(EntityAttributeCreationEvent event) {
            event.put(this.entityType.get(), this.defaultAttributes.get().build());
        }
    }
}
