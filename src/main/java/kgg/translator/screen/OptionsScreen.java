package kgg.translator.screen;

import kgg.translator.option.Option;
import kgg.translator.option.ScreenOption;
import kgg.translator.option.WorldOption;
import kgg.translator.option.ChatOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

public class OptionsScreen extends SimpleOptionsScreen {
    public OptionsScreen() {
        super(null, MinecraftClient.getInstance().options, Text.literal("翻译选项"),
                new SimpleOption[]{
                        createOption(ChatOption.autoChat),
                        createOption(ChatOption.chatTip),
                        createOptionAndTooltip(ScreenOption.autoTitle),
                        createOption(ScreenOption.autoScoreboard),
                        createOption(ScreenOption.autoBossBar),
                        createOptionAndTooltip(ScreenOption.autoTooltip),
                        createOption(WorldOption.autoEntityName),
                        createOption(WorldOption.autoSign),
                        createOptionAndTooltip(ScreenOption.screenTranslate),
                        createOptionAndTooltip(WorldOption.signCombine)
                });
    }

    public static SimpleOption<Boolean> createOption(Option option) {
        return SimpleOption.ofBoolean("translator.option." + option.name, option.isEnable(), option::setEnable);
    }

    public static SimpleOption<Boolean> createOptionAndTooltip(Option option) {
        return SimpleOption.ofBoolean("translator.option." + option.name,
                (b) -> Tooltip.of(Text.translatable("translator.option." + option.name + ".desc")),
                option.isEnable(), option::setEnable);
    }
}
