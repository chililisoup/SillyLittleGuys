package dev.chililisoup.sillylittleguys.reg;

import dev.chililisoup.sillylittleguys.SillyLittleGuys;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> MONKEY_FOOD = create("monkey_food");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, SillyLittleGuys.loc(name));
    }
}

