package com.hersh.liquid_lab.predicate;

import com.hersh.liquid_lab.fluid.FlowableGasFluid;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public record TouchingFluidsBlockPredicate(RegistryEntryList<Fluid> fluids) implements BlockPredicate {
    public static final MapCodec<TouchingFluidsBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.FLUID).fieldOf("fluids").forGetter(TouchingFluidsBlockPredicate::fluids)
    ).apply(instance, TouchingFluidsBlockPredicate::new));

    @Override
    public boolean test(StructureWorldAccess world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            FluidState fluidState = world.getFluidState(pos.offset(dir));
            if (fluidState.getFluid() instanceof FlowableGasFluid) {
                if (dir == Direction.UP) continue;
            } else if (dir == Direction.DOWN) continue;

            if (fluidState.isIn(this.fluids)) return true;
        }
        return false;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return ModBlockPredicates.TOUCHING_FLUIDS;
    }
}
