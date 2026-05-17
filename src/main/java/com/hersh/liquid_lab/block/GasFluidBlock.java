package com.hersh.liquid_lab.block;

import com.google.common.collect.ImmutableList;
import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class GasFluidBlock extends FluidBlock {
    public static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    public static final ImmutableList<Direction> FLOW_DIRECTIONS = ImmutableList.of(
            Direction.UP, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST
    );

    public GasFluidBlock(FlowableGasFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return context.isAbove(COLLISION_SHAPE, pos, false) && state.get(LEVEL) == 0 && context.canWalkOnFluid(world.getFluidState(pos.down()), state.getFluidState())
                ? COLLISION_SHAPE
                : VoxelShapes.empty();
    }
}
