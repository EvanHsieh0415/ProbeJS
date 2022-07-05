package com.probejs.formatter.formatter;

import com.probejs.ProbeJS;
import com.probejs.util.PlatformSpecial;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterIngredient implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<ResourceLocation> ingredients = PlatformSpecial.INSTANCE.get().getIngredientTypes();
        if (ingredients.isEmpty())
            return List.of("%stype Ingredient = string;".formatted(" ".repeat(indent)));
        return List.of("%stype Ingredient = %s;".formatted(" ".repeat(indent),
                ingredients
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | ")))
        );
    }
}
