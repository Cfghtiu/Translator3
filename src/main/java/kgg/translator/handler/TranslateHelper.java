package kgg.translator.handler;

import kgg.translator.Translate;
import kgg.translator.util.TextUtil;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class TranslateHelper {
    private static final Logger LOGGER = LogManager.getLogger(TranslateHelper.class);
    private static final ConcurrentHashMap<String, Long> failedTextCache = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<String> translatingTexts = new ConcurrentLinkedQueue<>();
    private static final int MAX_FAILED_TEXT_CACHE_TIME = 1000 * 60 * 2;  // 2分钟
    private static final long CHECK_TIME = 1000 * 60 * 5;  // 5分钟
    // 清理线程
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    static {
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            failedTextCache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > MAX_FAILED_TEXT_CACHE_TIME);
        }, CHECK_TIME, CHECK_TIME, TimeUnit.MILLISECONDS);
    }

    public static Text translateNoWait(Text text, String source) {
        return translateNoWait(text, s -> {}, source);
    }

    public static Text translateNoWait(Text text, Consumer<String> comparable, String source) {
        return TextUtil.toText(translateNoWait(TextUtil.getString(text), source, comparable), text);
    }

    public static String translateNoWait(String text, String source, Consumer<String> comparable) {
        // 检查此文本
        Long aLong = failedTextCache.get(text);
        if (aLong != null) {
            if (System.currentTimeMillis() - aLong > MAX_FAILED_TEXT_CACHE_TIME) {
                failedTextCache.remove(text);
            } else {
                return text;
            }
        }

        String cache = Translate.getFromCache(text, source);
        if (cache != null) {
            return cache;
        }
        if (translatingTexts.contains(text)) {
            return text;
        }

        translatingTexts.add(text);
        CompletableFuture.runAsync(() -> {
            try {
                String s = Translate.cachedTranslate(text, source);
                comparable.accept(s);
            } catch (Exception e) {
                failedTextCache.put(text, System.currentTimeMillis());
            } finally {
                translatingTexts.remove(text);
            }
        });
        return text;
    }

    public static void clearCache() {
        LOGGER.info("Clear failed cache");
        failedTextCache.clear();
    }
}
