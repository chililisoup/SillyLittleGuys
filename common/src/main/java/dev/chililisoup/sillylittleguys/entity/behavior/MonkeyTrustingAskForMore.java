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

public class MonkeyTrustingAskForMore extends Behavior<CapuchinMonkey> {
    private final float speedMultiplier;

    public MonkeyTrustingAskForMore(float speedMultiplier) {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.IS_PRIVATELY_EATING.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_ABSENT
        ), 20);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CapuchinMonkey monkey) {
        return this.canStillUse(level, monkey, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        return !monkey.isHoldingFood();
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        Optional<UUID> tamerUUID = monkey.getBrain().getMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get());
        if (tamerUUID.isPresent()) {
            Entity tamer = level.getEntity(tamerUUID.get());
            if (tamer != null)
                BehaviorUtils.setWalkAndLookTargetMemories(monkey, tamer, this.speedMultiplier, 1);
        }
    }
}
