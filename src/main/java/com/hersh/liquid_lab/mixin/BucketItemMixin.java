package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow @Final private Fluid fluid;

    @ModifyExpressionValue(method = "placeFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/Fluid;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean shouldEvaporate(boolean original) {
        return original || this.fluid.isIn(ModFluidTags.EVAPORATES);
    }

    @ModifyVariable(method = "playEmptyingSound", at = @At("STORE"))
    private SoundEvent playEmptyingSound(SoundEvent original) {
        return this.fluid.getBucketEmptySound().orElse(original);
    }
}
