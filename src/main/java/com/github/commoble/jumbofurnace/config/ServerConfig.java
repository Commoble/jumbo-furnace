package com.github.commoble.jumbofurnace.config;

import com.github.commoble.jumbofurnace.config.ConfigHelper.ConfigValueListener;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{	
	public final ConfigValueListener<Integer> jumboFurnaceCookTime;
	public final ConfigValueListener<Integer> maxSimultaneousRecipes;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Cooking Settings");
		this.jumboFurnaceCookTime = subscriber.subscribe(builder
			.comment("Cook Time")
			.translation("jumbofurnace.cooktime")
			.defineInRange("cooktime", 200, 1, Integer.MAX_VALUE));
		this.maxSimultaneousRecipes = subscriber.subscribe(builder
			.comment("Maximum recipes the Jumbo Furnace is able to process simultaneously")
			.translation("jumbofurnace.max_simultaneous_recipes")
			.defineInRange("max_simultaneous_recipes", 16, 1, Integer.MAX_VALUE));
		builder.pop();
	}
}
