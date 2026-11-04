package me.eclipxia.customcapes;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * CustomCapes plugin entry point
 *
 * @author eclipxia (original plugin by FelekDevYT)
 */
public class CustomCapes extends Plugin {
	
	@Override
	public void onLoad() {
        RusherHackAPI.getModuleManager().registerFeature(new CustomCapeModule());
        RusherHackAPI.getCommandManager().registerFeature(new CapeCommand());

	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("CustomCapes unloaded!");
	}
	
}