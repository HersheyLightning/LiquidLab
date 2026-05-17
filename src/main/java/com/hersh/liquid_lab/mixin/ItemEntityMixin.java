package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.util.FluidMotion;
import com.hersh.liquid_lab.util.FluidUtils;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;applyGravity()V"))
    private boolean applyFluidBuoyancy(ItemEntity entity, @Local Vec3d velocity) {
        Set<Fluid> updatedFluids = new HashSet<>();
        Box box = entity.getBoundingBox().contract(0.001, 0.101, 0.001);
        return !FluidUtils.forFluidsInBox(entity.getWorld(), box, (blockPos, fluidState) -> {
            if (updatedFluids.stream().anyMatch(fluid -> fluid.matchesType(fluidState.getFluid()))) return false;
            FluidMotion properties = fluidState.getFluid().getItemMotion();
            if (properties == null) return false;
            entity.setVelocity(velocity.x * properties.viscosity.x, velocity.y * properties.viscosity.y + (velocity.y < properties.pushForce ? properties.movementSpeed : 0.0F), velocity.z * properties.viscosity.z);
            updatedFluids.add(fluidState.getFluid());
            return true;
        });
    }
}
