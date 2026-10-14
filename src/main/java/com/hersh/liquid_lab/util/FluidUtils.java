package com.hersh.liquid_lab.util;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import com.hersh.liquid_lab.util.mixing.FluidRule;
import com.hersh.liquid_lab.util.mixing.MixType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class FluidUtils {
    public static final Map<TagKey<Fluid>, List<FluidRule>> fluidRules = new HashMap<>();

    public static List<FluidRule> getFluidRules(FluidState fluid, MixType type) {
        List<FluidRule> fluidRules = new ArrayList<>();

        fluid.streamTags().forEach(tag -> {
            List<FluidRule> rules = FluidUtils.fluidRules.get(tag);
            if (rules != null) fluidRules.addAll(rules.stream().filter(rule -> rule.matchesFluid(fluid.isStill(), type)).toList());
        });
        return fluidRules;
    }

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

    public static void splashEntity(Entity entity, ParticleEffect splash, @Nullable ParticleEffect bubbles) {
        Entity splashingEntity = Objects.requireNonNullElse(entity.getControllingPassenger(), entity);
        float speedMultiplier = splashingEntity == entity ? 0.2F : 0.9F;
        Vec3d velocity = splashingEntity.getVelocity();
        float speed = Math.min(1.0F, (float) Math.sqrt(velocity.x * velocity.x * 0.2F + velocity.y * velocity.y + velocity.z * velocity.z * 0.2F) * speedMultiplier);
        if (speed < 0.25F) {
            entity.playSound(entity.getSplashSound(), speed, 1.0F + (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.4F);
        } else {
            entity.playSound(entity.getHighSpeedSplashSound(), speed, 1.0F + (entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.4F);
        }

        float blockY = MathHelper.floor(entity.getY());
        if (bubbles != null) {
            for (int i = 0; i < 1.0F + entity.getWidth() * 20.0F; i++) {
                double d = (entity.getRandom().nextDouble() * 2.0 - 1.0) * entity.getWidth();
                double e = (entity.getRandom().nextDouble() * 2.0 - 1.0) * entity.getWidth();
                entity.getWorld().addParticle(bubbles, entity.getX() + d, blockY + 1.0F, entity.getZ() + e, velocity.x, velocity.y - entity.getRandom().nextDouble() * 0.2F, velocity.z);
            }
        }
        for (int i = 0; i < 1.0F + entity.getWidth() * 20.0F; i++) {
            double d = (entity.getRandom().nextDouble() * 2.0 - 1.0) * entity.getWidth();
            double e = (entity.getRandom().nextDouble() * 2.0 - 1.0) * entity.getWidth();
            entity.getWorld().addParticle(splash, entity.getX() + d, blockY + 1.0F, entity.getZ() + e, velocity.x, velocity.y, velocity.z);
        }

        entity.emitGameEvent(GameEvent.SPLASH);
    }

    public static void splashEntity(Entity entity, ParticleEffect splash) {
        splashEntity(entity, splash, null);
    }
}
