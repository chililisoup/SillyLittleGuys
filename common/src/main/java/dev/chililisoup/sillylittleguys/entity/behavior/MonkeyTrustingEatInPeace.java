package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class MonkeyTrustingEatInPeace extends Behavior<CapuchinMonkey> {
    private long startTime;

    public MonkeyTrustingEatInPeace() {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.IS_PRIVATELY_EATING.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LIKED_PLAYER, MemoryStatus.VALUE_ABSENT
        ), 100);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CapuchinMonkey monkey) {
        return monkey.isHoldingFood();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        Brain<CapuchinMonkey> brain = monkey.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.LIKED_PLAYER))
            return false;

        Optional<UUID> tamerUUID = brain.getMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get());
        if (tamerUUID.isPresent()) {
            Entity tamer = level.getEntity(tamerUUID.get());
            if (tamer != null && tamer.closerThan(monkey, 8)) {
                ItemStack heldStack = monkey.getMainHandItem();
                if (!heldStack.isEmpty()) {
                    monkey.spawnAtLocation(heldStack);
                    monkey.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }

                brain.setMemory(MemoryModuleType.IS_PANICKING, true);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.triggerAnim("arm_controller", "consume");

        monkey.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.startTime = gameTime;
    }

    @Override
    protected void tick(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        long elapsedTime = gameTime - this.startTime;
        if (elapsedTime < 10) return;

        ItemStack heldStack = monkey.getMainHandItem();
        if (!monkey.isFood(heldStack)) return;

        if (elapsedTime % 4 == 0) {
            Vec3 offset = new Vec3((monkey.getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
                    .xRot(-monkey.getXRot() * (float) (Math.PI / 180.0))
                    .yRot(-monkey.getYRot() * (float) (Math.PI / 180.0));

            level.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, heldStack),
                    monkey.getX() + monkey.getLookAngle().x / 2.0,
                    monkey.getY(),
                    monkey.getZ() + monkey.getLookAngle().z / 2.0,
                    5,
                    offset.x,
                    offset.y + 0.05,
                    offset.z,
                    0.1
            );
        }

        if (elapsedTime >= 40) {
            monkey.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

            Brain<CapuchinMonkey> brain = monkey.getBrain();
            if (monkey.getRandom().nextInt(3) == 0) {
                level.broadcastEntityEvent(monkey, (byte) 41);
                brain.getMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get()).ifPresent(
                        tamerUUID -> brain.setMemory(MemoryModuleType.LIKED_PLAYER, tamerUUID)
                );
            } else {
                level.broadcastEntityEvent(monkey, (byte) 40);
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.stopTriggeredAnim("arm_controller", "consume");

        monkey.getBrain().eraseMemory(ModMemoryModuleTypes.IS_PRIVATELY_EATING.get());
    }
}
