package ro.petitii.util;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class StringUtil {
    public static String cleanHtml(String content) {
        return Jsoup.clean(preserveNewLines(content), Whitelist.relaxed());
    }

    public static String toPlainText(String content) {
        return Jsoup.clean(content, Whitelist.none());
    }

    public static String preserveNewLines(String content) {
        return content.replaceAll("\n", "<br />");
    }

    public static String prepareForView(String string, int length) {
        return WordUtils.wrap(string, length, "<br />", true);
    }

    public static String toRelativeURL(String url, String defaultValue) {
        try {
            String path = url.split("//")[1].substring(url.split("//")[1].indexOf("/") + 1);
            if (path.isEmpty()) {
                return defaultValue;
            } else {
                return path;
            }
        } catch (NullPointerException e) {
            // no biggie, the visit was by typing the url directly
        }
        return defaultValue;
    }
}
