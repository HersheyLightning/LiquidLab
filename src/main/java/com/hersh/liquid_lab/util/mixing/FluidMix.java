package com.hersh.liquid_lab.util.mixing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record FluidMix(boolean replace, List<FluidRule> rules) {
    public static final Codec<FluidMix> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("replace").orElse(false).forGetter(FluidMix::replace),
            FluidRule.CODEC.listOf().fieldOf("rules").forGetter(FluidMix::rules)
    ).apply(instance, FluidMix::new));
}
