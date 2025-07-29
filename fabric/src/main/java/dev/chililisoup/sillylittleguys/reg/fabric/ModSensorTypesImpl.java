package dev.chililisoup.sillylittleguys.reg.fabric;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.function.Supplier;

public class ModSensorTypesImpl {
    public static <U extends Sensor<?>> Supplier<SensorType<U>> register(String name, Supplier<U> sensorSupplier) {
        SensorType<U> sensorType = Registry.register(
                BuiltInRegistries.SENSOR_TYPE,
                SillyLittleGuys.loc(name),
                new SensorType<>(sensorSupplier)
        );

        return () -> sensorType;
    }
}
