package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.hersh.liquid_lab.util.FluidMotion;
import com.hersh.liquid_lab.util.FluidPush;
import com.hersh.liquid_lab.util.FluidUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract boolean isRegionUnloaded();
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract World getWorld();
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract void setVelocity(Vec3d velocity);
    @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
    @Shadow public abstract boolean isPushedByFluids();
    @Shadow protected boolean firstUpdate;
    @Shadow public abstract double getFluidHeight(TagKey<Fluid> fluid);
    @Shadow public float fallDistance;
    @Shadow public abstract boolean isInLava();
    @Shadow public abstract boolean isSubmergedIn(TagKey<Fluid> fluidTag);
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract boolean hasVehicle();
    @Shadow private BlockPos blockPos;
    @Shadow public abstract void onLanding();

    @Unique private Set<Fluid> touchingFluids = new HashSet<>();

    @ModifyReturnValue(method = "isInFluid", at = @At("TAIL"))
    private boolean isInFluid(boolean original) {
        return original || !firstUpdate && fluidHeight.values().doubleStream().anyMatch(height -> height > 0.0);
    }

    @Inject(method = "updateWaterState", at = @At("TAIL"))
    private void pushInFluids(CallbackInfoReturnable<Boolean> cir) {
        if (this.isRegionUnloaded()) return;
        Box box = this.getBoundingBox().contract(0.001);
        Set<FluidPush> pushStates = new HashSet<>();

        FluidUtils.forFluidsInBox(this.getWorld(), box, (blockPos, fluidState) -> {
            FluidMotion properties = fluidState.getFluid().getFluidMotion(this.getWorld(), (Entity)(Object) this);
            if (properties == null) return false;

            FluidPush pushState = pushStates.stream()
                    .filter(push -> push.fluid.getFluid().matchesType(fluidState.getFluid()))
                    .findAny()
                    .orElseGet(() -> {
                        FluidPush push = new FluidPush(fluidState, properties.pushForce);
                        pushStates.add(push);
                        return push;
                    });
            double height = fluidState.getHeight(this.getWorld(), blockPos);
            double offsetHeight;
            if (fluidState.getFluid() instanceof FlowableGasFluid) {
                offsetHeight = box.maxY - blockPos.getY() + 1 - height;
            } else {
                offsetHeight = blockPos.getY() + height - box.minY;
            }
            pushState.addAffectingFluid(offsetHeight, fluidState.getVelocity(this.getWorld(), blockPos));
            pushState.fluid.streamTags().forEach(tag ->
                    this.fluidHeight.put(tag, Math.max(this.fluidHeight.getDouble(tag), pushState.getHeight()))
            );
            return true;
        });

        for (FluidPush pushState : pushStates) {
            if (this.touchingFluids.stream().noneMatch(fluid -> pushState.fluid.getFluid().matchesType(fluid))) {
                pushState.fluid.getFluid().onEntityEntry(this.getWorld(), (Entity)(Object) this);
            }

            if (!this.isPushedByFluids()) continue;
            Vec3d velocity = pushState.getTotalVelocity();
            if (!((Entity)(Object) this instanceof PlayerEntity)) {
                velocity = velocity.normalize();
            }
            velocity = velocity.multiply(pushState.pushForce);
            if (Math.abs(this.getVelocity().x) < 0.003 && Math.abs(this.getVelocity().z) < 0.003 && velocity.length() < 0.0045000000000000005) {
                velocity = velocity.normalize().multiply(0.0045000000000000005);
            }
            this.setVelocity(this.getVelocity().add(velocity));
        }
        if (this.getFluidHeight(ModFluidTags.FALL_DAMAGE_RESETTING) > 0.0) this.onLanding();

        this.touchingFluids = pushStates.stream()
                .map(pushState -> pushState.fluid.getFluid())
                .collect(Collectors.toSet());
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attemptTickInVoid()V"))
    private void reduceFall(CallbackInfo ci) {
        if (!this.isInLava() && this.getFluidHeight(ModFluidTags.FALL_DAMAGE_REDUCING) > 0.0) this.fallDistance *= 0.5F;
    }

    @Inject(method = "checkBlockCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos$Mutable;set(III)Lnet/minecraft/util/math/BlockPos$Mutable;", shift = At.Shift.AFTER))
    private void checkFluidCollision(CallbackInfo ci, @Local Box box, @Local BlockPos.Mutable pos) {
        if (!FluidUtils.canBoxSubmergeIn(this.getWorld(), pos, box)) return;
        Fluid fluid = this.getWorld().getFluidState(pos).getFluid();

        try {
            fluid.onEntityCollision(this.getWorld(), pos, (Entity)(Object) this);
        } catch (Throwable error) {
            CrashReport crashReport = CrashReport.create(error, "Colliding entity with fluid");
            CrashReportSection crashReportSection = crashReport.addElement("Fluid being collided with");
            CrashReportSection.addBlockInfo(crashReportSection, this.getWorld(), pos, null);
            throw new CrashException(crashReport);
        }
    }

    @ModifyExpressionValue(method = "isCrawling", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isTouchingWater()Z"))
    private boolean touchingDiveableFluid(boolean original) {
        return original || this.getFluidHeight(ModFluidTags.DIVEABLE) > 0.0;
    }

    @ModifyArg(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setSwimming(Z)V", ordinal = 0))
    private boolean swimmingSwimCondition(boolean original) {
        return original || this.isSprinting() && this.getFluidHeight(ModFluidTags.DIVEABLE) > 0.0 && !this.hasVehicle();
    }
    @ModifyArg(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setSwimming(Z)V", ordinal = 1))
    private boolean notSwimmingSwimCondition(boolean original) {
        return original || this.isSprinting() && this.isSubmergedIn(ModFluidTags.DIVEABLE) && !this.hasVehicle() && this.getWorld().getFluidState(this.blockPos).isIn(ModFluidTags.DIVEABLE);
    }
}