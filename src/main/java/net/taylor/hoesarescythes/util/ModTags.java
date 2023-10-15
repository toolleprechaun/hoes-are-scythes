package net.taylor.hoesarescythes.util;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.taylor.hoesarescythes.HoesAreScythes;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> SCYTHE_BLOCKS =
                createTag("scythe_blocks");

        public static final TagKey<Block> SCYTHE_BLOCKS_DROPPABLE =
                createTag("scythe_blocks_droppable");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, new Identifier(HoesAreScythes.MOD_ID, name));
        }
    }
}
