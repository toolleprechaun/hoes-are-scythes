package net.taylor.hoesarescythes.logic;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.taylor.hoesarescythes.config.ConfigManager;
import net.taylor.hoesarescythes.config.ModConfig;

/** Returns the scythe radius for an item, using the JSON config first, then defaults. */
public final class RadiusResolver {
    private RadiusResolver() {}

    /** @return radius (>=0). 0 means “not a scythe” and disables the AoE. */
    public static int getRadius(ItemStack stack) {
        if (stack == null) return 0;
        Item item = stack.getItem();
        if (item == null) return 0;

        // 1) Config-defined entries (first match wins)
        ModConfig cfg = ConfigManager.get();
        for (ModConfig.HEntry e : cfg.hoes) {
            if (e.item != null && idEquals(item, e.item)) {
                return clamp(e.radius);
            }
            if (e.tag != null && e.tag.startsWith("#") && isInItemTag(stack, e.tag.substring(1))) {
                return clamp(e.radius);
            }
        }

        // 2) Defaults (only if not replacing)
        if (!cfg.replaceDefaultHoes) {
            var id = Registries.ITEM.getId(item);
            String s = id.toString();
            if (s.endsWith("_hoe")) {
                if (s.contains("wooden") || s.contains("stone")) return 1;
                if (s.contains("iron")) return 2;
                if (s.contains("diamond")) return 3;
                return 4; // netherite or stronger
            }
        }

        return 0;
    }

    private static boolean idEquals(Item item, String idStr) {
        Identifier id = Identifier.tryParse(idStr);
        return id != null && Registries.ITEM.getId(item).equals(id);
    }

    private static boolean isInItemTag(ItemStack stack, String tagIdNoHash) {
        Identifier tagId = Identifier.tryParse(tagIdNoHash);
        if (tagId == null) return false;
        TagKey<net.minecraft.item.Item> key = TagKey.of(RegistryKeys.ITEM, tagId);
        return stack.isIn(key);
    }

    private static int clamp(int r) { return Math.max(0, Math.min(16, r)); }
}