package com.reimonsworkshop.namcm;

import com.reimonsworkshop.namcm.item.Items;
import com.reimonsworkshop.namcm.scheduler.ChunkDepleteScheduler;
import com.reimonsworkshop.namcm.scheduler.ParticleBorderScheduler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NAMCMMod implements ModInitializer {
	public static final String MOD_ID = "namcm";
	public static final Logger NAMCM_LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Items.initialize();
		ParticleBorderScheduler.class.getName();
		ChunkDepleteScheduler.class.getName();
	}
}