package com.hersh.liquid_lab;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hersh.liquid_lab.predicate.ModBlockPredicates;
import com.hersh.liquid_lab.util.FluidUtils;
import com.hersh.liquid_lab.util.mixing.FluidMix;
import com.hersh.liquid_lab.util.mixing.FluidRule;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiquidLab implements ModInitializer {
	public static final String MOD_ID = "liquid_lab";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier FLUID_MIX_LOADER = LiquidLab.id("fluid_mix");

	@Override
	public void onInitialize() {
        ModBlockPredicates.registerAll();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FLUID_MIX_LOADER, registryLookup -> new SimpleSynchronousResourceReloadListener() {

            @Override
            public Identifier getFabricId() {
                return FLUID_MIX_LOADER;
            }

            @Override
            public void reload(ResourceManager manager) {
                FluidUtils.fluidRules.clear();
                ResourceFinder resourceFinder = ResourceFinder.json("fluid_mixing");
                RegistryOps<JsonElement> registryOps = registryLookup.getOps(JsonOps.INSTANCE);

                for(Map.Entry<Identifier, List<Resource>> entry : resourceFinder.findAllResources(manager).entrySet()) {
                    TagKey<Fluid> tag = TagKey.of(RegistryKeys.FLUID, resourceFinder.toResourceId(entry.getKey()));

                    for (Resource resource : entry.getValue()) {
                        try (Reader reader = resource.getReader()) {
                            registryLookup.getWrapperOrThrow(RegistryKeys.FLUID).getOrThrow(tag);
                            FluidMix fluidMix = FluidMix.CODEC.parse(registryOps, JsonParser.parseReader(reader)).getOrThrow();
                            if (fluidMix.replace()) {
                                FluidUtils.fluidRules.put(tag, fluidMix.rules());
                            } else {
                                FluidUtils.fluidRules.merge(tag, fluidMix.rules(), (original, rules) -> {
                                    List<FluidRule> combined = new ArrayList<>(original);
                                    combined.addAll(0, rules);
                                    return combined;
                                });
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error parsing fluid mix {} in data pack {}", entry.getKey(), resource.getPackId(), e);
                        }
                    }
                }
            }
        });
	}

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
}