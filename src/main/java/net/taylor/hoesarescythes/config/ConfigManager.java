package net.taylor.hoesarescythes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Reads/writes config/hoesarescythes.json
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "hoesarescythes.json";

    private static ModConfig CURRENT = new ModConfig();

    private ConfigManager() {}

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    // Returns the last loaded config (never null).
    public static ModConfig get() {
        return CURRENT;
    }

    // Create a default file if missing, then load it into memory.
    public static void loadOrCreateDefault() {
        Path p = path();
        if (!Files.exists(p)) {
            writeDefault(p);
        }
        try {
            String json = Files.readString(p);
            ModConfig cfg = GSON.fromJson(json, ModConfig.class);
            if (cfg == null) cfg = new ModConfig();
            CURRENT = cfg;
        } catch (Exception e) {
            System.err.println("[HoesAreScythes] Failed to read config, keeping previous: " + e.getMessage());
        }
    }

    private static void writeDefault(Path p) {
        try {
            ModConfig def = new ModConfig();
            def.hoes.add(entry("minecraft:wooden_hoe", 1));
            def.hoes.add(entry("minecraft:stone_hoe", 1));
            def.hoes.add(entry("minecraft:iron_hoe", 2));
            def.hoes.add(entry("minecraft:diamond_hoe", 3));
            def.hoes.add(entry("minecraft:netherite_hoe", 4));

            Files.createDirectories(p.getParent());
            Files.writeString(p, GSON.toJson(def));
        } catch (IOException ignored) {}
    }

    private static ModConfig.HEntry entry(String id, int radius) {
        ModConfig.HEntry e = new ModConfig.HEntry();
        e.item = id;
        e.radius = radius;
        return e;
    }
}