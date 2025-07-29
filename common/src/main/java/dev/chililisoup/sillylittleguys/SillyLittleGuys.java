package dev.chililisoup.sillylittleguys;

import dev.chililisoup.sillylittleguys.reg.ModEntities;
import dev.chililisoup.sillylittleguys.reg.ModEntitySubPredicates;
import dev.chililisoup.sillylittleguys.reg.ModMemoryModuleTypes;
import dev.chililisoup.sillylittleguys.reg.ModSensorTypes;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SillyLittleGuys {
    public static final String MOD_ID = "sillylittleguys";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        ModMemoryModuleTypes.init();
        ModSensorTypes.init();
        ModEntities.init();
        ModEntitySubPredicates.init();
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
