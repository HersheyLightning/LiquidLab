package com.hersh.liquid_lab.util.mixing;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum MixType implements StringIdentifiable {
    ON_UPDATE("on_update"),
    ON_FLOW("on_flow");

    public static final Codec<MixType> CODEC = StringIdentifiable.createCodec(MixType::values);
    private final String name;

    MixType(final String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
