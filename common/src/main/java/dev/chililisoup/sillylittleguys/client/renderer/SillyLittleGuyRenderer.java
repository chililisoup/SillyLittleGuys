package dev.chililisoup.sillylittleguys.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.sillylittleguys.client.model.SillyLittleGuyModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SillyLittleGuyRenderer<T extends Entity&GeoAnimatable> extends GeoEntityRenderer<T> {
    public SillyLittleGuyRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    public SillyLittleGuyRenderer(EntityRendererProvider.Context renderManager, String name) {
        this(renderManager, new SillyLittleGuyModel<>(name));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T entity, BakedGeoModel model, @Nullable RenderType renderType,
                               MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour
    ) {
        if (entity instanceof AgeableMob ageable) {
            if (ageable.isBaby()) poseStack.scale(0.4F, 0.4F, 0.4F);
            else poseStack.scale(1.0F, 1.0F, 1.0F);
        }

        super.actuallyRender(poseStack, entity, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}
