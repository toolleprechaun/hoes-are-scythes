package net.taylor.hoesarescythes.logic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.taylor.hoesarescythes.config.ConfigManager;
import net.taylor.hoesarescythes.HoesAreScythes;
import net.taylor.hoesarescythes.util.ModTags;

public final class ScythePredicate {
    private ScythePredicate() {}

    /** True if the state should be cleared by the scythe logic. */
    public static boolean isScythable(BlockState state) {
        // 1) Your data tag (base list; supports required:false entries)
        if (state.isIn(ModTags.Blocks.SCYTHE_BLOCKS)) return true;

        // 2) Runtime extensions from config
        for (String entry : ConfigManager.get().extraScythableBlocks) {
            if (entry == null || entry.isEmpty()) continue;

            // Tag entry: "#namespace:path"
            if (entry.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(entry.substring(1));
                if (tagId == null) {
                    HoesAreScythes.LOGGER.debug("Ignoring invalid scythable tag '{}'", entry);
                    continue;
                }
                TagKey<Block> tagKey = TagKey.of(RegistryKeys.BLOCK, tagId);
                if (state.isIn(tagKey)) return true;
                continue;
            }

            // Single block id: "namespace:path"
            Identifier id = Identifier.tryParse(entry);
            if (id == null) {
                HoesAreScythes.LOGGER.debug("Ignoring invalid scythable id '{}'", entry);
                continue;
            }
            if (Registries.BLOCK.getId(state.getBlock()).equals(id)) return true;
        }

        return false;
    }
}