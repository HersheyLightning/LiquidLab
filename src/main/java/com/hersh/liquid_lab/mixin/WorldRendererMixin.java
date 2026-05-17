package com.hersh.liquid_lab.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyVariable(method = "tickRainSplashing", at = @At("STORE"))
    private ParticleEffect setRainParticle(ParticleEffect original, @Local FluidState fluidState) {
        if (!fluidState.isEmpty()) {
            ParticleEffect particle = fluidState.getFluid().getRainParticle();
            if (particle != null) return particle;
        }
        return original;
    }
}
