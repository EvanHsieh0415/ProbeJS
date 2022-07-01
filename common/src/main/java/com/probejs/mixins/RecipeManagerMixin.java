package com.probejs.mixins;

import com.google.gson.JsonObject;
import com.probejs.formatter.formatter.FormatterRecipeId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public class RecipeManagerMixin {
    @Inject(method = "apply*", at = @At("HEAD"))
    private void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        FormatterRecipeId.originalIds = map.keySet().stream().toList();
    }
}
