package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {
    @Shadow @Final protected FlowableFluid fluid;
    @Shadow protected abstract void playExtinguishSound(WorldAccess world, BlockPos pos);

    @Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
    private void receiveNeighborFluids(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        for (Direction dir : Direction.values()) {
            FluidState fluidState = world.getFluidState(pos.offset(dir));
            if (fluidState.getFluid() instanceof FlowableGasFluid) {
                if (dir == Direction.UP) continue;
            } else if (dir == Direction.DOWN) continue;
            if (fluid.onFluidMix(world, pos, dir, fluidState)) {
                playExtinguishSound(world, pos);
                cir.setReturnValue(false);
            }
        }
    }

    @ModifyReturnValue(method = "canPathfindThrough", at = @At("RETURN"))
    private boolean canPathfindThrough(boolean original, BlockState state, NavigationType type) {
        return original && fluid.canPathfindThrough(type);
    }
}