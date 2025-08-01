package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MonkeyTrustingGoSomewherePrivate extends Behavior<CapuchinMonkey> {
    private final float speedMultiplier;

    public MonkeyTrustingGoSomewherePrivate(float speedMultiplier) {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.IS_PRIVATELY_EATING.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_ABSENT
        ), 100, 120);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CapuchinMonkey monkey) {
        return this.canStillUse(level, monkey, 0);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        return monkey.isHoldingFood();
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        Brain<CapuchinMonkey> brain = monkey.getBrain();
        brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
        brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        if (this.canStillUse(level, monkey, gameTime))
            monkey.getBrain().setMemory(ModMemoryModuleTypes.IS_PRIVATELY_EATING.get(), true);
    }

    @Override
    protected void tick(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        if (monkey.getNavigation().isInProgress()) return;

        Vec3 pos = this.getPrivatePos(monkey, level);
        if (pos == null) return;

        monkey.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, this.speedMultiplier, 0));
    }

    @Nullable
    private Vec3 getPrivatePos(CapuchinMonkey monkey, ServerLevel level) {
        Optional<UUID> tamerUUID = monkey.getBrain().getMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get());
        if (tamerUUID.isPresent()) {
            Entity tamer = level.getEntity(tamerUUID.get());
            if (tamer != null)
                return LandRandomPos.getPosAway(monkey, 16, 7, tamer.position());
        }

        return LandRandomPos.getPos(monkey, 16, 7);
    }
}
