package kgg.translator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kgg.translator.exception.NoTranslatorException;
import kgg.translator.exception.NotConfiguredException;
import kgg.translator.exception.TranslateException;
import kgg.translator.ocrtrans.ResRegion;
import kgg.translator.option.Options;
import kgg.translator.translator.Source;
import kgg.translator.translator.Translator;
import kgg.translator.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import static kgg.translator.TranslatorManager.*;

import java.util.concurrent.ExecutionException;

public class Translate {
    private static final Logger LOGGER = LogManager.getLogger(Translate.class);
    private record Text(String text, String source) {}

    private static final LoadingCache<Text, String> cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<>() {
        @Override
        public @NotNull String load(@NotNull Text key) throws Exception {
            return translate(key.text, key.source);
        }
    });

    private static Text createText(String text, String source) {
        if (!Options.markSources.getValue()) {
            source = Source.UNKNOWN;
        }
        return new Text(text, source);
    }

    public static String cachedTranslate(String text, String source) throws TranslateException {
        try {
            return cache.get(new Text(text, source));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TranslateException c) {
                throw c;
            } else {
                throw new TranslateException(e.getCause());
            }
        }
    }

    public static String getFromCache(String text, String source) {
        return cache.getIfPresent(createText(text, source));
    }

    public static String translate(String text, String source) throws TranslateException {
        return translate(text, getCurrent(), getFrom(), getTo(), source);
    }

    public static String translate(String text, Translator translator, String from, String to, String source) throws TranslateException {
        if (StringUtil.isBlank(text)) return text;
        if (StringUtils.isNumeric(text)) return text;
        checkTranslator(translator);

        try {
            String translate;
//            if (source == null) {
//                translate = translator.translate(text, from, to);
//            } else {
                translate = translator.translate(text, from, to, source);
//            }
            LOGGER.info("{} translate from {} to {}: ({})\"{}\" -> \"{}\"", translator, from, to, source, StringUtil.getOutString(text), StringUtil.getOutString(translate));
            return translate;
        } catch (Exception e) {
            LOGGER.error("{} translate from {} to {} failed: \"{}\"", translator, from, to, StringUtil.getOutString(text), e);
            if (e instanceof TranslateException c) {
                throw c;
            } else {
                throw new TranslateException(e);
            }
        }
    }

    public static ResRegion[] ocrtrans(byte[] img) throws TranslateException {
        return ocrtrans(getCurrent(), img, getFrom(), getTo());
    }

    public static ResRegion[] ocrtrans(Translator translator, byte[] img, String from, String to) throws TranslateException {
        checkTranslator(translator);
        LOGGER.info("{} ocrtrans, from {} to {}", translator, from, to);
        try {
            return translator.ocrtrans(img, from, to);
        } catch (Exception e) {
            LOGGER.error("{} ocrtrans, from {} to {} failed:", translator, from, to, e);
            if (e instanceof TranslateException c) {
                throw c;
            } else {
                throw new TranslateException(e);
            }
        }
    }

    private static void checkTranslator(Translator translator) throws TranslateException {
        if (translator == null) {
            throw new NoTranslatorException();
        }
        if (!translator.isConfigured()) {
            throw new NotConfiguredException(translator);
        }
    }

    public static void clearCache() {
        LOGGER.info("Clear cache");
        cache.invalidateAll();
    }
}
