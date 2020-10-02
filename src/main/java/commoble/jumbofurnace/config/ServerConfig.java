package commoble.jumbofurnace.config;

import commoble.jumbofurnace.config.ConfigHelper.ConfigValueListener;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig
{	
	public final ConfigValueListener<Integer> jumboFurnaceCookTime;
	
	public final ConfigValueListener<Boolean> allowShearing;
	
	public ServerConfig(ForgeConfigSpec.Builder builder, ConfigHelper.Subscriber subscriber)
	{
		builder.push("Cooking Settings");
		this.jumboFurnaceCookTime = subscriber.subscribe(builder
			.comment("Cook Time: Time in ticks needed for one cooking cycle")
			.translation("jumbofurnace.cooktime")
			.defineInRange("cooktime", 200, 1, Integer.MAX_VALUE));
		builder.pop();
		
		builder.push("Construction Settings");
		this.allowShearing = subscriber.subscribe(builder
			.comment("Shearable: Allow jumbo furnaces to be cleanly dismantled with shears")
			.translation("jumbofurnace.shearable")
			.define("shearable", true));
		builder.pop();
		
	}
}
