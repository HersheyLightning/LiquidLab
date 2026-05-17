package com.hersh.liquid_lab.util;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class FluidUtils {
    public static boolean isSubmerged(BlockView world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        FluidState fluidState = world.getFluidState(blockPos);
        if (fluidState.isEmpty()) return false;
        if (fluidState.getFluid() instanceof FlowableGasFluid) {
            return pos.y >= blockPos.getY() + 1 - fluidState.getHeight(world, blockPos);
        }
        return pos.y <= blockPos.getY() + fluidState.getHeight(world, blockPos);
    }

    public static boolean canBoxSubmergeIn(World world, BlockPos pos, Box box) {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty()) return false;
        if (fluidState.getFluid() instanceof FlowableGasFluid) {
            return box.maxY >= pos.getY() + 1 - fluidState.getHeight(world, pos);
        }
        return box.minY <= pos.getY() + fluidState.getHeight(world, pos);
    }

    public static boolean forFluidsInBox(World world, Box box, BiPredicate<BlockPos, FluidState> predicate) {
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        boolean success = false;
        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    blockPos.set(x, y, z);
                    if (canBoxSubmergeIn(world, blockPos, box)) {
                        FluidState fluidState = world.getFluidState(blockPos);
                        if (predicate.test(blockPos, fluidState)) success = true;
                    }
                }
            }
        }
        return success;
    }

    public static boolean firstFluidInBox(World world, Box box, BiPredicate<BlockPos, FluidState> predicate) {
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    blockPos.set(x, y, z);
                    if (canBoxSubmergeIn(world, blockPos, box)) {
                        FluidState fluidState = world.getFluidState(blockPos);
                        if (predicate.test(blockPos, fluidState)) return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean firstFluidInCamera(Camera camera, Predicate<FluidState> predicate) {
        if (!camera.isReady()) return false;

        Camera.Projection projection = camera.getProjection();
        for (Vec3d offset : Arrays.asList(projection.center, projection.getBottomRight(), projection.getTopRight(), projection.getBottomLeft(), projection.getTopLeft())) {
            Vec3d pos = camera.getPos().add(offset);
            if (!isSubmerged(camera.area, pos)) continue;
            FluidState fluidState = camera.area.getFluidState(BlockPos.ofFloored(pos));
            if (predicate.test(fluidState)) return true;
        }
        return false;
    }

    public static void capFog(BackgroundRenderer.FogData fogData, float max, boolean spectator) {
        if (spectator) {
            fogData.fogStart = -8.0F;
            fogData.fogEnd = max * 0.5F;
        }
        if (fogData.fogEnd > max) {
            fogData.fogEnd = max;
            fogData.fogShape = FogShape.CYLINDER;
        }
    }
}
