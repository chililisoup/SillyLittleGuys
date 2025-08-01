package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SitWhenOrderedTo<E extends TamableAnimal> extends Behavior<E> {
    public SitWhenOrderedTo() {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_PRESENT);
            builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
            builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
            return builder.build();
        }));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if (!entity.isTame())
            return false;
        if (entity.isInWaterOrBubble())
            return false;
        if (!entity.onGround())
            return false;

        LivingEntity owner = entity.getOwner();
        if (owner == null)
            return true;

        return (!(entity.distanceToSqr(owner) < 144.0) || owner.getLastHurtByMob() == null) && entity.isOrderedToSit();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        return entity.isOrderedToSit();
    }

    @Override
    public void start(ServerLevel level, E entity, long gameTime) {
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        entity.setInSittingPose(true);
    }

    @Override
    public void stop(ServerLevel level, E entity, long gameTime) {
        entity.setInSittingPose(false);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }
}
