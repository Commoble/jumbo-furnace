package net.commoble.jumbofurnace;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

public class VanillaRegistryObject<T> implements Supplier<T>
{
	private Supplier<T> delegate;
	
	public static <T> VanillaRegistryObject<T> create(List<Runnable> commonSetupRunnables, ResourceLocation id, Function<ResourceLocation, T> factory)
	{
		VanillaRegistryObject<T> obj = new VanillaRegistryObject<>();
		obj.delegate = () -> {throw new IllegalStateException(String.format("%s accessed before common setup registration", id));};
		commonSetupRunnables.add(() ->
		{
			T t = factory.apply(id);
			obj.delegate = ()->t;
		});
		return obj;
	}

	@Override
	public T get()
	{
		return this.delegate.get();
	}
}
