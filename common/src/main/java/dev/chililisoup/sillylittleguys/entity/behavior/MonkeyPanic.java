package dev.chililisoup.sillylittleguys.entity.behavior;

import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;

public class MonkeyPanic extends AnimalPanic<CapuchinMonkey> {
    public static final UniformInt HOPPING_TIME = UniformInt.of(40, 60);

    public MonkeyPanic(float speedModifier) {
        super(speedModifier);
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.getBrain().setMemory(ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get(), HOPPING_TIME.sample(level.random));
        monkey.setHopping(true);
        super.start(level, monkey, gameTime);
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.setHopping(false);
        monkey.getBrain().eraseMemory(ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get());
        super.stop(level, monkey, gameTime);
    }

    @Override
    protected void tick(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        if (monkey.isHopping() && !monkey.getBrain().hasMemoryValue(ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get()))
            monkey.setHopping(false);

        super.tick(level, monkey, gameTime);
    }
}