package dev.chililisoup.sillylittleguys.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkeyAi;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;

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
