package ro.petitii.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class CleanUtil {
    public static String cleanHtml(String content) {
        return Jsoup.clean(preserveNewLines(content), Whitelist.relaxed());
    }

    public static String preserveNewLines(String content) {
        return content.replaceAll("\n", "<br />");
    }
}
