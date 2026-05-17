package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.hersh.liquid_lab.util.FluidMotion;
import com.hersh.liquid_lab.util.FluidUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected abstract boolean shouldSwimInFluids();
    @Shadow protected abstract void swimUpward(TagKey<Fluid> fluid);

    @Inject(method = "travel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), cancellable = true)
    private void sinkInFluids(Vec3d movementInput, CallbackInfo ci, @Local double gravity, @Local FluidState fluidState) {
        LivingEntity entity = (LivingEntity)(Object) this;

        Set<Fluid> updatedFluids = new HashSet<>();
        Box box = entity.getBoundingBox().contract(0.001);
        if (this.shouldSwimInFluids() && !entity.canWalkOnFluid(fluidState) && FluidUtils.forFluidsInBox(entity.getWorld(), box, (blockPos, fluidState2) -> {
            if (updatedFluids.stream().anyMatch(fluid -> fluid.matchesType(fluidState2.getFluid()))) return false;
            FluidMotion properties = fluidState2.getFluid().getFluidMotion(entity.getWorld(), entity);
            if (properties == null) return false;
            double lastY = entity.getY();
            entity.updateVelocity(properties.movementSpeed, movementInput);
            entity.move(MovementType.SELF, entity.getVelocity());
            Vec3d velocity = entity.getVelocity().multiply(properties.viscosity).subtract(0.0, gravity / 16.0, 0.0);
            if (entity.horizontalCollision && entity.doesNotCollide(velocity.x, velocity.y + 0.6F - entity.getY() + lastY, velocity.z)) {
                entity.setVelocity(velocity.x, 0.3F, velocity.z);
            } else {
                entity.setVelocity(velocity);
            }
            updatedFluids.add(fluidState2.getFluid());
            return true;
        })) {
            entity.updateLimbs(this instanceof Flutterer);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "tickMovement", slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInLava()Z", ordinal = 1)
    ), at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", ordinal = 1))
    public boolean modFluidSwim(boolean original) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (entity.getFluidHeight(ModFluidTags.SWIMMABLE) > 0.0 && (!entity.isOnGround() || entity.getFluidHeight(ModFluidTags.SWIMMABLE) > entity.getSwimHeight())) {
            this.swimUpward(null);
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean holdingBreath(boolean original) {
        LivingEntity entity = (LivingEntity)(Object) this;
        return original || entity.isSubmergedIn(ModFluidTags.DROWNS);
    }

    @Inject(method = "fall", at = @At("HEAD"))
    private void resetFall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object) this;

        if (entity.getFluidHeight(ModFluidTags.FALL_DAMAGE_RESETTING) > 0.0) return;
        Box box = entity.getBoundingBox().contract(0.001);
        FluidUtils.firstFluidInBox(entity.getWorld(), box, (blockPos, fluidState) -> {
            if (fluidState.isIn(ModFluidTags.FALL_DAMAGE_RESETTING)) {
                entity.onLanding();
                return true;
            }
            return false;
        });
    }
}
