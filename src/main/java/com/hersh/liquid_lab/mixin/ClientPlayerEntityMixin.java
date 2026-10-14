package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.fluid.ModFluidTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
    private boolean touchingDiveableFluid(boolean original) {
        ClientPlayerEntity entity = (ClientPlayerEntity)(Object) this;
        return original || entity.getFluidHeight(ModFluidTags.ENHANCED_MOBILITY) > 0.0;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedInWater()Z"))
    private boolean submergedDiveableFluid(boolean original) {
        ClientPlayerEntity entity = (ClientPlayerEntity)(Object) this;
        return original || entity.isSubmergedIn(ModFluidTags.ENHANCED_MOBILITY);
    }
}
