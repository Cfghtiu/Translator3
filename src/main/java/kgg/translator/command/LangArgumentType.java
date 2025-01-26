package kgg.translator.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kgg.translator.Language;
import kgg.translator.TranslatorManager;
import kgg.translator.util.EasyProperties;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LangArgumentType implements ArgumentType<String> {
    public static LangArgumentType lang() {
        return new LangArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readQuotedString();
    }

    public static String getLanguage(CommandContext<FabricClientCommandSource> context, String name) {
        String string = context.getArgument(name, String.class);
//        EasyProperties properties = TranslatorManager.getCurrent().getLanguageProperties();
        for (Map.Entry<String, String> entry : Language.translatorMap.get(TranslatorManager.getCurrent().getName()).entrySet()) {
            String s = Text.translatable("language." + entry.getKey()).getString();
            if (s.equals(string)) return entry.getValue();
        }
//        if (!properties.containsKey(string)) return string;
//        String lang = properties.getProperty(string);
//        if (lang != null) return lang;
        return string;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSource) {
            Set<String> support = Language.getTranslatorSupport(TranslatorManager.getCurrent().getName());
            return CommandSource.suggestMatching(support.stream().map(c -> '"' + Text.translatable("language."+c).getString() + '"').toList(), builder);
        }
        return Suggestions.empty();
    }

}
