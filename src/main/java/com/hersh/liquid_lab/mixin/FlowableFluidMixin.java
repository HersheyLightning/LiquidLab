package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.util.FluidUtils;
import com.hersh.liquid_lab.util.mixing.FluidRule;
import com.hersh.liquid_lab.util.mixing.MixType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "flow", at = @At("HEAD"), cancellable = true)
    private void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        for (FluidRule rule : FluidUtils.getFluidRules(world.getFluidState(pos), MixType.ON_FLOW)) {
            if (rule.predicate().test((ServerWorld) world, pos)) {
                world.setBlockState(pos, rule.transform().get(world.getRandom(), pos),  Block.NOTIFY_ALL);
                if (rule.smoke()) world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
                ci.cancel(); return;
            }
        }
    }
}
