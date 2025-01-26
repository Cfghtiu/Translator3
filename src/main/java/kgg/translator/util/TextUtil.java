package kgg.translator.util;

import net.minecraft.text.OrderedText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;

public class TextUtil {
    public static boolean isSystemText(Text text) {
        if (text.getContent() == Text.empty().getContent()) {
            for (Text sibling : text.getSiblings()) {
                if (sibling.getContent() instanceof TranslatableTextContent content) {
                    if (!Language.getInstance().hasTranslation(content.getKey())) {
                        return false;
                    }
                } else if (sibling.getContent() instanceof PlainTextContent content) {
                    if (!StringUtil.isBlank(content.string())) {
                        return false;
                    }
                }
            }
            return true;
        } else if (text.getContent() instanceof TranslatableTextContent content) {
            if (Language.getInstance().hasTranslation(content.getKey())) {
                for (Object arg : content.getArgs()) {
                    if (arg instanceof Text t) {
                        if (!isSystemText(t)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static Text toText(String text, Text origin) {
        return Text.literal(text).fillStyle(origin.getStyle());
    }

    public static String getString(Text text) {  // 方便增强模组重写
        return StringUtil.strip(text.getString());
    }

    public static String getString(OrderedText text) {
        StringBuffer sb = new StringBuffer();
        text.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }
}
