package dev.chililisoup.sillylittleguys.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import dev.chililisoup.sillylittleguys.entity.behavior.MonkeyFinishEquip;
import dev.chililisoup.sillylittleguys.entity.behavior.MonkeyPanic;
import dev.chililisoup.sillylittleguys.entity.behavior.MonkeyStartEquip;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModItemTags;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class CapuchinMonkeyAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final UniformInt TIME_BETWEEN_EQUIPS = UniformInt.of(300, 1200);

    protected static Brain<CapuchinMonkey> makeBrain(Brain<CapuchinMonkey> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initEquipItemActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<CapuchinMonkey> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new MonkeyPanic(2.0F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                new CountDownCooldownTicks(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get()),
                new CountDownCooldownTicks(ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get())
        ));
    }

    private static void initIdleActivity(Brain<CapuchinMonkey> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new RunOne<>(ImmutableList.of(
                                Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
                                Pair.of(SetEntityLookTarget.create(ModEntities.CAPUCHIN_MONKEY.get(), 8.0F), 1),
                                Pair.of(SetEntityLookTarget.create(8.0F), 1),
                                Pair.of(new DoNothing(30, 60), 1)
                        ))),
                        Pair.of(0, new AnimalMakeLove(ModEntities.CAPUCHIN_MONKEY.get())),
                        Pair.of(1, new FollowTemptation(livingEntity -> 1.25F)),
                        Pair.of(2, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(3, new RunOne<>(ImmutableList.of(
                                Pair.of(RandomStroll.stroll(1.0F), 2),
                                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                                Pair.of(new DoNothing(30, 60), 1)
                        )))
                ),
                ImmutableSet.of(Pair.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT))
        );
    }

    private static void initEquipItemActivity(Brain<CapuchinMonkey> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(
                Activity.ADMIRE_ITEM,
                ImmutableList.of(
                        Pair.of(0, new MonkeyFinishEquip()),
                        Pair.of(1, new MonkeyStartEquip(TIME_BETWEEN_EQUIPS))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(MemoryModuleType.ADMIRING_ITEM, ModMemoryModuleTypes.MID_EQUIP.get())
        );
    }

    public static void updateActivity(CapuchinMonkey monkey) {
        monkey.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ModItemTags.MONKEY_FOOD);
    }
}
