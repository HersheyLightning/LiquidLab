package com.hersh.liquid_lab.util;

import net.minecraft.util.math.Vec3d;

public class FluidMotion {
    public final Vec3d viscosity;
    public final float pushForce;
    public final float movementSpeed;

    public FluidMotion(Vec3d viscosity, float pushForce, float movementSpeed) {
        this.viscosity = viscosity;
        this.pushForce = pushForce;
        this.movementSpeed = movementSpeed;
    }

    public static FluidMotion ofEntityMotion(Vec3d viscosity, float pushForce) {
        return new FluidMotion(viscosity, pushForce, 0.02F);
    }

    public static FluidMotion ofItemMotion(Vec3d viscosity) {
        return new FluidMotion(viscosity, 0.06F, 0.0005F);
    }
}
