package com.hersh.liquid_lab.fluid;

import com.google.common.collect.Maps;
import com.hersh.liquid_lab.mixin.FlowableFluidAccessor;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Map;

public abstract class FlowableGasFluid extends FlowableFluid {
    private final Map<FluidState, VoxelShape> shapeCache = Maps.newIdentityHashMap();

    @Override
    public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        double x = 0.0F;
        double z = 0.0F;
        BlockPos.Mutable offsetPos = new BlockPos.Mutable();

        for(Direction dir : Direction.Type.HORIZONTAL) {
            offsetPos.set(pos, dir);
            FluidState offsetState = world.getFluidState(offsetPos);
            if (!this.isEmptyOrThis(offsetState)) continue;
            float height = offsetState.getHeight();
            float heightDiff = 0.0F;
            if (height == 0.0F) {
                if (!world.getBlockState(offsetPos).blocksMovement()) {
                    FluidState fluidState2 = world.getFluidState(offsetPos.up());
                    if (this.isEmptyOrThis(fluidState2)) {
                        height = fluidState2.getHeight();
                        if (height > 0.0F) {
                            heightDiff = state.getHeight() - (height - 0.8888889F);
                        }
                    }
                }
            } else if (height > 0.0F) {
                heightDiff = state.getHeight() - height;
            }

            if (heightDiff != 0.0F) {
                x += dir.getOffsetX() * heightDiff;
                z += dir.getOffsetZ() * heightDiff;
            }
        }

        Vec3d vec3d = new Vec3d(x, 0.0F, z);
        if (state.get(FALLING)) {
            for(Direction dir : Direction.Type.HORIZONTAL) {
                offsetPos.set(pos, dir);
                if (this.isFlowBlocked(world, offsetPos, dir) || this.isFlowBlocked(world, offsetPos.down(), dir)) {
                    vec3d = vec3d.normalize().add(0.0F, 6.0F, 0.0F);
                    break;
                }
            }
        }
        return vec3d.normalize();
    }

    @Override
    protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid().matchesType(this)) {
            return false;
        } else if (direction == Direction.DOWN) {
            return true;
        } else {
            return !(blockState.getBlock() instanceof IceBlock) && blockState.isSideSolidFullSquare(world, pos, direction);
        }
    }

    @Override
    protected void tryFlow(World world, BlockPos fluidPos, FluidState state) {
        if (state.isEmpty()) return;
        BlockState blockState = world.getBlockState(fluidPos);
        BlockPos blockPos = fluidPos.up();
        BlockState blockState2 = world.getBlockState(blockPos);
        FluidState fluidState = this.getUpdatedState(world, blockPos, blockState2);
        if (this.canFlow(world, fluidPos, blockState, Direction.UP, blockPos, blockState2, world.getFluidState(blockPos), fluidState.getFluid())) {
            this.flow(world, blockPos, blockState2, Direction.UP, fluidState);
            if (((FlowableFluidAccessor) this).invokeCountNeighboringSources(world, fluidPos) >= 3) {
                ((FlowableFluidAccessor) this).invokeFlowToSides(world, fluidPos, state, blockState);
            }
        } else if (state.isStill() || !this.canFlowUpTo(world, fluidState.getFluid(), fluidPos, blockState, blockPos, blockState2)) {
            ((FlowableFluidAccessor) this).invokeFlowToSides(world, fluidPos, state, blockState);
        }
    }

    @Override
    protected FluidState getUpdatedState(World world, BlockPos pos, BlockState state) {
        int highestLevel = 0;
        int sources = 0;

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.getFluid().matchesType(this) && ((FlowableFluidAccessor) this).invokeReceivesFlow(direction, world, pos, state, blockPos, blockState)) {
                if (fluidState.isStill()) sources++;
                highestLevel = Math.max(highestLevel, fluidState.getLevel());
            }
        }

        if (this.isInfinite(world) && sources >= 2) {
            BlockState blockState2 = world.getBlockState(pos.up());
            FluidState fluidState2 = blockState2.getFluidState();
            if (blockState2.isSolid() || this.isMatchingAndStill(fluidState2)) {
                return this.getStill(false);
            }
        }

        BlockPos blockPos2 = pos.down();
        BlockState blockState3 = world.getBlockState(blockPos2);
        FluidState fluidState3 = blockState3.getFluidState();
        if (!fluidState3.isEmpty() && fluidState3.getFluid().matchesType(this) && ((FlowableFluidAccessor) this).invokeReceivesFlow(Direction.DOWN, world, pos, state, blockPos2, blockState3)) {
            return this.getFlowing(8, true);
        } else {
            int newLevel = highestLevel - this.getLevelDecreasePerBlock(world);
            return newLevel <= 0 ? Fluids.EMPTY.getDefaultState() : this.getFlowing(newLevel, false);
        }
    }

    @Override
    protected int getMinFlowDownDistance(WorldView world, BlockPos pos, int i, Direction direction, BlockState state, BlockPos fromPos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap flowUpCache) {
        int minDistance = 1000;

        for (Direction direction2 : Direction.Type.HORIZONTAL) {
            if (direction2 == direction) continue;
            BlockPos blockPos = pos.offset(direction2);
            short packedOffset = packXZOffset(fromPos, blockPos);
            Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(packedOffset, (Short2ObjectFunction<? extends Pair<BlockState, FluidState>>)(s -> {
                BlockState blockState = world.getBlockState(blockPos);
                return Pair.of(blockState, blockState.getFluidState());
            }));
            BlockState blockState = pair.getFirst();
            FluidState fluidState = pair.getSecond();
            if (this.canFlowThrough(world, this.getFlowing(), pos, state, direction2, blockPos, blockState, fluidState)) {
                if (
                        flowUpCache.computeIfAbsent(packedOffset, (s -> {
                            BlockPos upPos = blockPos.up();
                            BlockState upState = world.getBlockState(upPos);
                            return this.canFlowUpTo(world, this.getFlowing(), blockPos, blockState, upPos, upState);
                        }))
                ) return i;

                if (i < this.getMaxFlowDistance(world)) {
                    int k = this.getMinFlowDownDistance(world, blockPos, i + 1, direction2.getOpposite(), blockState, fromPos, stateCache, flowUpCache);
                    if (k < minDistance) minDistance = k;
                }
            }
        }
        return minDistance;
    }

    @Override
    protected Map<Direction, FluidState> getSpread(World world, BlockPos pos, BlockState state) {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2BooleanMap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            short s = packXZOffset(pos, blockPos);
            Pair<BlockState, FluidState> pair = short2ObjectMap.computeIfAbsent(s, (Short2ObjectFunction<? extends Pair<BlockState, FluidState>>)(sx -> {
                BlockState blockStatex = world.getBlockState(blockPos);
                return Pair.of(blockStatex, blockStatex.getFluidState());
            }));
            BlockState blockState = pair.getFirst();
            FluidState fluidState = pair.getSecond();
            FluidState fluidState2 = this.getUpdatedState(world, blockPos, blockState);
            if (this.canFlowThrough(world, fluidState2.getFluid(), pos, state, direction, blockPos, blockState, fluidState)) {
                BlockPos blockPos2 = blockPos.up();
                int j;
                if (
                        short2BooleanMap.computeIfAbsent(s, (sx -> {
                            BlockState blockState2 = world.getBlockState(blockPos2);
                            return this.canFlowUpTo(world, this.getFlowing(), blockPos, blockState, blockPos2, blockState2);
                        }))
                ) j = 0;
                else j = this.getMinFlowDownDistance(world, blockPos, 1, direction.getOpposite(), blockState, pos, short2ObjectMap, short2BooleanMap);

                if (j < i) {
                    map.clear();
                }

                if (j <= i) {
                    map.put(direction, fluidState2);
                    i = j;
                }
            }
        }
        return map;
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        return isFluidBelowEqual(state, world, pos) ? 1.0F : state.getHeight();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        return state.getLevel() == 9 && isFluidBelowEqual(state, world, pos)
                ? VoxelShapes.fullCube()
                : this.shapeCache.computeIfAbsent(state, state2 -> VoxelShapes.cuboid(0.0, 1.0 - state2.getHeight(world, pos), 0.0, 1.0, 1.0, 1.0));
    }

    private boolean canFlowUpTo(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!((FlowableFluidAccessor) this).invokeReceivesFlow(Direction.UP, world, pos, state, fromPos, fromState)) {
            return false;
        } else {
            return fromState.getFluidState().getFluid().matchesType(this) || ((FlowableFluidAccessor) this).invokeCanFill(world, fromPos, fromState, fluid);
        }
    }

    private static boolean isFluidBelowEqual(FluidState state, BlockView world, BlockPos pos) {
        return state.getFluid().matchesType(world.getFluidState(pos.down()).getFluid());
    }

    private static short packXZOffset(BlockPos from, BlockPos to) {
        int x = to.getX() - from.getX();
        int z = to.getZ() - from.getZ();
        return (short)((x + 128 & 0xFF) << 8 | z + 128 & 0xFF);
    }

    private boolean isEmptyOrThis(FluidState state) {
        return state.isEmpty() || state.getFluid().matchesType(this);
    }

    private boolean isMatchingAndStill(FluidState state) {
        return state.isStill() && state.getFluid().matchesType(this);
    }

    private boolean canFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
        return !this.isMatchingAndStill(fluidState) && ((FlowableFluidAccessor) this).invokeReceivesFlow(face, world, pos, state, fromPos, fromState) && ((FlowableFluidAccessor) this).invokeCanFill(world, fromPos, fromState, fluid);
    }
}
