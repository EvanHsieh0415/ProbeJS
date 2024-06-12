package moe.wolfgirl.probejs.specials;

import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.ProbeCommands;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.ProbePaths;
import moe.wolfgirl.probejs.specials.special.FormatterLang;
import moe.wolfgirl.probejs.specials.special.FormatterLootTable;
import moe.wolfgirl.probejs.specials.special.FormatterRecipeId;
import moe.wolfgirl.probejs.specials.special.FormatterTag;
import moe.wolfgirl.probejs.features.plugin.DocGenerationEventJS;
import moe.wolfgirl.probejs.util.json.JArray;
import moe.wolfgirl.probejs.util.json.JObject;
import moe.wolfgirl.probejs.util.json.JPrimitive;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.bindings.ItemWrapper;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SnippetCompiler {
    public static class KubeDump {
        public Map<String, Map<String, List<String>>> tags;
        public Map<String, List<String>> registries;

        public KubeDump(Map<String, Map<String, List<String>>> tags, Map<String, List<String>> registries) {
            this.tags = tags;
            this.registries = registries;
        }

        private static void putTag(Map<String, Map<String, List<String>>> tags, String type, Registry<?> registry) {
            tags.put(type, FormatterTag.getTagsFromRegistry(registry).stream().collect(Collectors.toMap(s -> s, s -> new ArrayList<>())));
        }

        private static <T> void putRegistry(Map<String, List<String>> registries, String type, ResourceKey<Registry<T>> registry) {
            registries.put(type, ProbeCommands.getRegistry(registry).keySet().stream().map(ResourceLocation::toString).toList());
        }

        @SuppressWarnings("unchecked")
        public static <T> KubeDump fetch() {
            Map<String, Map<String, List<String>>> tags = new HashMap<>();
            Map<String, List<String>> registries = new HashMap<>();
            ProbeCommands.COMMAND_LEVEL.registryAccess().registries().forEach(registry -> {
                ResourceKey<? extends Registry<?>> key = registry.key();
                String[] paths = key.location().getPath().split("/");
                String name = paths[paths.length - 1];
                putTag(tags, name, registry.value());
                putRegistry(registries, name, (ResourceKey<Registry<T>>) key);
            });
            return new KubeDump(tags, registries);
        }

        @Override
        public String toString() {
            return "KubeDump{" +
                    "tags=" + tags +
                    ", registries=" + registries +
                    '}';
        }

        private static void addSnippets(JsonObject resultJson, String type, Collection<String> members) {
            if (!members.isEmpty()) {
                resultJson.add(type, JObject.create()
                        .add("prefix", JArray.create().add(new JPrimitive("@" + type)))
                        .add("body", new JPrimitive("\"${1|%s|}\"".formatted(String.join(",", members))))
                        .serialize()
                );
            }
        }

        private static void addRecipeSnippets(JsonObject resultJson) {
            FormatterRecipeId.ORIGINAL_RECIPES.forEach((rl, json) -> {
                if (json instanceof JsonObject) {
                    resultJson.add(rl.toString(), JObject.create()
                            .add("prefix", JArray.create().add(new JPrimitive("#" + rl)))
                            .add("body", new JPrimitive(ProbeJS.GSON_WRITER.toJson(json)))
                            .serialize());
                }
            });
        }

        public JsonObject toSnippet(DocGenerationEventJS event) {
            JsonObject resultJson = new JsonObject();
            // Compile normal entries to snippet
            for (Map.Entry<String, List<String>> entry : this.registries.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue();
                addSnippets(resultJson, type, members);
            }

            // Compile tag entries to snippet
            for (Map.Entry<String, Map<String, List<String>>> entry : this.tags.entrySet()) {
                String type = entry.getKey();
                List<String> members = entry.getValue().keySet().stream().toList();
                addSnippets(resultJson, type + "_tag", members);
            }
            addSnippets(resultJson, "loot_table", FormatterLootTable.LOOT_TABLES.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
            addSnippets(resultJson, "advancements", ProbeCommands.COMMAND_LEVEL.getServer()
                    .getAdvancements()
                    .getAllAdvancements()
                    .stream()
                    .map(Advancement::getId)
                    .map(ResourceLocation::toString).collect(Collectors.toList()));
            addSnippets(resultJson, "mod", Platform.getModIds());
            addSnippets(resultJson, "lang_key", FormatterLang.getLangKeys(LanguageManager.DEFAULT_LANGUAGE_CODE).map(Map.Entry::getKey).toList());
            resultJson.add("itemstack", JObject.create()
                    .add("prefix", JArray.create().add(new JPrimitive("@itemstack")))
                    .add("body", new JPrimitive("\"${1}x ${2|%s|}\"".formatted(String.join(",", ItemWrapper.getTypeList()))))
                    .serialize());
            if (!ProbeConfig.INSTANCE.disableRecipeJsonDump)
                addRecipeSnippets(resultJson);

            event.getSnippets().forEach(consumer -> consumer.accept(resultJson));
            return resultJson;
        }

    }

    public static void compile(DocGenerationEventJS event) throws IOException {
        Path codeFile = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
        KubeDump kubeDump = KubeDump.fetch();
        BufferedWriter writer = Files.newBufferedWriter(codeFile);
        writer.write(ProbeJS.GSON.toJson(kubeDump.toSnippet(event)));
        writer.close();
    }

}
