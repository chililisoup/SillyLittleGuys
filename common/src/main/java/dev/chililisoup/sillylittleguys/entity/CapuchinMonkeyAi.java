package dev.chililisoup.sillylittleguys.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import dev.chililisoup.sillylittleguys.entity.behavior.*;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModItemTags;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import dev.chililisoup.sillylittleguys.reg.ModSensorTypes;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class CapuchinMonkeyAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final UniformInt TIME_BETWEEN_EQUIPS = UniformInt.of(300, 1200);

    protected static final ImmutableList<SensorType<? extends Sensor<? super CapuchinMonkey>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_ADULT,
            SensorType.HURT_BY,
            ModSensorTypes.MONKEY_TEMPTATIONS.get()
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATE_RECENTLY,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.LIKED_PLAYER,
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.ADMIRING_ITEM,
            ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(),
            ModMemoryModuleTypes.MID_EQUIP.get(),
            ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get(),
            ModMemoryModuleTypes.TRY_TRUST_TICKS.get(),
            ModMemoryModuleTypes.MID_TRUSTING_BY.get(),
            ModMemoryModuleTypes.IS_PRIVATELY_EATING.get()
    );

    public static Brain.Provider<CapuchinMonkey> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<CapuchinMonkey> makeBrain(Brain<CapuchinMonkey> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initRestActivity(brain);
        initEquipItemActivity(brain);
        initTrustingActivity(brain);
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
                new SitWhenOrderedTo<>(),
                new MoveToTargetSink(),
                new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                new CountDownCooldownTicks(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get()),
                new CountDownCooldownTicks(ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get()),
                new CountDownCooldownTicks(ModMemoryModuleTypes.TRY_TRUST_TICKS.get())
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
                        Pair.of(1, new AnimalMakeLove(ModEntities.CAPUCHIN_MONKEY.get())),
                        Pair.of(2, new FollowTemptation(livingEntity -> 1.25F)),
                        Pair.of(3, BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F)),
                        Pair.of(4, new FollowOwner<>(1, 10, 2)),
                        Pair.of(5, new RunOne<>(ImmutableList.of(
                                Pair.of(RandomStroll.stroll(1.0F), 2),
                                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                                Pair.of(new DoNothing(30, 60), 1)
                        )))
                ),
                ImmutableSet.of(Pair.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT))
        );
    }

    private static void initRestActivity(Brain<CapuchinMonkey> brain) {
        brain.addActivityWithConditions(
                Activity.REST,
                ImmutableList.of(
                        Pair.of(0, new RunOne<>(ImmutableList.of(
                                Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1),
                                Pair.of(SetEntityLookTarget.create(ModEntities.CAPUCHIN_MONKEY.get(), 8.0F), 1),
                                Pair.of(SetEntityLookTarget.create(8.0F), 1),
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
                        Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(MemoryModuleType.ADMIRING_ITEM, ModMemoryModuleTypes.MID_EQUIP.get())
        );
    }

    private static void initTrustingActivity(Brain<CapuchinMonkey> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(
                Activity.MEET,
                ImmutableList.of(
                        Pair.of(0, new MonkeyTrustingFinish(1.25F)),
                        Pair.of(1, new MonkeyTrustingAskForMore(1.25F)),
                        Pair.of(2, new MonkeyTrustingEatInPeace()),
                        Pair.of(3, new MonkeyTrustingGoSomewherePrivate(1.5F))
                ),
                ImmutableSet.of(
                        Pair.of(ModMemoryModuleTypes.MID_TRUSTING_BY.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(ModMemoryModuleTypes.TRY_TRUST_TICKS.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(
                        ModMemoryModuleTypes.MID_TRUSTING_BY.get(),
                        ModMemoryModuleTypes.TRY_TRUST_TICKS.get(),
                        ModMemoryModuleTypes.IS_PRIVATELY_EATING.get()
                )
        );
    }

    public static boolean tryBeginTrustingActivity(Brain<CapuchinMonkey> brain, Player player) {
        Optional<UUID> existingPlayer = brain.getMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get());
        if (existingPlayer.isPresent() && existingPlayer.get().equals(player.getUUID())) {
            brain.setMemory(ModMemoryModuleTypes.TRY_TRUST_TICKS.get(), 2400);
            return true;
        }

        if (brain.hasMemoryValue(ModMemoryModuleTypes.TRY_TRUST_TICKS.get()))
            return false;

        Optional<Player> temptingPlayer = brain.getMemory(MemoryModuleType.TEMPTING_PLAYER);
        if (temptingPlayer.isEmpty() || !temptingPlayer.get().is(player))
            return false;

        brain.setMemory(ModMemoryModuleTypes.MID_TRUSTING_BY.get(), player.getUUID());
        brain.setMemory(ModMemoryModuleTypes.TRY_TRUST_TICKS.get(), 2400);
        return true;
    }

    public static void updateActivity(CapuchinMonkey monkey) {
        if (!monkey.isTame()) {
            monkey.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                    Activity.MEET,
                    Activity.ADMIRE_ITEM,
                    Activity.IDLE
            ));
            return;
        }

        if (monkey.isOrderedToSit()) {
            monkey.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                    Activity.ADMIRE_ITEM,
                    Activity.REST
            ));
            return;
        }

        monkey.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                Activity.ADMIRE_ITEM,
                Activity.IDLE
        ));
    }

    public static Predicate<ItemStack> getTemptations() {
        return itemStack -> itemStack.is(ModItemTags.MONKEY_FOOD);
    }
}
