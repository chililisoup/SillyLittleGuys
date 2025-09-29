package dev.chililisoup.sillylittleguys.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModItemTags;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

public class CapuchinMonkey extends TamableAnimal implements GeoEntity, VariantHolder<CapuchinMonkey.Variant> {
    /* TODO:

    - Sounds
    - Add cool mechanics
    - Higher jump height w/ anim (similar to frog leaping?)
    - Dancing
    - Fighting??
    - Drop items they don't care about?

    */

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HOPPING = SynchedEntityData.defineId(CapuchinMonkey.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDimensions HOPPING_DIMENSIONS = EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.35F);

    protected static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit");
    protected static final RawAnimation SIT_HOLD_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.sit_hold");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.walk");
    protected static final RawAnimation STAND_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.stand");
    protected static final RawAnimation STAND_HOLD_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.stand_hold");
    protected static final RawAnimation HOP_ANIM = RawAnimation.begin().thenLoop("animation.capuchin_monkey.hop");
    protected static final RawAnimation EQUIP_ANIM = RawAnimation.begin().then("animation.capuchin_monkey.equip", Animation.LoopType.PLAY_ONCE);
    protected static final RawAnimation UNEQUIP_ANIM = RawAnimation.begin().then("animation.capuchin_monkey.unequip", Animation.LoopType.PLAY_ONCE);
    protected static final RawAnimation CONSUME_ANIM = RawAnimation.begin().then("animation.capuchin_monkey.consume", Animation.LoopType.PLAY_ONCE);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public CapuchinMonkey(EntityType<? extends CapuchinMonkey> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NotNull Brain.Provider<CapuchinMonkey> brainProvider() {
        return CapuchinMonkeyAi.brainProvider();
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
        if (spawnGroupData == null) spawnGroupData = new CapuchinMonkeyGroupData(
                0.3F,
                Util.getRandom(Variant.values(), level.getRandom())
        );
        this.setVariant(((CapuchinMonkeyGroupData) spawnGroupData).getGroupVariant());

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

    public boolean isTrusting() {
        return this.getBrain().hasMemoryValue(MemoryModuleType.LIKED_PLAYER);
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && this.isTrusting();
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal == this) return false;
        if (!this.isTrusting()) return false;
        if (!(otherAnimal instanceof CapuchinMonkey monkey)) return false;
        if (!monkey.isTrusting()) return false;
        if (monkey.isInSittingPose()) return false;
        return this.isInLove() && monkey.isInLove();
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.isEmpty()) {
            if (!player.isShiftKeyDown() && this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                if (this.restrictPlayerModifyItems()) return InteractionResult.PASS;

                Brain<CapuchinMonkey> brain = this.getBrain();
                if (!this.isTrusting()) {
                    if (this.isBaby() || !this.isFood(itemStack))
                        return InteractionResult.PASS;

                    if (!CapuchinMonkeyAi.tryBeginTrustingActivity(brain, player))
                        return InteractionResult.PASS;
                }

                itemStack.consume(1, player);
                this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.copyWithCount(1));

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            ItemStack hat;
            if (
                    this.isTrusting() &&
                    itemStack.is(Items.SHEARS) &&
                    !(hat = this.getItemBySlot(EquipmentSlot.HEAD)).isEmpty() &&
                    !EnchantmentHelper.has(hat, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
            ) {
                if (this.restrictPlayerModifyItems())
                    return InteractionResult.PASS;

                this.level().playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.gameEvent(GameEvent.SHEAR, player);

                if (!this.level().isClientSide()) {
                    this.spawnAtLocation(hat);
                    this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    itemStack.hurtAndBreak(1, player, getSlotForHand(hand));
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        } else if (player.isShiftKeyDown() && this.isTrusting()) {
            ItemStack heldStack =  this.getItemBySlot(EquipmentSlot.MAINHAND);

            if (!heldStack.isEmpty()) {
                if (!this.level().isClientSide()) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    player.setItemInHand(hand, heldStack);
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        InteractionResult interactionResult = super.mobInteract(player, hand);
        if (!interactionResult.consumesAction() && this.isOwnedBy(player)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }

        return interactionResult;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source))
            return false;

        if (!this.level().isClientSide)
            this.setOrderedToSit(false);

        return super.hurt(source, amount);
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
    public void handleEntityEvent(byte id) {
        if (id == 41) this.spawnTrustingParticles(true);
        else if (id == 40) this.spawnTrustingParticles(false);
        else super.handleEntityEvent(id);
    }

    public void spawnTrustingParticles(boolean trusting) {
        // Might make unique some day
        this.spawnTamingParticles(trusting);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItemTags.MONKEY_FOOD);
    }

    public static boolean checkCapuchinMonkeySpawnRules(EntityType<CapuchinMonkey> monkey, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return random.nextInt(3) != 0;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        CapuchinMonkey monkey = ModEntities.CAPUCHIN_MONKEY.get().create(level);
        if (monkey != null && otherParent instanceof CapuchinMonkey otherParentMonkey) {
            this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER).ifPresent(playerUUID -> {
                monkey.setOwnerUUID(playerUUID);
                monkey.setTame(true, true);
                monkey.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, playerUUID);
            });

            monkey.setVariant(this.random.nextBoolean() ? this.getVariant() : otherParentMonkey.getVariant());
        }

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
    public void hurtArmor(DamageSource damageSource, float damageAmount) {
        this.hurtHelmet(damageSource, damageAmount);
    }

    @Override
    public void hurtHelmet(DamageSource damageSource, float damageAmount) {
        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.HEAD);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(stack);
        return equipmentSlot == EquipmentSlot.HEAD && this.getItemBySlot(equipmentSlot).isEmpty();
    }

    public boolean isHoldingFood() {
        return this.isFood(this.getMainHandItem());
    }

    public boolean restrictPlayerModifyItems() {
        Brain<CapuchinMonkey> brain = this.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.IS_PANICKING) ||
                brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) ||
                brain.hasMemoryValue(MemoryModuleType.ADMIRING_ITEM) ||
                brain.hasMemoryValue(ModMemoryModuleTypes.IS_PRIVATELY_EATING.get());
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
        controllers.add(new AnimationController<>(this, "arm_controller", 5, state -> PlayState.STOP)
                .triggerableAnim("equip", EQUIP_ANIM)
                .triggerableAnim("unequip", UNEQUIP_ANIM)
                .triggerableAnim("consume", CONSUME_ANIM)
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

        return event.setAndContinue(this.isInSittingPose() || !this.isTame() ?
                (this.getMainHandItem().isEmpty() ? SIT_ANIM : SIT_HOLD_ANIM) :
                (this.getMainHandItem().isEmpty() ? STAND_ANIM : STAND_HOLD_ANIM)
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

    public static class CapuchinMonkeyGroupData extends AgeableMobGroupData {
        private final Variant groupVariant;

        public CapuchinMonkeyGroupData(float babySpawnChance, Variant groupVariant) {
            super(babySpawnChance);
            this.groupVariant = groupVariant;
        }

        public Variant getGroupVariant() {
            return this.groupVariant;
        }
    }
}
