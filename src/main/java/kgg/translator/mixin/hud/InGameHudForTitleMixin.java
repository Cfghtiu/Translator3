package kgg.translator.mixin.hud;

import kgg.translator.handler.TranslateHelper;
import kgg.translator.option.Options;
import kgg.translator.translator.Source;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameHud.class)
public abstract class InGameHudForTitleMixin {
    @ModifyArg(method = "renderTitleAndSubtitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"), index = 0)
    public StringVisitable getWidthTitle(StringVisitable text) {
        if (!Options.autoTitle.getValue()) return text;
        return TranslateHelper.translateNoWait((Text) text, Source.TITLE);
    }

    @ModifyArg(method = "renderTitleAndSubtitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"), index = 1)
    public Text renderTitle(Text text) {
        if (!Options.autoTitle.getValue()) return text;
        return TranslateHelper.translateNoWait(text, Source.TITLE);
    }

    @ModifyArg(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"), index = 0)
    public StringVisitable getWidthOverlay(StringVisitable text) {
        if (!Options.autoTitle.getValue()) return text;
        return TranslateHelper.translateNoWait((Text) text, Source.TITLE);
    }

    @ModifyArg(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"), index = 1)
    public Text renderOverlay(Text text) {
        if (!Options.autoTitle.getValue()) return text;
        return TranslateHelper.translateNoWait(text, Source.TITLE);
    }
}
