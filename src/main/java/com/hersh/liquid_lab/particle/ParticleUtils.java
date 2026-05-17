package com.hersh.liquid_lab.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

@Environment(EnvType.CLIENT)
public final class ParticleUtils {
    public static <T extends ParticleEffect> void registerDripping(ParticleType<T> type, Fluid fluid, int color, ParticleEffect nextParticle) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.Dripping(world, x, y, z, fluid, nextParticle);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerFalling(ParticleType<T> type, Fluid fluid, int color, ParticleEffect nextParticle) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.ContinuousFalling(world, x, y, z, fluid, nextParticle);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerLanding(ParticleType<T> type, Fluid fluid, int color) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.Landing(world, x, y, z, fluid);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerGasDripping(ParticleType<T> type, Fluid fluid, int color, ParticleEffect nextParticle) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.Dripping(world, x, y, z, fluid, nextParticle);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerGasFalling(ParticleType<T> type, Fluid fluid, int color, ParticleEffect nextParticle) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.ContinuousFalling(world, x, y, z, fluid, nextParticle);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerGasLanding(ParticleType<T> type, Fluid fluid, int color) {
        registerBlockLeak(type, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.Landing(world, x, y, z, fluid);
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            particle.setColor(red, green, blue);

            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerBlockLeakSet(ParticleType<T> dripping, ParticleType<T> falling, ParticleType<T> landing, Fluid fluid, int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        registerBlockLeak(dripping, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.Dripping(world, x, y, z, fluid, (ParticleEffect) falling);
            particle.setColor(red, green, blue);
            return particle;
        });
        registerBlockLeak(falling, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.ContinuousFalling(world, x, y, z, fluid, (ParticleEffect) landing);
            particle.setColor(red, green, blue);
            return particle;
        });
        registerBlockLeak(landing, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            BlockLeakParticle particle = new BlockLeakParticle.Landing(world, x, y, z, fluid);
            particle.setColor(red, green, blue);
            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerGasBlockLeakSet(ParticleType<T> dripping, ParticleType<T> falling, ParticleType<T> landing, Fluid fluid, int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        registerBlockLeak(dripping, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.Dripping(world, x, y, z, fluid, (ParticleEffect) falling);
            particle.setColor(red, green, blue);
            return particle;
        });
        registerBlockLeak(falling, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.ContinuousFalling(world, x, y, z, fluid, (ParticleEffect) landing);
            particle.setColor(red, green, blue);
            return particle;
        });
        registerBlockLeak(landing, (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            GasBlockLeakParticle particle = new GasBlockLeakParticle.Landing(world, x, y, z, fluid);
            particle.setColor(red, green, blue);
            return particle;
        });
    }

    public static <T extends ParticleEffect> void registerBlockLeak(ParticleType<T> type, ParticleFactory.BlockLeakParticleFactory<T> factory) {
        ParticleFactoryRegistry.getInstance().register(type, (sprite) -> (typex, world, x, y, z, velocityX, velocityY, velocityZ) -> {
            SpriteBillboardParticle particle = factory.createParticle(typex, world, x, y, z, velocityX, velocityY, velocityZ);
            if (particle != null) particle.setSprite(sprite);
            return particle;
        });
    }
}
