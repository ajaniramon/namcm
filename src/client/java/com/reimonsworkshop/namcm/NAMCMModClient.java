package com.reimonsworkshop.namcm;

import net.fabricmc.api.ClientModInitializer;

import static com.reimonsworkshop.namcm.NAMCMMod.NAMCM_LOGGER;

public class NAMCMModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		NAMCM_LOGGER.info("NAMCM Loaded. Not another chunk mod.");
	}
}