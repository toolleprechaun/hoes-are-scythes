package net.taylor.hoesarescythes;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoesAreScythes implements ModInitializer {
	public static final String MOD_ID = "hoes-are-scythes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing HoesAreScythes mod...");
		ClearGrass.register();
		LOGGER.info("HoesAreScythes mod initialized!");
	}
}