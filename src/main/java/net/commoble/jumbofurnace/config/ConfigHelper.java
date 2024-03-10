package net.commoble.jumbofurnace.config;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;


/**
 * Helper for creating configs and defining complex objects in configs 
 */
public record ConfigHelper(ModConfigSpec.Builder builder)
{
	static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Register a config using a default config filename for your mod.
	 * @param <T> The class of your config implementation
	 * @param configType Forge config type:
	 * <ul>
	 * <li>SERVER configs are defined by the server and synced to clients; individual configs are generated per-save. Filename will be modid-server.toml
	 * <li>COMMON configs are definable by both server and clients and not synced (they may have different values). Filename will be modid-client.toml
	 * <li>CLIENT configs are defined by clients and not used on the server. Filename will be modid-client.toml.
	 * </ul>
	 * @param configFactory A constructor or factory for your config class
	 * @return An instance of your config class
	 */
	public static <T> T register(
		final ModConfig.Type configType,
		final Function<ModConfigSpec.Builder, T> configFactory)
	{
		return register(configType, configFactory, null);
	}
	
	/**
	 * Register a config using a custom filename.
	 * @param <T> Your config class
	 * @param configType Forge config type:
	 * <ul>
	 * <li>SERVER configs are defined by the server and synced to clients; individual configs are generated per-save.
	 * <li>COMMON configs are definable by both server and clients and not synced (they may have different values)
	 * <li>CLIENT configs are defined by clients and not used on the server
	 * </ul>
	 * @param configFactory A constructor or factory for your config class
	 * @param configName Name of your config file. Supports subfolders, e.g. "yourmod/yourconfig".
	 * @return An instance of your config class
	 */
	public static <T> T register(
		final ModConfig.Type configType,
		final Function<ModConfigSpec.Builder, T> configFactory,
		final @Nullable String configName)
	{
		final ModLoadingContext modContext = ModLoadingContext.get();
		final org.apache.commons.lang3.tuple.Pair<T, ModConfigSpec> entry = new ModConfigSpec.Builder()
			.configure(configFactory);
		final T config = entry.getLeft();
		final ModConfigSpec spec = entry.getRight();
		if (configName == null)
		{
			modContext.registerConfig(configType,spec);
		}
		else
		{
			modContext.registerConfig(configType, spec, configName + ".toml");
		}
		
		return config;
	}
}
