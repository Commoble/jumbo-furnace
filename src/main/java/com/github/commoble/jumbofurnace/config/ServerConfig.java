package com.github.commoble.jumbofurnace.config;

import com.github.commoble.jumbofurnace.config.ConfigHelper.ConfigValueListener;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig
{
	public static ServerConfig INSTANCE;
	
	// called from jumbo furnace mod constructor
	public static void initConfig()
	{
		INSTANCE = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::new);
	}
	
	public final ConfigValueListener<Integer> jumboFurnaceCookTime;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Cooking Settings");
		this.jumboFurnaceCookTime = subscriber.subscribe(builder
			.comment("Cook Time")
			.translation("jumbofurnace.cooktime")
			.defineInRange("cooktime", 200, 1, Integer.MAX_VALUE));
		builder.pop();
	}
}
