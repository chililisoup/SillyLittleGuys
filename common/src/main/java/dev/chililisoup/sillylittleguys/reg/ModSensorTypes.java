package dev.chililisoup.sillylittleguys.reg;

import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkeyAi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.animal.goat.GoatAi;

import java.util.function.Supplier;

public class ModSensorTypes {
    public static final Supplier<SensorType<TemptingSensor>> MONKEY_TEMPTATIONS = register(
            "monkey_temptations", () -> new TemptingSensor(CapuchinMonkeyAi.getTemptations())
    );

    @ExpectPlatform
    private static <U extends Sensor<?>> Supplier<SensorType<U>> register(String name, Supplier<U> sensorSupplier) {
        throw new AssertionError();
    }

    public static void init() {}
}
