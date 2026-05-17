package com.hersh.liquid_lab.fluid;

import com.hersh.liquid_lab.LiquidLab;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModFluidTags {
    public static final TagKey<Fluid> SWIMMABLE = register("swimmable");
    public static final TagKey<Fluid> DIVEABLE = register("diveable");
    public static final TagKey<Fluid> DROWNS = register("drowns");
    public static final TagKey<Fluid> EVAPORATES = register("evaporates");
    public static final TagKey<Fluid> FALL_DAMAGE_RESETTING = register("fall_damage_resetting");
    public static final TagKey<Fluid> FALL_DAMAGE_REDUCING = register("fall_damage_reducing");
    public static final TagKey<Fluid> GASEOUS = register("gaseous");

    private static TagKey<Fluid> register(String name) {
        return TagKey.of(RegistryKeys.FLUID, Identifier.of(LiquidLab.MOD_ID, name));
    }
}