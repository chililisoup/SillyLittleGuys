package dev.chililisoup.sillylittleguys.client.model;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class CapuchinMonkeyModel extends SillyLittleGuyModel<CapuchinMonkey> {
    private final Map<CapuchinMonkey.Variant, ResourceLocation> variantTextures;

    public CapuchinMonkeyModel(String name) {
        super(name);

        this.variantTextures = Arrays.stream(CapuchinMonkey.Variant.values()).collect(Collectors.toUnmodifiableMap(
                variant -> variant,
                variant -> this.buildFormattedTexturePath(SillyLittleGuys.loc(name + "_" + variant.getSerializedName()))
        ));
    }

    @Override
    public ResourceLocation getTextureResource(CapuchinMonkey monkey, @Nullable GeoRenderer<CapuchinMonkey> renderer) {
        return variantTextures.get(monkey.getVariant());
    }

    @Override
    public void setCustomAnimations(CapuchinMonkey monkey, long instanceId, AnimationState<CapuchinMonkey> animationState) {
        super.setCustomAnimations(monkey, instanceId, animationState);

        ItemStack stack = monkey.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack.isEmpty() || stack.is(Items.SHIELD)) return;

        GeoBone rightArm = this.getAnimationProcessor().getBone("rightArm");
        if (rightArm == null) return;

        Vec3 velocity = monkey.getDeltaMovement();
        if ((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2 >= 0.015) return;

        rightArm.setPosX(rightArm.getPosX() - 1.5F);
        rightArm.setPosY(rightArm.getPosY() + 0.75F);
        rightArm.setPosZ(rightArm.getPosZ() + 1.75F);

        rightArm.setRotX(rightArm.getRotX() + 1.0F);
        rightArm.setRotY(rightArm.getRotY() - 0.165F);
        rightArm.setRotZ(rightArm.getRotZ() - 0.88F);
    }
}
