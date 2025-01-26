package kgg.translator.util;

public class StringUtil {
    public static boolean isBlank(String text) {
        return text.replaceAll("§.", "").isBlank();
    }

    public static String strip(String text) {
        return text.replaceAll("§.", "").strip();
    }

    public static boolean equals(String text1, String text2) {
        return strip(text1).replace(" ", "").replace("\n", "").equals(strip(text2).replace(" ", "").replace("\n", ""));
    }

    public static String getOutString(String text) {
        if (text.length() > 20) {
            return text.substring(0, 20) + "...";
        }
        return text;
    }
}
