package net.taylor.hoesarescythes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoesAreScythes implements ModInitializer {
	public static final String MOD_ID = "hoes-are-scythes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Hoes Are Scythes...");
		ClearGrass.register();
		LOGGER.info("Hoes Are Scythes is initialized!");
		LOGGER.info("MC version: {}", SharedConstants.getGameVersion().name());


	}
}