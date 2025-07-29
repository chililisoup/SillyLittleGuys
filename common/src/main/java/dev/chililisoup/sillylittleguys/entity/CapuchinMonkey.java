package dev.chililisoup.sillylittleguys.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModItemTags;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import dev.chililisoup.sillylittleguys.reg.ModSensorTypes;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.IntFunction;

public class CapuchinMonkey extends Animal implements GeoEntity, VariantHolder<CapuchinMonkey.Variant> {
    /* TODO:

    - Hand monkey food a few times for it to eat to tame it
    - Sounds
    - Add cool mechanics
    - Higher jump height w/ anim (similar to frog leaping?)
    - Dancing
    - Fighting??
    - Drop items they dont care about?

    */

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HOPPING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDimensions HOPPING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.35F);

    protected static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit");
    protected static final RawAnimation SIT_HOLD_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit_hold");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.walk");
    protected static final RawAnimation STAND_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.stand");
    protected static final RawAnimation HOP_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.hop");
    protected static final RawAnimation EQUIP_ANIM = RawAnimation.begin().then("animation.capuchin_monkey.equip", Animation.LoopType.PLAY_ONCE);
    protected static final RawAnimation UNEQUIP_ANIM = RawAnimation.begin().then("animation.capuchin_monkey.unequip", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);


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
            ModMemoryModuleTypes.EQUIP_COOLDOWN_TICKS.get(),
            ModMemoryModuleTypes.MID_EQUIP.get(),
            ModMemoryModuleTypes.HOP_COOLDOWN_TICKS.get(),
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.ADMIRING_ITEM
    );

    public CapuchinMonkey(EntityType<? extends CapuchinMonkey> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NotNull Brain.Provider<CapuchinMonkey> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected @NotNull Brain<CapuchinMonkey> makeBrain(Dynamic<?> dynamic) {
        return CapuchinMonkeyAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Brain<CapuchinMonkey> getBrain() {
        return (Brain<CapuchinMonkey>) super.getBrain();
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(Util.getRandom(Variant.values(), level.getRandom()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("slg_capuchinMonkeyBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("slg_capuchinMonkeyActivityUpdate");
        CapuchinMonkeyAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    public boolean isHopping() {
        return this.entityData.get(DATA_HOPPING);
    }

    public void setHopping(boolean hopping) {
        this.entityData.set(DATA_HOPPING, hopping);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        ItemStack hat = this.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.is(Items.SHEARS) && !hat.isEmpty() && !EnchantmentHelper.has(hat, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            this.level().playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.gameEvent(GameEvent.SHEAR, player);

            if (!this.level().isClientSide()) {
                this.spawnAtLocation(hat);
                this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                itemStack.hurtAndBreak(1, player, getSlotForHand(hand));
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (!itemStack.isEmpty() && this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            itemStack.consume(1, player);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copyWithCount(1));

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 41) this.spawnTrustingParticles(true);
        else if (id == 40) this.spawnTrustingParticles(false);
        else super.handleEntityEvent(id);
    }

    private void spawnTrustingParticles(boolean isTrusted) {
        ParticleOptions particleOptions = isTrusted ? ParticleTypes.HEART : ParticleTypes.SMOKE;

        for (int i = 0; i < 7; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
        }
    }

    public CapuchinMonkey.@NotNull Variant getVariant() {
        return CapuchinMonkey.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(CapuchinMonkey.Variant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(CapuchinMonkey.Variant.byId(compound.getInt("Variant")));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HOPPING, false);
        builder.define(DATA_TRUSTING, false);
        builder.define(DATA_VARIANT_ID, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        if (DATA_HOPPING.equals(dataAccessor)) {
            this.setBoundingBox(this.makeBoundingBox());
        }

        super.onSyncedDataUpdated(dataAccessor);
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return this.isHopping() ?
                HOPPING_DIMENSIONS.scale(this.getAgeScale()).makeBoundingBox(this.position()) :
                super.makeBoundingBox();

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItemTags.MONKEY_FOOD);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CapuchinMonkey monkey = ModEntities.CAPUCHIN_MONKEY.get().create(level);
        if (monkey != null)
            monkey.setVariant(this.random.nextBoolean() ? this.getVariant() : ((CapuchinMonkey) otherParent).getVariant());

        return monkey;
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource damageSource) {
        ItemStack handItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!handItem.isEmpty()) {
            this.spawnAtLocation(handItem);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        ItemStack headItem = this.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.isEmpty()) {
            this.spawnAtLocation(headItem);
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        super.dropAllDeathLoot(level, damageSource);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(stack);
        return equipmentSlot == EquipmentSlot.HEAD && this.getItemBySlot(equipmentSlot).isEmpty();
    }

    public float swapInterest() {
        ItemStack head = this.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack hand = this.getMainHandItem();

        if (head.isEmpty())
            return this.getEquipmentSlotForItem(hand) == EquipmentSlot.HEAD ? 0.15F : 0.015F;
        else if (EnchantmentHelper.has(head, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
            return 0;

        if (hand.isEmpty())
            return this.getEquipmentSlotForItem(head) == EquipmentSlot.HEAD ? 0.03F : 0.75F;

        EquipmentSlot headSlot = this.getEquipmentSlotForItem(head);
        EquipmentSlot handSlot = this.getEquipmentSlotForItem(hand);

        if (headSlot == EquipmentSlot.HEAD && handSlot == EquipmentSlot.HEAD)
            return this.canReplaceCurrentItem(hand, head) ? 0.15F : 0;

        return handSlot == EquipmentSlot.HEAD ? 0.15F : 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animController));
        controllers.add(new AnimationController<>(this, "equip_controller", 5, state -> PlayState.STOP)
                .triggerableAnim("equip", EQUIP_ANIM)
                .triggerableAnim("unequip", UNEQUIP_ANIM)
        );
    }

    protected <E extends CapuchinMonkey> PlayState animController(final AnimationState<E> event) {
        event.setControllerSpeed(1);

        if (this.getVehicle() != null)
            return event.setAndContinue(STAND_ANIM);

        if (this.isHopping())
            return event.setAndContinue(HOP_ANIM);

        if (event.isMoving()) {
            event.setControllerSpeed((float) (this.getDeltaMovement().horizontalDistance() * 20));
            return event.setAndContinue(WALK_ANIM);
        }

        if (!this.onGround())
            return event.setAndContinue(STAND_ANIM);

        return event.setAndContinue(
                this.getMainHandItem().isEmpty() ? SIT_ANIM : SIT_HOLD_ANIM
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public enum Variant implements StringRepresentable {
        HOODED(0, "hooded"),
        WHITE_FACED(1, "white_faced");

        public static final Codec<CapuchinMonkey.Variant> CODEC =
                StringRepresentable.fromEnum(CapuchinMonkey.Variant::values);
        private static final IntFunction<CapuchinMonkey.Variant> BY_ID =
                ByIdMap.continuous(CapuchinMonkey.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        Variant(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public static CapuchinMonkey.Variant byId(int id) {
            return BY_ID.apply(id);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
