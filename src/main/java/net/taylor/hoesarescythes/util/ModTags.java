// net/taylor/hoesarescythes/util/ModTags.java

package net.taylor.hoesarescythes.util;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.taylor.hoesarescythes.HoesAreScythes;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> SCYTHE_BLOCKS = createTag();

        private static TagKey<Block> createTag() {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(HoesAreScythes.MOD_ID, "scythe_blocks"));
        }
    }
}