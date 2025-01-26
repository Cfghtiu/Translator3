package kgg.translator.handler;

import kgg.translator.Translate;
import kgg.translator.option.Options;
import kgg.translator.translator.Source;
import kgg.translator.util.StringUtil;
import kgg.translator.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

// todo 太丑了
public class ChatHandler {
    private static final Logger LOGGER = LogManager.getLogger(ChatHandler.class);

    private static final List<MutableText> translatingTexts = new CopyOnWriteArrayList<>();

    private static void refresh() {
        MinecraftClient.getInstance().inGameHud.getChatHud().refresh();
    }

    public static void addTip() {
        if (!Options.chatTip.getValue()) {  // 添加翻译提示
            return;
        }
        for (ChatHudLine message : MinecraftClient.getInstance().inGameHud.getChatHud().messages) {
            MutableText text = initText(message.content());
            addTip(text);
        }
        refresh();
    }

    private static void addTip(MutableText text) {
        if (TextUtil.isSystemText(text) || StringUtil.isBlank(text.getString())) {
            return;
        }

        if (translatingTexts.contains(text)) {
            text.append(" ").append(TRANSLATING_TIP);
        } else if (getTranslateClickEvent(text) == null) {
            text.append(" ").append(Text.literal("[翻译]").setStyle(Style.EMPTY
                    .withColor(TextColor.fromRgb(65522))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击翻译")))
                    .withClickEvent(new TranslateClickEvent(text))
                    .withInsertion(text.getString())));
        }
    }

    public static void removeTip() {
        for (ChatHudLine message : MinecraftClient.getInstance().inGameHud.getChatHud().messages) {
            MutableText text = initText(message.content());
            /*需要移除翻译提示的文本
            * 1.有翻译按钮
            * 2.没被点过（点过的是结果）*/
            TranslateClickEvent event = getTranslateClickEvent(text);
            if (event != null && !event.clicked) {
                text.siblings.removeLast();
                text.siblings.removeLast();
            }
        }
        refresh();
    }

    public static void handleNewMessage(Text text) {
        MutableText initText = initText(text);
        if (Options.autoChat.getValue()) {  // 如果是自动翻译则处理
            translate(initText);
        } else if (Options.chatTip.getValue()) {
            if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
                addTip(initText);  // 如果在聊天框内，则添加翻译按钮
            }
        }
    }

    private static final Text TRANSLATING_TIP = Text.literal("[翻译中]")
            .setStyle(Style.EMPTY
                    .withColor(TextColor.fromRgb(2259711))
                    .withClickEvent(new TranslateClickEvent(null)));

    public static void translate(MutableText text) {
        String s = StringUtil.strip(text.getString());
        translatingTexts.add(text);
        CompletableFuture.supplyAsync(() -> {
            try {
                String result = Translate.cachedTranslate(s, Source.CHAT);
                return createResultText(result, text);
            } catch (Exception e) {
                return createErrorText(e.getMessage(), text, s);
            }
        }).thenAccept(result -> {
            translatingTexts.remove(text);
            TranslateClickEvent event = getTranslateClickEvent(text);
            if (event != null) {
                text.siblings.set(text.siblings.size() - 1, result);
            } else {
                text.append(" ").append(result);
            }
            MinecraftClient.getInstance().execute(ChatHandler::refresh);
        });
    }

    public static void translateWithTip(MutableText text) {
        if (translatingTexts.contains(text)) {
            return;
        }
        text.siblings.removeLast();
        translate(text);  // 获得没有提示按钮时的文本
        text.siblings.add(TRANSLATING_TIP);
        refresh();
    }

    private static final HoverEvent TRANSLATE_HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击重新翻译"));

    private static Text createErrorText(String err, MutableText originalText, String original) {
        return Text.literal("[" + err + "]").setStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(13378339))
                .withHoverEvent(TRANSLATE_HOVER_EVENT)
                .withClickEvent(new TranslateClickEvent(originalText, true))
                .withInsertion(original));
    }

    private static Text createResultText(String result, MutableText originalText) {
        return Text.literal(result).setStyle(Style.EMPTY
                .withColor(TextColor.fromRgb(3145516))
                .withHoverEvent(TRANSLATE_HOVER_EVENT)
                .withClickEvent(new TranslateClickEvent(originalText, true))
                .withInsertion(result));
    }

    private static MutableText initText(Text text) {
        MutableText t = (MutableText) text;
        if (!(t.siblings instanceof ArrayList<Text>)) {
            t.siblings = new ArrayList<>(t.siblings);
        }
        return t;
    }

    @Nullable
    private static TranslateClickEvent getTranslateClickEvent(MutableText text) {
        if (text.siblings.size() >= 2 && (text.siblings.getLast().getStyle().getClickEvent() instanceof TranslateClickEvent event)) {
            return event;
        }
        return null;
    }

    public static class TranslateClickEvent extends ClickEvent {
        public MutableText text;
        public boolean clicked;
        public TranslateClickEvent(MutableText text, boolean clicked) {
            super(null, "");
            this.text = text;
            this.clicked = clicked;
        }
        public TranslateClickEvent(MutableText text) {
            this(text, false);
        }
    }

}
