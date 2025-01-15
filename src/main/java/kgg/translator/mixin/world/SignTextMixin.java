package kgg.translator.mixin.world;

import kgg.translator.handler.SignHelper;
import kgg.translator.handler.TranslateHelper;
import kgg.translator.option.Options;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Function;

@Mixin(SignText.class)
public abstract class SignTextMixin {
    @Shadow @Nullable public OrderedText[] orderedMessages;
    @Shadow private boolean filtered;
    @Shadow public abstract Text getMessage(int line, boolean filtered);

    @Unique
    private boolean autoSign = false;
    @Unique
    private boolean signCombine = false;
    @Unique
    private boolean updated = false;
    @Unique
    private boolean translate;

    /**
     * @author KGG_Xing_Kong
     * @reason 翻译
     * 按照原本的getOrderedMessages的逻辑，只有在文本发生变更时才更改orderedMessages
     */
    @Overwrite
    public OrderedText[] getOrderedMessages(boolean filtered, Function<Text, OrderedText> messageOrderer) {
        updateFlags();

        if (orderedMessages == null || filtered!= this.filtered) {
            this.filtered = filtered;
            orderedMessages = new OrderedText[4];
            if (autoSign && !(MinecraftClient.getInstance().currentScreen instanceof AbstractSignEditScreen) && translate) {
                if (signCombine) {
                    handleCombinedTranslation();
                } else {
                    handleLineByLineTranslation();
                }
            } else {
                for (int i = 0; i < 4; ++i) {
                    Text message = getMessage(i, filtered);
                    orderedMessages[i] = messageOrderer.apply(message);
                }
            }
        }
        return orderedMessages;
    }

    @Unique
    private void updateFlags() {
        if (autoSign!= Options.autoSign.getValue() || signCombine!= Options.signCombine.getValue() || translate != SignHelper.translate) {
            autoSign = Options.autoSign.getValue();
            signCombine = Options.signCombine.getValue();
            translate = SignHelper.translate;
            updated = true;
        }
        if (updated) {
            updated = false;
            orderedMessages = null;
        }
    }

    @Unique
    private void handleCombinedTranslation() {
        MutableText text = getMessage(0, filtered).copy();
        for (int i = 1; i < 4; ++i) {
            text.append(getMessage(i, filtered));
        }
        Text combinedMessage = TranslateHelper.translateNoWait(text, t -> updated = true);

        List<OrderedText> list = MinecraftClient.getInstance().textRenderer.wrapLines(combinedMessage, SignHelper.lineWidth);
        for (int i = 0; i < 4; ++i) {
            if (i < list.size()) {
                orderedMessages[i] = list.get(i);
            } else {
                orderedMessages[i] = OrderedText.empty();
            }
        }
    }

    @Unique
    private void handleLineByLineTranslation() {
        for (int i = 0; i < 4; ++i) {
            Text message = getMessage(i, filtered);
            message = TranslateHelper.translateNoWait(message, t -> updated = true);
            orderedMessages[i] = message.asOrderedText();
        }
    }
}