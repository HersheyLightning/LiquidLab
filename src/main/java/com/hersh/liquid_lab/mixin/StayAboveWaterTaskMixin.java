package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StayAboveWaterTask.class)
public abstract class StayAboveWaterTaskMixin {

    @ModifyReturnValue(method = "isUnderwater", at = @At("TAIL"))
    private static boolean isSubmerged(boolean original, MobEntity entity) {
        return original || entity.getFluidHeight(ModFluidTags.SWIMMABLE) > entity.getSwimHeight();
    }
}
