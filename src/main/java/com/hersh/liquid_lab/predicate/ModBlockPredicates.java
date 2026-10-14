package com.hersh.liquid_lab.predicate;

import com.hersh.liquid_lab.LiquidLab;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class ModBlockPredicates {
    public static final BlockPredicateType<TouchingFluidsBlockPredicate> TOUCHING_FLUIDS = register("touching_fluids", TouchingFluidsBlockPredicate.CODEC);
    public static final BlockPredicateType<AdjacentBlocksBlockPredicate> ADJACENT_BLOCKS = register("adjacent_blocks", AdjacentBlocksBlockPredicate.CODEC);

    private static <P extends BlockPredicate> BlockPredicateType<P> register(String name, MapCodec<P> codec) {
        return Registry.register(Registries.BLOCK_PREDICATE_TYPE, LiquidLab.id(name), () -> codec);
    }

    public static void registerAll() {
    }
}
