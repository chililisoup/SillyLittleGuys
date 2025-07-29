package dev.chililisoup.sillylittleguys.reg;

import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.function.Supplier;

public class ModMemoryModuleTypes {
    public static final Supplier<MemoryModuleType<Boolean>> MID_EQUIP = register("mid_equip", Codec.BOOL);
    public static final Supplier<MemoryModuleType<Integer>> EQUIP_COOLDOWN_TICKS = register("equip_cooldown_ticks", Codec.INT);
    public static final Supplier<MemoryModuleType<Integer>> HOP_COOLDOWN_TICKS = register("hop_cooldown_ticks", Codec.INT);

    @ExpectPlatform
    private static <U> Supplier<MemoryModuleType<U>> register(String name, Codec<U> codec) {
        throw new AssertionError();
    }

    public static void init() {}
}
