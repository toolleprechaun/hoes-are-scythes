package net.taylor.hoesarescythes.config;

import java.util.ArrayList;
import java.util.List;

/** POJO for config JSON. */
public final class ModConfig {
    /** If true, only hoes declared here are used. If false, these extend your defaults. */
    public boolean replaceDefaultHoes = false;

    /** Custom hoes (by item id or tag) with their radius. First match wins. */
    public List<HEntry> hoes = new ArrayList<>();

    /** Extra scythable blocks or tags to ADD to your data tag. */
    public List<String> extraScythableBlocks = new ArrayList<>();

    /** Single config entry. One of item or tag may be set. */
    public static final class HEntry {
        public String item;   // e.g. "minecraft:iron_hoe"
        public String tag;    // e.g. "#c:scythes"
        public int radius = 1;
    }
}
