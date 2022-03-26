package commoble.jumbofurnace.client;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

// see https://en.wikipedia.org/wiki/Hypercube
public class OrthodimensionalHyperfurnaceRenderer extends BlockEntityWithoutLevelRenderer
{
	public static final Supplier<OrthodimensionalHyperfurnaceRenderer> INSTANCE = Lazy.of(() ->
	{
		Minecraft mc = Minecraft.getInstance();
		return new OrthodimensionalHyperfurnaceRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
	});
	
	@SuppressWarnings("deprecation")
	private static final Material MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft:block/furnace_front"));

	// hypercube has 16 vertices, 32 edges, 24 faces
	
	public static final Vec3[] VERTICES = {
		// "outer" vertices
		// rendering vertices in clockwise order looking inward renders an outward-facing face
		
		// front
		new Vec3(0D,0D,0D),	//0
		new Vec3(0D,1D,0D),	//1
		new Vec3(1D,1D,0D),	//2
		new Vec3(1D,0D,0D),	//3
		
		// back
		new Vec3(1D,0D,1D),	//4
		new Vec3(1D,1D,1D),	//5
		new Vec3(0D,1D,1D),	//6
		new Vec3(0D,0D,1D),	//7
		
		// "inner" vertices
		// front
		new Vec3(0.25D,0.25D,0.25D),	//8
		new Vec3(0.25D,0.75D,0.25D),	//9
		new Vec3(0.75D,0.75D,0.25D),	//10
		new Vec3(0.75D,0.25D,0.25D),	//11
		
		// back
		new Vec3(0.75D,0.25D,0.75D),	//12
		new Vec3(0.75D,0.75D,0.75D),	//13
		new Vec3(0.25D,0.75D,0.75D),	//14
		new Vec3(0.25D,0.25D,0.75D)		//15
	};
	
	public static class Face
	{
		public final Vec3[] vertices;
		public final Vec3 normal;
		public final Vec3 reverseNormal;
		
		public Face(Vec3 a, Vec3 b, Vec3 c, Vec3 d)
		{
			// can't do this.vertices = {a,b,c,d} because java
			Vec3[] tempVertices = {a,b,c,d};
			this.vertices = tempVertices;

			// need to calculate normals so vertex can have sided lighting
			// to get the normal for a vertex v1 connected to v2 and v3,
			// we take the cross product (v2 - v1) x (v3 - v1)
			// for a given quad, all four vertices should have the same normal, so we only need to calculate one of them
			// and reverse it for the reverse quad
			
			this.normal = this.vertices[1].subtract(this.vertices[0]).cross(this.vertices[3].subtract(this.vertices[0]));
			this.reverseNormal = this.normal.multiply(-1,-1,-1);
		}
	}
	
	// vertices should be declared in clockwise order
	public static Face face(int a, int b, int c, int d)
	{
		return new Face(VERTICES[a], VERTICES[b], VERTICES[c], VERTICES[d]);
	}
	
	// vertices should be clockwise from bottom-left of front of furnace
	public static final Face[] FACES = {
		// outer faces
		// outer faces binned as transparent textures have sorting issues
//		face(0,1,2,3, FaceType.OUTER),	// -z
//		face(4,5,6,7, FaceType.OUTER),	// +z
//		face(7,6,1,0, FaceType.OUTER),	// -x
//		face(3,2,5,4, FaceType.OUTER),	// +x
//		face(0,3,4,7, FaceType.OUTER),	// -y
//		face(2,1,6,5, FaceType.OUTER),	// +y
		
		// inner faces
		face(8,9,10,11),	//-z
		face(12,13,14,15),	//+z
		face(15,14,9,8),	//-x
		face(11,10,13,12),	//+x
		face(8,11,12,15),	//-y
		face(10,9,14,13),	//+y
		
		// upper conjoining faces
		face(1,9,10,2),		//-z
		face(5,6,14,13),	//+z
		face(1,9,14,6),		//-x
		face(5,13,10,2),	//+x
		
		// middle conjoining faces
		face(1,9,8,0),	//-x,-z
		face(2,10,11,3),//+x,-z
		face(5,13,12,4),//+x,+z
		face(6,14,15,7),//-x,+z
		
		// bottom conjoining faces
		face(3,11,8,0), //-z
		face(7,15,12,4),//+z
		face(0,8,15,7),	//-x
		face(4,12,11,3)	//+x
		
	};

	public OrthodimensionalHyperfurnaceRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models)
	{
		super(dispatcher, models);
	}
	
	//render
	@Override
	public void renderByItem(ItemStack stack, TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay)
	{		
		TextureAtlasSprite texture = MATERIAL.sprite();
		VertexConsumer vertexBuilder = MATERIAL.buffer(buffer, RenderType::entitySolid, stack.hasFoil());
		
		float minU = texture.getU0();
		float maxU = texture.getU1();
		float minV = texture.getV0();
		float maxV = texture.getV1();
		
		matrixStack.pushPose();
		
		PoseStack.Pose matrixEntry = matrixStack.last();
		
		int faces = FACES.length;
		for (int i=0; i<faces; i++)
		{
			Face face = FACES[i];
			Vec3[] vertices = face.vertices;
			Vec3 normal = face.normal;
			Vec3 reverseNormal = face.reverseNormal;
			
			putVertex(matrixEntry, vertexBuilder, vertices[0], minU, maxV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[1], minU, minV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[2], maxU, minV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[3], maxU, maxV, combinedLight, normal);


			putVertex(matrixEntry, vertexBuilder, vertices[3], maxU, maxV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[2], maxU, minV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[1], minU, minV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[0], minU, maxV, combinedLight, reverseNormal);
		}
		
		matrixStack.popPose();
	}
	
	private static void putVertex(PoseStack.Pose matrixEntryIn, VertexConsumer bufferIn, Vec3 pos, float texU, float texV, int packedLight, Vec3 normal)
	{
		bufferIn.vertex(matrixEntryIn.pose(), (float)pos.x(), (float)pos.y(), (float)pos.z())
			.color(1F, 1F, 1F, 1F)
			.uv(texU, texV)
			.overlayCoords(0, 10)
			.uv2(packedLight)
			.normal(matrixEntryIn.normal(), (float)normal.x, (float)normal.y, (float)normal.z)
			.endVertex();
	}
	
}
