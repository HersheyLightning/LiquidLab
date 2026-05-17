package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Shadow protected abstract void addParticle(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters);
    @Shadow protected abstract void addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y);

    @WrapWithCondition(method = "randomBlockDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/particle/ParticleEffect;Z)V"))
    private boolean addParticle(ClientWorld instance, BlockPos upPos, BlockState upState, ParticleEffect particle, boolean solidAbove, @Local(argsOnly = true) BlockPos.Mutable pos, @Local BlockState blockState, @Local FluidState fluidState) {
        if (!(fluidState.getFluid() instanceof FlowableGasFluid)) return true;
        upPos = pos.up();
        upState = instance.getBlockState(upPos);
        solidAbove = blockState.isSideSolidFullSquare(instance, pos, Direction.UP);

        if (upState.getFluidState().isEmpty()) {
            VoxelShape voxelShape = upState.getCollisionShape(instance, upPos);
            double minY = voxelShape.getMin(Direction.Axis.Y);
            if (minY > 0.0) {
                if (solidAbove) {
                    this.addParticle(upPos.getX(), upPos.getX() + 1, upPos.getZ(), upPos.getZ() + 1, upPos.getY() + 0.05, particle);
                }
            } else if (!upState.isIn(BlockTags.IMPERMEABLE)) {
                double maxY = voxelShape.getMax(Direction.Axis.Y);
                if (maxY < 1.0) {
                    this.addParticle(upPos, particle, voxelShape, upPos.getY() + maxY + 0.05);
                } else {
                    BlockPos upPos2 = upPos.up();
                    BlockState upState2 = instance.getBlockState(upPos2);
                    VoxelShape voxelShape2 = upState2.getCollisionShape(instance, upPos2);
                    double minY2 = voxelShape2.getMin(Direction.Axis.Y);
                    if (minY2 > 0.0 && upState2.getFluidState().isEmpty()) {
                        this.addParticle(upPos, particle, voxelShape, upPos.getY() + 1.05);
                    }
                }
            }
        }
        return false;
    }
}
