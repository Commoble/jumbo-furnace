package commoble.jumbofurnace.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

// see https://en.wikipedia.org/wiki/Hypercube
public class OrthodimensionalHyperfurnaceRenderer extends ItemStackTileEntityRenderer
{
	@SuppressWarnings("deprecation")
	private static final RenderMaterial MATERIAL = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("minecraft:block/furnace_front"));

	// hypercube has 16 vertices, 32 edges, 24 faces
	
	public static final Vector3d[] VERTICES = {
		// "outer" vertices
		// rendering vertices in clockwise order looking inward renders an outward-facing face
		
		// front
		new Vector3d(0D,0D,0D),	//0
		new Vector3d(0D,1D,0D),	//1
		new Vector3d(1D,1D,0D),	//2
		new Vector3d(1D,0D,0D),	//3
		
		// back
		new Vector3d(1D,0D,1D),	//4
		new Vector3d(1D,1D,1D),	//5
		new Vector3d(0D,1D,1D),	//6
		new Vector3d(0D,0D,1D),	//7
		
		// "inner" vertices
		// front
		new Vector3d(0.25D,0.25D,0.25D),	//8
		new Vector3d(0.25D,0.75D,0.25D),	//9
		new Vector3d(0.75D,0.75D,0.25D),	//10
		new Vector3d(0.75D,0.25D,0.25D),	//11
		
		// back
		new Vector3d(0.75D,0.25D,0.75D),	//12
		new Vector3d(0.75D,0.75D,0.75D),	//13
		new Vector3d(0.25D,0.75D,0.75D),	//14
		new Vector3d(0.25D,0.25D,0.75D)		//15
	};
	
	public static class Face
	{
		public final Vector3d[] vertices;
		public final Vector3d normal;
		public final Vector3d reverseNormal;
		
		public Face(Vector3d a, Vector3d b, Vector3d c, Vector3d d)
		{
			// can't do this.vertices = {a,b,c,d} because java
			Vector3d[] tempVertices = {a,b,c,d};
			this.vertices = tempVertices;

			// need to calculate normals so vertex can have sided lighting
			// to get the normal for a vertex v1 connected to v2 and v3,
			// we take the cross product (v2 - v1) x (v3 - v1)
			// for a given quad, all four vertices should have the same normal, so we only need to calculate one of them
			// and reverse it for the reverse quad
			
			this.normal = this.vertices[1].subtract(this.vertices[0]).crossProduct(this.vertices[3].subtract(this.vertices[0]));
			this.reverseNormal = this.normal.mul(-1,-1,-1);
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
	
	//render
	@Override
	public void func_239207_a_(ItemStack stack, TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay)
	{		
		TextureAtlasSprite texture = MATERIAL.getSprite();
		IVertexBuilder vertexBuilder = MATERIAL.getItemRendererBuffer(buffer, RenderType::getEntitySolid, stack.hasEffect());
		
		float minU = texture.getMinU();
		float maxU = texture.getMaxU();
		float minV = texture.getMinV();
		float maxV = texture.getMaxV();
		
		matrixStack.push();
		
		MatrixStack.Entry matrixEntry = matrixStack.getLast();
		
		int faces = FACES.length;
		for (int i=0; i<faces; i++)
		{
			Face face = FACES[i];
			Vector3d[] vertices = face.vertices;
			Vector3d normal = face.normal;
			Vector3d reverseNormal = face.reverseNormal;
			
			putVertex(matrixEntry, vertexBuilder, vertices[0], minU, maxV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[1], minU, minV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[2], maxU, minV, combinedLight, normal);
			putVertex(matrixEntry, vertexBuilder, vertices[3], maxU, maxV, combinedLight, normal);


			putVertex(matrixEntry, vertexBuilder, vertices[3], maxU, maxV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[2], maxU, minV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[1], minU, minV, combinedLight, reverseNormal);
			putVertex(matrixEntry, vertexBuilder, vertices[0], minU, maxV, combinedLight, reverseNormal);
		}
		
		matrixStack.pop();
	}
	
	private static void putVertex(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, Vector3d pos, float texU, float texV, int packedLight, Vector3d normal)
	{
		bufferIn.pos(matrixEntryIn.getMatrix(), (float)pos.getX(), (float)pos.getY(), (float)pos.getZ())
			.color(1F, 1F, 1F, 1F)
			.tex(texU, texV)
			.overlay(0, 10)
			.lightmap(packedLight)
			.normal(matrixEntryIn.getNormal(), (float)normal.x, (float)normal.y, (float)normal.z)
			.endVertex();
	}
	
}
