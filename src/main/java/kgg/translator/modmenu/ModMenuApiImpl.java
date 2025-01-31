package kgg.translator.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import kgg.translator.TranslatorConfig;
import kgg.translator.TranslatorManager;
import kgg.translator.translator.Translator;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuApiImpl::createScreen;
    }

    public static Screen createScreen(Screen p) {
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("translator.modmenu.title")).setParentScreen(p);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("translator.modmenu.title"));
        // 当前翻译器
        DropdownBoxEntry<Translator> listEntry = entryBuilder.startDropdownMenu(Text.translatable("translator.modmenu.current"),
                TranslatorManager.getCurrent(),
                s -> TranslatorManager.getTranslators().stream().filter(t -> t.getName().equals(s)).findFirst().orElse(null),
                t -> Text.of(t.getName())
            ).setSelections(TranslatorManager.getTranslators())
            .setSaveConsumer(TranslatorManager::setTranslator)
            .build();
        category.addEntry(listEntry);
        // From To
        category.addEntry(entryBuilder.startStrField(Text.translatable("translator.modmenu.from"), TranslatorManager.getFrom())
            .setSaveConsumer(TranslatorManager::setFrom)
            .setTooltip(Text.translatable("translator.modmenu.suggestion"))
            .build());
        category.addEntry(entryBuilder.startStrField(Text.translatable("translator.modmenu.to"), TranslatorManager.getTo())
            .setSaveConsumer(TranslatorManager::setTo)
            .setTooltip(Text.translatable("translator.modmenu.suggestion"))
            .build());
        // 翻译器配置
        List<Runnable> runnables = new ArrayList<>(TranslatorManager.getTranslators().size());
        for (Translator translator : TranslatorManager.getTranslators()) {
            if (translator instanceof ModMenuConfigurable configurable) {
                // 为每个翻译器创建一个类别
                SubCategoryBuilder tranCategory = entryBuilder.startSubCategory(Text.literal(translator.getName()));
                runnables.add(configurable.registerEntry(entryBuilder, tranCategory));
                category.addEntry(tranCategory.build());
            }
        }
        builder.setSavingRunnable(() -> {
            runnables.forEach(Runnable::run);
            TranslatorConfig.writeFile();
        });
        return builder.build();
    }
}
