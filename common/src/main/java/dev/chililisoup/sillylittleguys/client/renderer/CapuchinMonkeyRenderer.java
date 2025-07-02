package dev.chililisoup.sillylittleguys.client.renderer;

import dev.chililisoup.sillylittleguys.client.model.CapuchinMonkeyModel;
import dev.chililisoup.sillylittleguys.entity.CapuchinMonkey;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CapuchinMonkeyRenderer extends SillyLittleGuyRenderer<CapuchinMonkey> {
    public CapuchinMonkeyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CapuchinMonkeyModel("capuchin_monkey"));
    }
}
