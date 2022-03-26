package commoble.jumbofurnace.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public record ServerConfig(IntValue jumboFurnaceCookTime, BooleanValue allowShearing)
{	
	public static ServerConfig create(ForgeConfigSpec.Builder builder)
	{
		builder.push("Cooking Settings");
		IntValue jumboFurnaceCookTime = builder
			.comment("Cook Time: Time in ticks needed for one cooking cycle")
			.translation("jumbofurnace.cooktime")
			.defineInRange("cooktime", 200, 1, Integer.MAX_VALUE);
		builder.pop();
		
		builder.push("Construction Settings");
		BooleanValue allowShearing = builder
			.comment("Shearable: Allow jumbo furnaces to be cleanly dismantled with shears")
			.translation("jumbofurnace.shearable")
			.define("shearable", true);
		builder.pop();
		
		return new ServerConfig(jumboFurnaceCookTime, allowShearing);
	}
}
