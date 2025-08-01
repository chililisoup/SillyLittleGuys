package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class MonkeyStartEquip extends Behavior<CapuchinMonkey> {
    private final UniformInt timeBetweenEquips;

    public MonkeyStartEquip(UniformInt timeBetweenEquips) {
        super(ImmutableMap.of(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT), 20);
        this.timeBetweenEquips = timeBetweenEquips;
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.triggerAnim(
                "arm_controller",
                monkey.getMainHandItem().isEmpty() ? "unequip" : "equip"
        );

        monkey.getBrain().setMemory(MemoryModuleType.ADMIRING_ITEM, true);
        monkey.getBrain().setMemory(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(), this.timeBetweenEquips.sample(level.random));
    }

    private boolean passesExtraStartConditions(CapuchinMonkey monkey) {
        if (!monkey.onGround() || monkey.isInWater() || monkey.isInLava())
            return false;

        if (monkey.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && monkey.getMainHandItem().isEmpty())
            return false;

        if (monkey.getDeltaMovement().lengthSqr() >= 0.015)
            return false;

        return monkey.getRandom().nextFloat() < monkey.swapInterest();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CapuchinMonkey monkey) {
        if (this.passesExtraStartConditions(monkey)) return true;

        monkey.getBrain().setMemory(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(), this.timeBetweenEquips.sample(level.random) / 10);
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        return !monkey.isInWaterOrBubble();
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        if (!this.canStillUse(level, monkey, gameTime)) {
            monkey.stopTriggeredAnim("arm_controller", "equip");
            monkey.stopTriggeredAnim("arm_controller", "unequip");

            monkey.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
            return;
        }

        monkey.getBrain().setMemory(ModMemoryModuleTypes.MID_EQUIP.get(), true);
    }
}
