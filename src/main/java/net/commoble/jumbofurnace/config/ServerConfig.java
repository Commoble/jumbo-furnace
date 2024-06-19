package net.commoble.jumbofurnace.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;

public record ServerConfig(BooleanValue allowShearing)
{	
	public static ServerConfig create(ModConfigSpec.Builder builder)
	{
		builder.push("Construction Settings");
		BooleanValue allowShearing = builder
			.comment("Shearable: Allow jumbo furnaces to be cleanly dismantled with shears")
			.translation("jumbofurnace.shearable")
			.define("shearable", true);
		builder.pop();
		
		return new ServerConfig(allowShearing);
	}
}
