package cofh.thermal.core.client.renderer.model;

import cofh.core.client.renderer.model.ModelUtils;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.lib.client.renderer.block.model.RetexturedBakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnderlayBakedModel extends BakedModelWrapper<BakedModel> implements IDynamicBakedModel {

    // Model instances are shared by the chunk compilation workers.
    private static final Map<FluidQuadKey, BakedQuad> FLUID_QUAD_CACHE = new ConcurrentHashMap<>();
    private static final Map<UnderlayQuadKey, BakedQuad> UNDERLAY_QUAD_CACHE = new ConcurrentHashMap<>();

    private record FluidQuadKey(BakedQuad base, TextureAtlasSprite texture, int color) {}
    private record UnderlayQuadKey(BakedQuad base, ResourceLocation texture) {}

    public static void clearCache() {

        FLUID_QUAD_CACHE.clear();
        UNDERLAY_QUAD_CACHE.clear();
    }

    protected int underlayQuadLevel = 0;

    public UnderlayBakedModel(BakedModel originalModel) {

        super(originalModel);
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {

        return addUnderlayQuads(new LinkedList<>(originalModel.getQuads(state, side, rand, extraData, renderType)), state, side, rand, extraData, renderType);
    }

    // region HELPERS
    protected List<BakedQuad> addUnderlayQuads(LinkedList<BakedQuad> quads, @Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {

        if (side == null || quads.size() <= underlayQuadLevel) {
            return quads;
        }
        BakedQuad baseQuad = quads.get(underlayQuadLevel);

        // FLUID
        if (extraData.has(ModelUtils.FLUID)) {
            FluidStack fluid = extraData.get(ModelUtils.FLUID);
            if (fluid != null && !fluid.isEmpty()) {
                var key = new FluidQuadKey(baseQuad, RenderHelper.getFluidTexture(fluid), FluidHelper.color(fluid));
                quads.offerFirst(FLUID_QUAD_CACHE.computeIfAbsent(key, quad ->
                        new RetexturedBakedQuad(RenderHelper.mulColor(quad.base(), quad.color()), quad.texture())));
            }
        } else if (extraData.has(ModelUtils.UNDERLAY)) {
            ResourceLocation texture = extraData.get(ModelUtils.UNDERLAY);
            if (texture != null) {
                var key = new UnderlayQuadKey(baseQuad, texture);
                quads.offerFirst(UNDERLAY_QUAD_CACHE.computeIfAbsent(key, quad ->
                        new RetexturedBakedQuad(quad.base(), RenderHelper.getTexture(quad.texture()))));
            }
        }
        return quads;
    }
    // endregion
}
