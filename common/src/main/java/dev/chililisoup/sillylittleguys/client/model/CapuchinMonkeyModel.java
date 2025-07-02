package dev.chililisoup.sillylittleguys.client.model;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
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
}
