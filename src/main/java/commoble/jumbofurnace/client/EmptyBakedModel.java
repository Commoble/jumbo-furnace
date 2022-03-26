package commoble.jumbofurnace.client;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class EmptyBakedModel implements BakedModel
{
	private static final List<BakedQuad> NO_QUADS = ImmutableList.of();
	
	private final TextureAtlasSprite oarticleTexture;
	
	public EmptyBakedModel(TextureAtlasSprite particleTexture)
	{
		this.oarticleTexture = particleTexture;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand)
	{
		return NO_QUADS;
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean usesBlockLight()
	{
		return false;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return this.oarticleTexture;
	}

	@Override
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}
	
}
