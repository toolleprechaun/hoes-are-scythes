// net/taylor/hoesarescythes/HoesAreScythes.java

package net.taylor.hoesarescythes;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.taylor.hoesarescythes.config.ConfigManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.taylor.hoesarescythes.config.ConfigReloadListener;

public class HoesAreScythes implements ModInitializer {
	public static final String MOD_ID = "hoes-are-scythes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Hoes Are Scythes...");

		// load or create config file
		ConfigManager.loadOrCreateDefault();
		LOGGER.info("Config loaded from {}", ConfigManager.path());

		// Enable /reload support
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConfigReloadListener());

		// existing registration
		ClearGrass.register();
		LOGGER.info("Hoes Are Scythes is initialized!");
	}
}