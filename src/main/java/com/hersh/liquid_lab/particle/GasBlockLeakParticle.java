package com.hersh.liquid_lab.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class GasBlockLeakParticle extends SpriteBillboardParticle {
    private final Fluid fluid;

    GasBlockLeakParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z);
        this.setBoundingBoxSpacing(0.01F, 0.01F);
        this.gravityStrength = 0.06F;
        this.fluid = fluid;
    }

    protected Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        this.updateAge();
        if (this.dead) return;
        this.velocityY += this.gravityStrength;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.updateVelocity();
        if (this.dead) return;
        this.velocityX *= 0.98F;
        this.velocityY *= 0.98F;
        this.velocityZ *= 0.98F;
        if (this.fluid != Fluids.EMPTY) {
            BlockPos blockPos = BlockPos.ofFloored(this.x, this.y, this.z);
            FluidState fluidState = this.world.getFluidState(blockPos);
            if (fluidState.getFluid() == this.fluid && this.y > blockPos.getY() + 1 - fluidState.getHeight(this.world, blockPos)) {
                this.markDead();
            }
        }
    }

    protected void updateAge() {
        if (this.maxAge-- <= 0) {
            this.markDead();
        }
    }

    protected void updateVelocity() {
    }

    @Environment(EnvType.CLIENT)
    public static class Dripping extends GasBlockLeakParticle {
        private final ParticleEffect nextParticle;

        public Dripping(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
            super(world, x, y, z, fluid);
            this.nextParticle = nextParticle;
            this.gravityStrength *= 0.02F;
            this.maxAge = 40;
        }

        @Override
        protected void updateAge() {
            if (this.maxAge-- <= 0) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, this.velocityX, this.velocityY, this.velocityZ);
            }
        }

        @Override
        protected void updateVelocity() {
            this.velocityX *= 0.02;
            this.velocityY *= 0.02;
            this.velocityZ *= 0.02;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ContinuousFalling extends GasBlockLeakParticle.Falling {
        protected final ParticleEffect nextParticle;

        public ContinuousFalling(ClientWorld world, double x, double y, double z, Fluid fluid, ParticleEffect nextParticle) {
            super(world, x, y, z, fluid);
            this.nextParticle = nextParticle;
        }

        @Override
        protected void updateVelocity() {
            if (this.stopped) {
                this.markDead();
                this.world.addParticle(this.nextParticle, this.x, this.y, this.z, 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    static class Falling extends GasBlockLeakParticle {
        Falling(ClientWorld clientWorld, double d, double e, double f, Fluid fluid) {
            this(clientWorld, d, e, f, fluid, (int)((double)64.0F / (Math.random() * 0.8 + 0.2)));
        }

        Falling(ClientWorld world, double x, double y, double z, Fluid fluid, int maxAge) {
            super(world, x, y, z, fluid);
            this.maxAge = maxAge;
        }

        @Override
        protected void updateVelocity() {
            if (this.stopped) {
                this.markDead();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Landing extends GasBlockLeakParticle {
        public Landing(ClientWorld clientWorld, double d, double e, double f, Fluid fluid) {
            super(clientWorld, d, e, f, fluid);
            this.maxAge = (int)((double)16.0F / (Math.random() * 0.8 + 0.2));
        }
    }
}
