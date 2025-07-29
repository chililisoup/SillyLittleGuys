package dev.chililisoup.sillylittleguys.reg.neoforge;

import dev.chililisoup.sillylittleguys.neoforge.SillyLittleGuysNeoForge;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

public class ModSensorTypesImpl {
    public static <U extends Sensor<?>> Supplier<SensorType<U>> register(String name, Supplier<U> sensorSupplier) {
        return SillyLittleGuysNeoForge.SENSOR_TYPES.register(name, () -> new SensorType<>(sensorSupplier));
    }
}
