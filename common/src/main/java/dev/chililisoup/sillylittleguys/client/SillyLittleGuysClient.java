package dev.chililisoup.sillylittleguys.client;

import com.mojang.datafixers.util.Pair;
import dev.chililisoup.sillylittleguys.client.reg.ModEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class SillyLittleGuysClient {
    public static void init() {
        ModEntityRenderers.init();
    }

    private static boolean comesAfter(Item newEntry, Item existingEntry) {
        return BuiltInRegistries.ITEM.getKey(newEntry).compareTo(BuiltInRegistries.ITEM.getKey(existingEntry)) > 0;
    }

    public static @Nullable Pair<ItemStack, Boolean> getEggPlacement(Item eggItem, List<ItemStack> parentEntries) {
        Iterator<ItemStack> iterator = parentEntries.iterator();
        if (!iterator.hasNext())
            return null;

        ItemStack existing = iterator.next();
        while (iterator.hasNext() && !(existing.getItem() instanceof SpawnEggItem)) existing = iterator.next();

        if (existing.getItem() instanceof SpawnEggItem && !comesAfter(eggItem, existing.getItem()))
            return Pair.of(existing, false);

        while (iterator.hasNext()) {
            ItemStack next = iterator.next();
            if (!(next.getItem() instanceof SpawnEggItem)) continue;
            if (comesAfter(eggItem, next.getItem())) existing = next;
            else break;
        }

        return Pair.of(existing, true);
    }
}
