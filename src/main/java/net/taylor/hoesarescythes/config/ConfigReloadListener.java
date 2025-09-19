package net.taylor.hoesarescythes.config;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.taylor.hoesarescythes.HoesAreScythes;

public final class ConfigReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return Identifier.of(HoesAreScythes.MOD_ID, "config_reload");
    }

    @Override
    public void reload(ResourceManager manager) {
        HoesAreScythes.LOGGER.info("[HoesAreScythes] Reloading config from {}", ConfigManager.path());
        ConfigManager.loadOrCreateDefault();
    }
}