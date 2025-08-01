package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.UUID;

public class MonkeyTrustingFinish extends Behavior<CapuchinMonkey> {
    private final float speedMultiplier;

    public MonkeyTrustingFinish(float speedMultiplier) {
        super(ImmutableMap.of(MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_PRESENT));
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        Optional<UUID> tamerUUID = monkey.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
        if (tamerUUID.isEmpty()) return false;

        Entity tamer = level.getEntity(tamerUUID.get());
        if (tamer == null) return false;

        if (tamer.closerThan(monkey, 2.5)) {
            level.broadcastEntityEvent(monkey, (byte) 41);
            return false;
        }

        return true;
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        Optional<UUID> tamerUUID = monkey.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
        if (tamerUUID.isPresent()) {
            Entity tamer = level.getEntity(tamerUUID.get());
            if (tamer != null)
                BehaviorUtils.setWalkAndLookTargetMemories(monkey, tamer, this.speedMultiplier, 1);
        }
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.getBrain().eraseMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get());
        monkey.getBrain().eraseMemory(ModMemoryModuleTypes.TRY_TRUST_TICKS.get());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }
}
