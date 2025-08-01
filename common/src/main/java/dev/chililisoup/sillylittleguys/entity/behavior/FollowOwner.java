package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class FollowOwner<E extends TamableAnimal> extends Behavior<E> {
    private final float speedModifier;
    private final float startDistance;
    private final float stopDistance;
    @Nullable private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowOwner(float speedModifier, float startDistance, float stopDistance) {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_PRESENT);
            builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
            return builder.build();
        }));
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        LivingEntity owner = entity.getOwner();
        if (owner == null)
            return false;
        if (entity.unableToMoveToOwner())
            return false;
        if (entity.distanceToSqr(owner) < this.startDistance * this.startDistance)
            return false;

        this.owner = owner;
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        if (this.owner == null || entity.getNavigation().isDone())
            return false;

        return !entity.unableToMoveToOwner() && !(entity.distanceToSqr(this.owner) <= this.stopDistance * this.stopDistance);
    }

    @Override
    public void start(ServerLevel level, E entity, long gameTime) {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = entity.getPathfindingMalus(PathType.WATER);
        entity.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop(ServerLevel level, E entity, long gameTime) {
        this.owner = null;
        entity.getNavigation().stop();
        entity.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick(ServerLevel level, E entity, long gameTime) {
        if (this.owner == null) return;

        boolean shouldTryTeleport = entity.shouldTryTeleportToOwner();
        if (!shouldTryTeleport) entity.getBrain().setMemory(
                MemoryModuleType.LOOK_TARGET,
                new EntityTracker(this.owner, true)
        );

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (shouldTryTeleport)
                entity.tryToTeleportToOwner();
            else
                entity.getBrain().setMemory(
                        MemoryModuleType.WALK_TARGET,
                        new WalkTarget(this.owner, this.speedModifier, 1)
                );
        }
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }
}
