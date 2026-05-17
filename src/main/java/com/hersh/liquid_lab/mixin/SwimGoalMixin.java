package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SwimGoal.class)
public abstract class SwimGoalMixin {
    @Shadow @Final private MobEntity mob;

    @ModifyReturnValue(method = "canStart", at = @At("TAIL"))
    private boolean isSubmerged(boolean original) {
        return original || mob.getFluidHeight(ModFluidTags.SWIMMABLE) > mob.getSwimHeight();
    }
}
