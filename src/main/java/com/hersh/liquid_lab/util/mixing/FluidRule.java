package com.hersh.liquid_lab.util.mixing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.Optional;

public record FluidRule(boolean smoke, MixType type, Optional<Boolean> isStill, BlockPredicate predicate, BlockStateProvider transform) {
    public static final Codec<FluidRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("smoke").orElse(true).forGetter(FluidRule::smoke),
            MixType.CODEC.fieldOf("type").forGetter(FluidRule::type),
            Codec.BOOL.optionalFieldOf("is_still").forGetter(FluidRule::isStill),
            BlockPredicate.BASE_CODEC.fieldOf("predicate").forGetter(FluidRule::predicate),
            BlockStateProvider.TYPE_CODEC.fieldOf("transform").forGetter(FluidRule::transform)
    ).apply(instance, FluidRule::new));

    public boolean matchesFluid(boolean isStill, MixType type) {
        return this.type == type && (this.isStill.isEmpty() || this.isStill.get() == isStill);
    }
}
