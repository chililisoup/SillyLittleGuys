package dev.chililisoup.sillylittleguys.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.chililisoup.sillylittleguys.client.model.CapuchinMonkeyModel;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import software.bernie.geckolib.util.RenderUtil;

public class CapuchinMonkeyRenderer extends SillyLittleGuyRenderer<CapuchinMonkey> {
    public CapuchinMonkeyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CapuchinMonkeyModel("capuchin_monkey"));

        addRenderLayer(new CapuchinMonkeyArmorLayer(this));
    }

    private static class CapuchinMonkeyArmorLayer extends ItemArmorGeoLayer<CapuchinMonkey> {
        public CapuchinMonkeyArmorLayer(GeoRenderer<CapuchinMonkey> geoRenderer) {
            super(geoRenderer);
        }

        @Override
        protected @NotNull ModelPart getModelPartForBone(
                GeoBone bone,
                EquipmentSlot slot,
                ItemStack stack,
                CapuchinMonkey monkey,
                HumanoidModel<?> baseModel
        ) {
            if (bone.getName().equals("head"))
                return baseModel.head;

            if (bone.getName().equals("rightArm"))
                return baseModel.rightArm;

            return baseModel.body;
        }

        @Override
        protected ItemStack getArmorItemForBone(GeoBone bone, CapuchinMonkey monkey) {
            if (bone.getName().equals("head"))
                return monkey.getItemBySlot(EquipmentSlot.HEAD);

            if (bone.getName().equals("rightArm"))
                return monkey.getItemBySlot(EquipmentSlot.MAINHAND);

            return null;
        }

        private boolean shouldRenderForBoneSuper(CapuchinMonkey monkey, GeoBone bone, ItemStack stack) {
            return bone.getName().equals("head") && (stack.getItem() instanceof ArmorItem ||
                    getModelForItem(bone, EquipmentSlot.HEAD, stack, monkey) instanceof GeoArmorRenderer<?>);
        }

        @Override
        public void renderForBone(
                PoseStack poseStack,
                CapuchinMonkey monkey,
                GeoBone bone,
                RenderType renderType,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                float partialTick,
                int packedLight,
                int packedOverlay
        ) {
            if (!bone.getName().equals("head") && !bone.getName().equals("rightArm"))
                return;

            ItemStack armorStack = getArmorItemForBone(bone, monkey);
            if (armorStack == null)
                return;

            if (shouldRenderForBoneSuper(monkey, bone, armorStack)) {
                super.renderForBone(poseStack, monkey, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                return;
            }

            poseStack.pushPose();
            RenderUtil.translateToPivotPoint(poseStack, bone);

            renderEquipmentItem(
                    poseStack,
                    armorStack,
                    monkey,
                    bufferSource,
                    packedLight,
                    packedOverlay,
                    bone.getName().equals("head") ? ItemDisplayContext.HEAD : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
            );

            poseStack.popPose();
        }

        private void renderEquipmentItem(
                PoseStack poseStack,
                ItemStack stack,
                CapuchinMonkey monkey,
                MultiBufferSource bufferSource,
                int packedLight,
                int packedOverlay,
                ItemDisplayContext displayContext
        ) {
            float scale;
            if (displayContext.equals(ItemDisplayContext.HEAD)) {
                if (stack.getItem() instanceof BlockItem blockItem) {
                    if (blockItem.getBlock() instanceof AbstractSkullBlock skullBlock) {
                        SkullBlock.Type type = skullBlock.getType();
                        SkullModelBase model = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels()).get(type);
                        RenderType renderType = SkullBlockRenderer.getRenderType(type, stack.get(DataComponents.PROFILE));

                        scale = 1.125F;
                        poseStack.scale(scale, scale, scale);
                        poseStack.translate(-0.5f, 0, -0.5f);
                        SkullBlockRenderer.renderSkull(null, 0, 0, poseStack, bufferSource, packedLight, model, renderType);
                        return;
                    }

                    poseStack.translate(0, 0.2, -0.025);
                    scale = 0.575F;
                } else {
                    poseStack.translate(0, 0.2, 0);
                    scale = 0.45F;
                }
            } else {
                if (stack.is(Items.SHIELD)) {
                    poseStack.translate(0.1, -0.575, -0.1);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                } else {
                    poseStack.translate(0, -0.575, 0);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                }
                scale = 0.875F;
            }

            poseStack.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    monkey,
                    stack,
                    displayContext,
                    false,
                    poseStack,
                    bufferSource,
                    monkey.level(),
                    packedLight,
                    packedOverlay,
                    monkey.getId()
            );
        }

        @Override
        protected void prepModelPartForRender(PoseStack poseStack, GeoBone bone, ModelPart sourcePart) {
            float scale = 0.8F;

            sourcePart.setPos(-(bone.getPivotX() - ((bone.getPivotX() * scale) - bone.getPivotX()) / scale),
                    -(bone.getPivotY() - ((bone.getPivotY() * scale) - bone.getPivotY()) / scale),
                    (bone.getPivotZ() - ((bone.getPivotZ() * scale) - bone.getPivotZ()) / scale) - 0.5F);

            sourcePart.xRot = -0.125F;

            poseStack.scale(0.9F, scale, scale);
        }
    }
}
