package com.hersh.liquid_lab.mixin;

import com.hersh.liquid_lab.util.FluidUtils;
import com.hersh.liquid_lab.util.FogProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyVariable(method = "getFov", at = @At("STORE"))
    private CameraSubmersionType getFov(CameraSubmersionType original, Camera camera) {
        Entity entity = camera.getFocusedEntity();
        if (FluidUtils.firstFluidInCamera(camera, (fluidState) -> {
            FogProperties properties = fluidState.getFluid().getFogProperties(entity.getWorld(), camera.getPos(), entity);
            return properties != null;
        })) {
            return CameraSubmersionType.WATER;
        }
        return original;
    }
}
