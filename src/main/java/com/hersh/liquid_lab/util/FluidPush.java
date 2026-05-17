package com.hersh.liquid_lab.util;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Vec3d;

public class FluidPush {
    public final FluidState fluid;
    public final double pushForce;
    private Vec3d totalVelocity;
    private double height;
    private int count;

    public FluidPush(FluidState fluid, double pushForce) {
        this.fluid = fluid;
        this.pushForce = pushForce;
        this.totalVelocity = Vec3d.ZERO;
        this.height = 0.0;
        this.count = 0;
    }

    public void addAffectingFluid(double height, Vec3d velocity) {
        this.height = Math.max(this.height, height);
        if (this.height < 0.4) {
            velocity = velocity.multiply(this.height);
        }
        this.totalVelocity = this.totalVelocity.add(velocity);
        this.count++;
    }

    public Vec3d getTotalVelocity() {
        if (count == 0) return Vec3d.ZERO;
        return totalVelocity.multiply(1.0 / count);
    }

    public double getHeight() {
        return this.height;
    }
}
