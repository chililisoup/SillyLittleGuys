package dev.chililisoup.sillylittleguys.entity;

import com.mojang.serialization.Codec;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModItemTags;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class CapuchinMonkey extends Animal implements GeoEntity, VariantHolder<CapuchinMonkey.Variant> {
    /* TODO:

    - Make sure trusting works correctly
    - Add correct foods to monkey food tag
    - Finish monkey texture
    - Add cool mechanics
    - Higher jump height w/ anim (similar to frog leaping?)

    */

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HOPPING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDimensions HOPPING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.35F);

    protected static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit");
    protected static final RawAnimation SIT_UPRIGHT_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit_upright");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.walk");
    protected static final RawAnimation STAND_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.stand");
    protected static final RawAnimation HOP_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.hop");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable private MonkeyAvoidEntityGoal<Player> monkeyAvoidPlayersGoal;
    @Nullable private MonkeyTemptGoal temptGoal;

    public CapuchinMonkey(EntityType<? extends CapuchinMonkey> entityType, Level level) {
        super(entityType, level);
        this.reassessTrustingGoals();
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(Util.getRandom(Variant.values(), level.getRandom()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new MonkeyTemptGoal(this, 1.2, itemStack -> itemStack.is(ModItemTags.MONKEY_FOOD), true);

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MonkeyPanicGoal(this, 1.75));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    boolean isTrusting() {
        return this.entityData.get(DATA_TRUSTING);
    }

    private void setTrusting(boolean trusting) {
        this.entityData.set(DATA_TRUSTING, trusting);
        this.reassessTrustingGoals();
    }

    boolean isHopping() {
        return this.entityData.get(DATA_HOPPING);
    }

    private void setHopping(boolean hopping) {
        this.entityData.set(DATA_HOPPING, hopping);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        ItemStack hat = this.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.is(Items.SHEARS) && !hat.isEmpty()) {
            this.level().playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.gameEvent(GameEvent.SHEAR, player);

            if (!this.level().isClientSide()) {
                this.spawnAtLocation(hat);
                this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                itemStack.hurtAndBreak(1, player, getSlotForHand(hand));
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            itemStack.consume(1, player);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copyWithCount(1));
        }

        if ((this.temptGoal != null && !this.temptGoal.isRunning()) || this.isTrusting() || !this.isFood(itemStack) || !(player.distanceToSqr(this) < 9.0))
            return super.mobInteract(player, hand);

        this.usePlayerItem(player, hand, itemStack);

        if (this.level().isClientSide)
            return InteractionResult.SUCCESS;

        if (this.random.nextInt(3) == 0) {
            this.setTrusting(true);
            this.spawnTrustingParticles(true);
            this.level().broadcastEntityEvent(this, (byte)41);
        } else {
            this.spawnTrustingParticles(false);
            this.level().broadcastEntityEvent(this, (byte)40);
        }

        return InteractionResult.CONSUME;
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

    protected void reassessTrustingGoals() {
        if (this.monkeyAvoidPlayersGoal == null) {
            this.monkeyAvoidPlayersGoal = new MonkeyAvoidEntityGoal<>(this, Player.class, 16.0F, 1.5, 1.875);
        }

        this.goalSelector.removeGoal(this.monkeyAvoidPlayersGoal);
        if (!this.isTrusting())
            this.goalSelector.addGoal(4, this.monkeyAvoidPlayersGoal);
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
        compound.putBoolean("Trusting", this.isTrusting());
        compound.putInt("Variant", this.getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setTrusting(compound.getBoolean("Trusting"));
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

    private static class MonkeyPanicGoal extends PanicGoal {
        private boolean hopping = false;
        private final CapuchinMonkey monkey;

        public MonkeyPanicGoal(CapuchinMonkey monkey, double speedModifier) {
            super(monkey, speedModifier);
            this.monkey = monkey;
        }

        protected boolean findRandomPosition(double scale) {
            for (int i = 0; i < 32; i++) {
                Vec3 vec3 = DefaultRandomPos.getPos(this.mob, (int) (10 * scale), (int) (4 * scale));
                if (vec3 != null) {
                    this.posX = vec3.x;
                    this.posY = vec3.y;
                    this.posZ = vec3.z;
                    return true;
                }
            }

            return false;
        }

        @Override
        protected boolean findRandomPosition() {
            return this.findRandomPosition(1);
        }

        @Override
        public boolean canContinueToUse() {
            if (super.canContinueToUse()) return true;
            if (!this.hopping) return false;

            this.monkey.setHopping(false);
            this.hopping = false;

            if (!this.findRandomPosition(4)) return false;

            this.mob.getNavigation().stop();
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier * 1.25);
            return true;
        }

        @Override
        public void start() {
            this.monkey.setHopping(true);
            this.hopping = true;
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            this.isRunning = true;
        }

        @Override
        public void stop() {
            this.monkey.setHopping(false);
            this.hopping = false;
            this.isRunning = false;
        }
    }

    private static class MonkeyAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final CapuchinMonkey monkey;

        public MonkeyAvoidEntityGoal(CapuchinMonkey monkey, Class<T> entityClassToAvoid, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
            super(monkey, entityClassToAvoid, maxDist, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.monkey = monkey;
        }

        @Override
        public boolean canUse() {
            return !this.monkey.isTrusting() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.monkey.isTrusting() && super.canContinueToUse();
        }
    }

    private static class MonkeyTemptGoal extends TemptGoal {
        private final CapuchinMonkey monkey;

        public MonkeyTemptGoal(CapuchinMonkey monkey, double speedModifier, Predicate<ItemStack> items, boolean canScare) {
            super(monkey, speedModifier, items, canScare);
            this.monkey = monkey;
        }

        @Override
        protected boolean canScare() {
            return super.canScare() && !this.monkey.isTrusting();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animController));
    }

    protected <E extends CapuchinMonkey> PlayState animController(final AnimationState<E> event) {
        event.setControllerSpeed(1);

        if (this.isHopping())
            return event.setAndContinue(HOP_ANIM);

        if (event.isMoving()) {
            event.setControllerSpeed((float) (this.getDeltaMovement().horizontalDistance() * 20));
            return event.setAndContinue(WALK_ANIM);
        }

        if (!this.onGround())
            return event.setAndContinue(STAND_ANIM);

        if (event.isCurrentAnimation(SIT_ANIM))
            return event.setAndContinue(SIT_ANIM);

        if (event.isCurrentAnimation(SIT_UPRIGHT_ANIM))
            return event.setAndContinue(SIT_UPRIGHT_ANIM);

        return this.random.nextBoolean() ? event.setAndContinue(SIT_ANIM) : event.setAndContinue(SIT_UPRIGHT_ANIM);
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
