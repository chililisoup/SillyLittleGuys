package dev.chililisoup.sillylittleguys.reg.fabric;

import com.mojang.serialization.Codec;
import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryModuleTypesImpl {
    public static <U> Supplier<MemoryModuleType<U>> register(String name, Codec<U> codec) {
        MemoryModuleType<U> memoryModuleType = Registry.register(
                BuiltInRegistries.MEMORY_MODULE_TYPE,
                SillyLittleGuys.loc(name),
                new MemoryModuleType<>(Optional.of(codec))
        );

        return () -> memoryModuleType;
    }
}
