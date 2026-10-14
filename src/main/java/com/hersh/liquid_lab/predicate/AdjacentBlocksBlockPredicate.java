package com.hersh.liquid_lab.predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public record AdjacentBlocksBlockPredicate(RegistryEntryList<Block> blocks) implements BlockPredicate {
    public static final MapCodec<AdjacentBlocksBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("blocks").forGetter(AdjacentBlocksBlockPredicate::blocks)
    ).apply(instance, AdjacentBlocksBlockPredicate::new));

    @Override
    public boolean test(StructureWorldAccess world, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState blockState = world.getBlockState(pos.offset(dir));
            if (blockState.isIn(this.blocks)) return true;
        }
        return false;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return ModBlockPredicates.ADJACENT_BLOCKS;
    }
}
