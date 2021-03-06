/*
 * Copyright (c) 2017 NOVA, All rights reserved.
 * This library is free software, licensed under GNU Lesser General Public License version 3
 *
 * This file is part of NOVA.
 *
 * NOVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NOVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NOVA.  If not, see <http://www.gnu.org/licenses/>.
 */

package nova.core.wrapper.mc.forge.v1_11_2.wrapper.render.backward;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import nova.core.render.Color;
import nova.core.render.model.Face;
import nova.core.render.model.MeshModel;
import nova.core.render.model.Model;
import nova.core.render.model.Vertex;
import nova.core.util.Direction;
import nova.core.util.math.MatrixStack;
import nova.core.util.math.TransformUtil;
import nova.core.util.math.Vector3DUtil;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.DirectionConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.assets.AssetConverter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * @author ExE Boss
 */
public class BWBakedModel extends MeshModel {

	@SuppressWarnings("deprecation")
	public final IBakedModel wrapped;

	public final VertexFormat format;

	public final Optional<IBlockState> blockState;

	public final long rand;

	public BWBakedModel(IBakedModel wrapped) {
		this(wrapped, DefaultVertexFormats.ITEM);
	}

	public BWBakedModel(IBakedModel wrapped, VertexFormat format) {
		this(wrapped, format, Optional.empty(), 0);
	}

	public BWBakedModel(IBakedModel wrapped, VertexFormat format, Optional<IBlockState> state, long rand) {
		this.wrapped = wrapped;
		this.format = format;
		this.matrix.translate(-0.5, -0.5, -0.5);
		this.blockState = state;
		this.rand = rand;

		if (!format.getElements().stream().anyMatch(VertexFormatElement::isPositionElement))
			return; // VertexFormat doesn't have a position

		Arrays.stream(Direction.values())
			.map(this::getQuads)
			.flatMap(Collection::stream)
			.map(this::quadToFace)
			.forEachOrdered(faces::add);
	}

	@Override
	public Set<Model> flatten(MatrixStack matrixStack) {
		Set<Model> models = new HashSet<>();

		matrixStack.pushMatrix();
		matrixStack.transform(matrix.getMatrix());
		//Create a new model with transformation applied.
		MeshModel transformedModel = clone();
		// correct formula for Normal Matrix is transpose(inverse(mat3(model_mat))
		// we have to augemnt that to 4x4
		RealMatrix normalMatrix3x3 = new LUDecomposition(matrixStack.getMatrix().getSubMatrix(0, 2, 0, 2), 1e-5).getSolver().getInverse().transpose();
		RealMatrix normalMatrix = MatrixUtils.createRealMatrix(4, 4);
		normalMatrix.setSubMatrix(normalMatrix3x3.getData(), 0, 0);
		normalMatrix.setEntry(3, 3, 1);

		transformedModel.faces.stream().forEach(f -> {
				f.normal = TransformUtil.transform(f.normal, normalMatrix);
				f.vertices.forEach(v -> v.vec = matrixStack.apply(v.vec));
			}
		);

		models.add(transformedModel);
		//Flatten child models
		matrixStack.pushMatrix();
		matrixStack.translate(0.5, 0.5, 0.5);
		models.addAll(children.stream().flatMap(m -> m.flatten(matrixStack).stream()).collect(Collectors.toSet()));
		matrixStack.popMatrix().popMatrix();
		return models;
	}

	public List<BakedQuad> getQuads(Direction side) {
		return getQuads(DirectionConverter.instance().toNative(side));
	}

	public List<BakedQuad> getQuads(@Nullable EnumFacing side) {
		return wrapped.getQuads(blockState.orElse(null), side, rand);
	}

	public Face quadToFace(BakedQuad quad) {
		Face face = new Face();
		final VertexFormat format = quad.getFormat();

		int[] data = quad.getVertexData();
		Optional<TextureAtlasSprite> texture = Optional.ofNullable(quad.getSprite() == null ? wrapped.getParticleTexture() : quad.getSprite());

		final Optional<VertexFormatElement> posElement = ((Collection<VertexFormatElement>)format.getElements()).stream()
			.filter(VertexFormatElement::isPositionElement)
			.findFirst();

		final Optional<VertexFormatElement> uvElement = ((Collection<VertexFormatElement>)format.getElements()).stream()
			.filter(vfe -> vfe.getUsage() == VertexFormatElement.EnumUsage.UV)
			.findFirst();

		face.texture = texture
			.filter(t -> uvElement.isPresent())
			.map(TextureAtlasSprite::getIconName)
			.map(ResourceLocation::new)
			.map(AssetConverter.instance()::toNovaTexture);

		// `VertexFormat` offsets are for a `ByteBuffer`
		// `data` is an int array, so we convert it

		// TODO: support offsets which are not divisible by four
		final int posOffset = posElement.map(VertexFormatElement::getIndex).map(i -> i / 4).orElse(-1);
		final int uvOffset = uvElement.map(VertexFormatElement::getIndex).map(i -> i / 4).orElse(-1);
		final int colorOffset = format.hasColor() ? (format.getColorOffset() / 4) : -1;
		final int normalOffset = format.hasNormal() ? (format.getNormalOffset() / 4) : -1;

		for (int i = 0; i < data.length; i += 7) {
			Vector3D pos = posElement.isPresent() ? new Vector3D(
				Float.intBitsToFloat(data[i + posOffset]),
				Float.intBitsToFloat(data[i + posOffset + 1]),
				Float.intBitsToFloat(data[i + posOffset + 2])) : Vector3D.ZERO;

			Vector2D uv = uvElement.isPresent() ? new Vector2D(
				deinterpolateU(Float.intBitsToFloat(data[i + uvOffset]), texture),
				deinterpolateV(Float.intBitsToFloat(data[i + uvOffset + 1]), texture)) : Vector2D.ZERO;

			Vertex vertex = new Vertex(pos, uv);
			if (format.hasColor()) {
				vertex.color = Color.argb(data[i + colorOffset]);
			}

			Optional<Vector3D> normal = Optional.empty();
			if (format.hasNormal()) {
				int mergedNormal = data[i + normalOffset];
				if (mergedNormal != 0)
					normal = Optional.of(new Vector3D(((byte)(mergedNormal & 0xFF)) / 127D,
						((byte)((mergedNormal >> 8) & 0xFF)) / 127D,
						((byte)((mergedNormal >> 16) & 0xFF)) / 127D));
			}

			if (format.hasNormal())
				vertex.normal = normal;
			face.drawVertex(vertex);
		}
		face.normal = Vector3DUtil.calculateNormal(face);
		return face;
	}

	private double deinterpolateU(double u, Optional<TextureAtlasSprite> texture) {
		return u;
	}

	private double deinterpolateV(double v, Optional<TextureAtlasSprite> texture) {
		return 1 - v; // Why do you change the format with every version, Mojang?
	}
}
