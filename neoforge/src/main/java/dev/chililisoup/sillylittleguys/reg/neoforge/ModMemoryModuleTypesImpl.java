package dev.chililisoup.sillylittleguys.reg.neoforge;

import com.mojang.serialization.Codec;
import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryModuleTypesImpl {
    public static <U> Supplier<MemoryModuleType<U>> register(String name, Codec<U> codec) {
        return SillyLittleGuysNeoForge.MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }
}
