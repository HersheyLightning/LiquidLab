package com.hersh.liquid_lab.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface FluidExtensions {

    @Nullable
    default FluidMotion getFluidMotion(WorldView world, Entity entity) {
        return null;
    }

    @Nullable
    default FluidMotion getItemMotion() {
        return null;
    }

    @Nullable
    default FogProperties getFogProperties(WorldView world, Vec3d pos, Entity entity) {
        return null;
    }

    default boolean canPathfindThrough(NavigationType type) {
        return true;
    }

    default void onEntityEntry(World world, Entity entity) {

    }
    
    default void onEntityCollision(World world, BlockPos pos, Entity entity) {
    }

    default boolean onFluidMix(World world, BlockPos pos, Direction dir, FluidState fluidState) {
        return false;
    }

    default Optional<SoundEvent> getBucketEmptySound() {
        return Optional.empty();
    }

    @Nullable
    default ParticleEffect getRainParticle() {
        return null;
    }
}