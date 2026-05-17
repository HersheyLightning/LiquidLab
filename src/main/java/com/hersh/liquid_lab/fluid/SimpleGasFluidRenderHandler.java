package com.hersh.liquid_lab.fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler.*;

@Environment(EnvType.CLIENT)
public class SimpleGasFluidRenderHandler implements FluidRenderHandler {
    protected final Identifier stillTexture;
    protected final Identifier flowingTexture;
    protected final Identifier overlayTexture;

    protected final Sprite[] sprites;

    protected final int tint;

    public SimpleGasFluidRenderHandler(Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture, int tint) {
        this.stillTexture = Objects.requireNonNull(stillTexture, "stillTexture");
        this.flowingTexture = Objects.requireNonNull(flowingTexture, "flowingTexture");
        this.overlayTexture = overlayTexture;
        this.sprites = new Sprite[overlayTexture == null ? 2 : 3];
        this.tint = tint;
    }

    public SimpleGasFluidRenderHandler(Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture) {
        this(stillTexture, flowingTexture, overlayTexture, -1);
    }

    public SimpleGasFluidRenderHandler(Identifier stillTexture, Identifier flowingTexture, int tint) {
        this(stillTexture, flowingTexture, null, tint);
    }

    public SimpleGasFluidRenderHandler(Identifier stillTexture, Identifier flowingTexture) {
        this(stillTexture, flowingTexture, null, -1);
    }

    public static SimpleGasFluidRenderHandler coloredWater(int tint) {
        return new SimpleGasFluidRenderHandler(WATER_STILL, WATER_FLOWING, WATER_OVERLAY, tint);
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return this.sprites;
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        this.sprites[0] = textureAtlas.getSprite(this.stillTexture);
        this.sprites[1] = textureAtlas.getSprite(this.flowingTexture);

        if (this.overlayTexture != null) {
            this.sprites[2] = textureAtlas.getSprite(this.overlayTexture);
        }
    }

    @Override
    public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return this.tint;
    }

    @Override
    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        int tint = this.getFluidColor(world, pos, fluidState);
        float red = (tint >> 16 & 0xFF) / 255.0F;
        float green = (tint >> 8 & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        BlockState downBlock = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState downFluid = downBlock.getFluidState();
        BlockState upBlock = world.getBlockState(pos.offset(Direction.UP));
        FluidState upFluid = upBlock.getFluidState();
        BlockState northBlock = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState northFluid = northBlock.getFluidState();
        BlockState southBlock = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState southFluid = southBlock.getFluidState();
        BlockState westBlock = world.getBlockState(pos.offset(Direction.WEST));
        FluidState westFluid = westBlock.getFluidState();
        BlockState eastBlock = world.getBlockState(pos.offset(Direction.EAST));
        FluidState eastFluid = eastBlock.getFluidState();
        boolean renderUp = shouldRenderSide(world, pos, fluidState, blockState, Direction.UP, upFluid)
                && !isSideCovered(world, pos, Direction.UP, 0.8888889F, upBlock);
        boolean renderDown = !isSameFluid(fluidState, downFluid);
        boolean renderNorth = shouldRenderSide(world, pos, fluidState, blockState, Direction.NORTH, northFluid);
        boolean renderSouth = shouldRenderSide(world, pos, fluidState, blockState, Direction.SOUTH, southFluid);
        boolean renderEast = shouldRenderSide(world, pos, fluidState, blockState, Direction.WEST, westFluid);
        boolean renderWest = shouldRenderSide(world, pos, fluidState, blockState, Direction.EAST, eastFluid);
        if (!renderDown && !renderUp && !renderWest && !renderEast && !renderNorth && !renderSouth) return;
        float downLight = world.getBrightness(Direction.DOWN, true);
        float upLight = world.getBrightness(Direction.UP, true);
        float northLight = world.getBrightness(Direction.NORTH, true);
        float westLight = world.getBrightness(Direction.WEST, true);
        Fluid fluid = fluidState.getFluid();
        float n = this.getFluidHeight(world, fluid, pos, blockState, fluidState);
        float NEHeight;
        float NWHeight;
        float SEHeight;
        float SWHeight;
        if (n >= 1.0F) {
            NEHeight = 1.0F;
            NWHeight = 1.0F;
            SEHeight = 1.0F;
            SWHeight = 1.0F;
        } else {
            float s = this.getFluidHeight(world, fluid, pos.north(), northBlock, northFluid);
            float t = this.getFluidHeight(world, fluid, pos.south(), southBlock, southFluid);
            float u = this.getFluidHeight(world, fluid, pos.east(), eastBlock, eastFluid);
            float v = this.getFluidHeight(world, fluid, pos.west(), westBlock, westFluid);
            NEHeight = this.calculateFluidHeight(world, fluid, n, s, u, pos.offset(Direction.NORTH).offset(Direction.EAST));
            NWHeight = this.calculateFluidHeight(world, fluid, n, s, v, pos.offset(Direction.NORTH).offset(Direction.WEST));
            SEHeight = this.calculateFluidHeight(world, fluid, n, t, u, pos.offset(Direction.SOUTH).offset(Direction.EAST));
            SWHeight = this.calculateFluidHeight(world, fluid, n, t, v, pos.offset(Direction.SOUTH).offset(Direction.WEST));
        }

        float s = pos.getX() & 15;
        float t = pos.getY() & 15;
        float u = pos.getZ() & 15;
        float v = 0.001F;
        float w = renderUp ? v : 0.0F;
        if (renderDown && !isSideCovered(world, pos, Direction.DOWN, Math.min(Math.min(NWHeight, SWHeight), Math.min(SEHeight, NEHeight)), downBlock)) {
            NWHeight -= v;
            SWHeight -= v;
            SEHeight -= v;
            NEHeight -= v;
            Vec3d vec3d = fluidState.getVelocity(world, pos);
            float x;
            float z;
            float ab;
            float ad;
            float y;
            float aa;
            float ac;
            float ae;
            if (vec3d.x == 0.0 && vec3d.z == 0.0) {
                Sprite sprite = this.sprites[0];
                x = sprite.getFrameU(0.0F);
                y = sprite.getFrameV(0.0F);
                z = x;
                aa = sprite.getFrameV(1.0F);
                ab = sprite.getFrameU(1.0F);
                ac = aa;
                ad = ab;
                ae = y;
            } else {
                Sprite sprite = this.sprites[1];
                float af = (float)MathHelper.atan2(vec3d.z, vec3d.x) - (float) (Math.PI / 2);
                float ag = MathHelper.sin(af) * 0.25F;
                float ah = MathHelper.cos(af) * 0.25F;
                float ai = 0.5F;
                x = sprite.getFrameU(ai + (-ah - ag));
                y = sprite.getFrameV(ai + (-ah + ag));
                z = sprite.getFrameU(ai + (-ah + ag));
                aa = sprite.getFrameV(ai + (ah + ag));
                ab = sprite.getFrameU(ai + (ah + ag));
                ac = sprite.getFrameV(ai + (ah - ag));
                ad = sprite.getFrameU(ai + (ah - ag));
                ae = sprite.getFrameV(ai + (-ah - ag));
            }

            float aj = (x + z + ab + ad) / 4.0F;
            float af = (y + aa + ac + ae) / 4.0F;
            float ag = this.sprites[0].getAnimationFrameDelta();
            x = MathHelper.lerp(ag, x, aj);
            z = MathHelper.lerp(ag, z, aj);
            ab = MathHelper.lerp(ag, ab, aj);
            ad = MathHelper.lerp(ag, ad, aj);
            y = MathHelper.lerp(ag, y, af);
            aa = MathHelper.lerp(ag, aa, af);
            ac = MathHelper.lerp(ag, ac, af);
            ae = MathHelper.lerp(ag, ae, af);
            int ak = this.getLight(world, pos);
            float redShaded = downLight * red;
            float greenShaded = downLight * green;
            float blueShaded = downLight * blue;
            this.vertex(vertexConsumer, s + 0.0F, t + 1 - NWHeight, u + 0.0F, redShaded, greenShaded, blueShaded, x, y, ak);
            this.vertex(vertexConsumer, s + 1.0F, t + 1 - NEHeight, u + 0.0F, redShaded, greenShaded, blueShaded, ad, ae, ak);
            this.vertex(vertexConsumer, s + 1.0F, t + 1 - SEHeight, u + 1.0F, redShaded, greenShaded, blueShaded, ab, ac, ak);
            this.vertex(vertexConsumer, s + 0.0F, t + 1 - SWHeight, u + 1.0F, redShaded, greenShaded, blueShaded, z, aa, ak);
            if (fluidState.canFlowTo(world, pos.down())) {
                this.vertex(vertexConsumer, s + 0.0F, t + 1 - NWHeight, u + 0.0F, redShaded, greenShaded, blueShaded, x, y, ak);
                this.vertex(vertexConsumer, s + 0.0F, t + 1 - SWHeight, u + 1.0F, redShaded, greenShaded, blueShaded, z, aa, ak);
                this.vertex(vertexConsumer, s + 1.0F, t + 1 - SEHeight, u + 1.0F, redShaded, greenShaded, blueShaded, ab, ac, ak);
                this.vertex(vertexConsumer, s + 1.0F, t + 1 - NEHeight, u + 0.0F, redShaded, greenShaded, blueShaded, ad, ae, ak);
            }
        }

        if (renderUp) {
            float xx = this.sprites[0].getMinU();
            float zx = this.sprites[0].getMaxU();
            float abx = this.sprites[0].getMinV();
            float adx = this.sprites[0].getMaxV();
            int an = this.getLight(world, pos.up());
            float redShaded = upLight * red;
            float greenShaded = upLight * green;
            float blueShaded = upLight * blue;
            this.vertex(vertexConsumer, s, t + 1 - w, u + 1.0F, redShaded, greenShaded, blueShaded, xx, adx, an);
            this.vertex(vertexConsumer, s + 1.0F, t + 1 - w, u + 1.0F, redShaded, greenShaded, blueShaded, zx, adx, an);
            this.vertex(vertexConsumer, s + 1.0F, t + 1 - w, u, redShaded, greenShaded, blueShaded, zx, abx, an);
            this.vertex(vertexConsumer, s, t + 1 - w, u, redShaded, greenShaded, blueShaded, xx, abx, an);
        }

        int ao = this.getLight(world, pos);

        for (Direction direction : Direction.Type.HORIZONTAL) {
            float adx;
            float yx;
            float aax;
            float acx;
            float aex;
            float ap;
            boolean shouldRender;
            switch (direction) {
                case NORTH:
                    adx = NWHeight;
                    yx = NEHeight;
                    aax = s;
                    aex = s + 1.0F;
                    acx = u + v;
                    ap = u + v;
                    shouldRender = renderNorth;
                    break;
                case SOUTH:
                    adx = SEHeight;
                    yx = SWHeight;
                    aax = s + 1.0F;
                    aex = s;
                    acx = u + 1.0F - v;
                    ap = u + 1.0F - v;
                    shouldRender = renderSouth;
                    break;
                case WEST:
                    adx = SWHeight;
                    yx = NWHeight;
                    aax = s + v;
                    aex = s + v;
                    acx = u + 1.0F;
                    ap = u;
                    shouldRender = renderEast;
                    break;
                default:
                    adx = NEHeight;
                    yx = SEHeight;
                    aax = s + 1.0F - v;
                    aex = s + 1.0F - v;
                    acx = u;
                    ap = u + 1.0F;
                    shouldRender = renderWest;
            }

            if (shouldRender && !isSideCovered(world, pos, direction, Math.max(adx, yx), world.getBlockState(pos.offset(direction)))) {
                BlockPos blockPos = pos.offset(direction);
                Sprite sprite2 = this.sprites[1];
                boolean changedSprite = false;
                if (this.overlayTexture != null) {
                    Block block = world.getBlockState(blockPos).getBlock();
                    if (FluidRenderHandlerRegistry.INSTANCE.isBlockTransparent(block)) {
                        sprite2 = this.sprites[2];
                        changedSprite = true;
                    }
                }

                float ah = sprite2.getFrameU(0.0F);
                float ai = sprite2.getFrameU(0.5F);
                float al = sprite2.getFrameV((1.0F - adx) * 0.5F);
                float am = sprite2.getFrameV((1.0F - yx) * 0.5F);
                float aq = sprite2.getFrameV(0.5F);
                float ar = direction.getAxis() == Direction.Axis.Z ? northLight : westLight;
                float redShaded = upLight * ar * red;
                float greenShaded = upLight * ar * green;
                float blueShaded = upLight * ar * blue;
                this.vertex(vertexConsumer, aax, t + 1 - w, acx, redShaded, greenShaded, blueShaded, ah, aq, ao);
                this.vertex(vertexConsumer, aex, t + 1 - w, ap, redShaded, greenShaded, blueShaded, ai, aq, ao);
                this.vertex(vertexConsumer, aex, t + 1 - yx, ap, redShaded, greenShaded, blueShaded, ai, am, ao);
                this.vertex(vertexConsumer, aax, t + 1 - adx, acx, redShaded, greenShaded, blueShaded, ah, al, ao);
                if (!changedSprite) {
                    this.vertex(vertexConsumer, aax, t + 1 - adx, acx, redShaded, greenShaded, blueShaded, ah, al, ao);
                    this.vertex(vertexConsumer, aex, t + 1 - yx, ap, redShaded, greenShaded, blueShaded, ai, am, ao);
                    this.vertex(vertexConsumer, aex, t + 1 - w, ap, redShaded, greenShaded, blueShaded, ai, aq, ao);
                    this.vertex(vertexConsumer, aax, t + 1 - w, acx, redShaded, greenShaded, blueShaded, ah, aq, ao);
                }
            }
        }
    }


    private static boolean isSameFluid(FluidState a, FluidState b) {
        return b.getFluid().matchesType(a.getFluid());
    }

    private static boolean isSideCovered(BlockView world, Direction direction, float height, BlockPos pos, BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.cuboid(0.0, 1.0 - height, 0.0, 1.0, 1.0, 1.0);
            VoxelShape voxelShape2 = state.getCullingShape(world, pos);
            return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
        } else {
            return false;
        }
    }

    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation, BlockState state) {
        return isSideCovered(world, direction, maxDeviation, pos.offset(direction), state);
    }

    private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
    }

    private static boolean shouldRenderSide(
            BlockRenderView world, BlockPos pos, FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState
    ) {
        return !isOppositeSideCovered(world, pos, blockState, direction) && !isSameFluid(fluidState, neighborFluidState);
    }

    private float calculateFluidHeight(BlockRenderView world, Fluid fluid, float originHeight, float northSouthHeight, float eastWestHeight, BlockPos pos) {
        if (!(eastWestHeight >= 1.0F) && !(northSouthHeight >= 1.0F)) {
            float[] fs = new float[2];
            if (eastWestHeight > 0.0F || northSouthHeight > 0.0F) {
                float f = this.getFluidHeight(world, fluid, pos);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                this.addHeight(fs, f);
            }

            this.addHeight(fs, originHeight);
            this.addHeight(fs, eastWestHeight);
            this.addHeight(fs, northSouthHeight);
            return fs[0] / fs[1];
        } else {
            return 1.0F;
        }
    }

    private void addHeight(float[] weightedAverageHeight, float height) {
        if (height >= 0.8F) {
            weightedAverageHeight[0] += height * 10.0F;
            weightedAverageHeight[1] += 10.0F;
        } else if (height >= 0.0F) {
            weightedAverageHeight[0] += height;
            weightedAverageHeight[1]++;
        }
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return this.getFluidHeight(world, fluid, pos, blockState, blockState.getFluidState());
    }

    private float getFluidHeight(BlockRenderView world, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (fluid.matchesType(fluidState.getFluid())) {
            BlockState blockState2 = world.getBlockState(pos.down());
            return fluid.matchesType(blockState2.getFluidState().getFluid()) ? 1.0F : fluidState.getHeight();
        } else {
            return !blockState.isSolid() ? 0.0F : -1.0F;
        }
    }

    private void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
        vertexConsumer.vertex(f, g, h).color(i, j, k, 1.0F).texture(l, m).light(n).normal(0.0F, 1.0F, 0.0F);
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.down());
        int k = i & 0xFF;
        int l = j & 0xFF;
        int m = i >> 16 & 0xFF;
        int n = j >> 16 & 0xFF;
        return (Math.max(k, l)) | (Math.max(m, n)) << 16;
    }
}
