package dev.chililisoup.sillylittleguys.entity.behavior;

import com.google.common.collect.ImmutableMap;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;

public class MonkeyFinishEquip extends Behavior<CapuchinMonkey> {
    public MonkeyFinishEquip() {
        super(ImmutableMap.of(ModMemoryModuleTypes.MID_EQUIP.get(), MemoryStatus.VALUE_PRESENT), 20, 60);
    }

    @Override
    protected void start(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        ItemStack head = monkey.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack hand = monkey.getMainHandItem();

        monkey.setItemSlot(EquipmentSlot.MAINHAND, head);
        monkey.setItemSlot(EquipmentSlot.HEAD, hand);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        return !monkey.isInWaterOrBubble();
    }

    @Override
    protected void stop(ServerLevel level, CapuchinMonkey monkey, long gameTime) {
        monkey.stopTriggeredAnim("arm_controller", "equip");
        monkey.stopTriggeredAnim("arm_controller", "unequip");

        monkey.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
        monkey.getBrain().eraseMemory(ModMemoryModuleTypes.MID_EQUIP.get());
    }
}
