package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.util.FluidUtils;
import com.hersh.liquid_lab.util.FogProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Shadow private static float red;
    @Shadow private static float green;
    @Shadow private static float blue;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
    private static void setFogColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        FluidUtils.firstFluidInCamera(camera, (fluidState) -> {
            FogProperties properties = fluidState.getFluid().getFogProperties(entity.getWorld(), camera.getPos(), entity);
            if (properties == null) return false;
            red = properties.red;
            green = properties.green;
            blue = properties.blue;
            return true;
        });
    }

    @ModifyVariable(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", shift = At.Shift.BEFORE))
    private static BackgroundRenderer.FogData setFogDistance(BackgroundRenderer.FogData fogData, Camera camera, BackgroundRenderer.FogType fogType, float viewDistance) {
        Entity entity = camera.getFocusedEntity();
        FluidUtils.firstFluidInCamera(camera, (fluidState) -> {
            FogProperties properties = fluidState.getFluid().getFogProperties(entity.getWorld(), camera.getPos(), entity);
            if (properties == null) return false;
            fogData.fogStart = properties.fogStart;
            fogData.fogEnd = properties.fogEnd;
            FluidUtils.capFog(fogData, viewDistance, camera.getFocusedEntity().isSpectator());
            return true;
        });
        return fogData;
    }
}
