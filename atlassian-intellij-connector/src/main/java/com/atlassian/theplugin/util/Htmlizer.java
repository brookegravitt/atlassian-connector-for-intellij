package com.atlassian.theplugin.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * User: kalamon
 * Date: Apr 24, 2009
 * Time: 3:23:30 PM
 */
public class Htmlizer {

    public static final String HYPERLINK_PATTERN = "(https?://[\\w|:|-|/|\\.]+)";

    public static final String A_PATTERN = "(<a\\s+.*</a>)";
    private boolean haveA;
    private boolean haveHyperlink;
    private String linkTextReplacement;
    private int truncateLinkTextAt = -1;

    public Htmlizer() {
    }

    public Htmlizer(int truncateLinkTextAt) {
        this.truncateLinkTextAt = truncateLinkTextAt;
    }

    public Htmlizer(String linkTextReplacement) {
        this.linkTextReplacement = linkTextReplacement;
    }

    public String htmlizeHyperlinks(String text) {
        Pattern patternHyperlink = Pattern.compile(HYPERLINK_PATTERN);
        Matcher matcherHyperlink = patternHyperlink.matcher(text);
        Pattern patternA = Pattern.compile(A_PATTERN);
        Matcher matcherA = patternA.matcher(text);
        StringBuilder result = new StringBuilder();
        int index = 0;
        haveA = false;
        haveHyperlink = false;
        while (matchBoth(matcherHyperlink, matcherA, index)) {
            boolean copyA;
            if (haveA && haveHyperlink) {
                copyA = matcherA.start() < matcherHyperlink.start();
            } else {
                copyA = haveA;
            }
            if (copyA) {
                index = copyAHref(text, matcherA, result, index);
            } else {
                index = replaceHyperlinkWithAHref(text, matcherHyperlink, result, index);
            }
            haveA = false;
            haveHyperlink = false;
        }
        String rest = text.substring(index, text.length());
        result.append(rest);
        return result.toString();
    }

    public String replaceBrackets(String text) {
        String result = text.replaceAll("<", "&lt;");
        result = result.replaceAll(">", "&gt;");
        return result;
    }
    
    public String replaceWhitespace(String text) {
        String result = text.replaceAll("    ", "&nbsp;&nbsp;&nbsp;&nbsp;");
        result = result.replaceAll("   ", "&nbsp;&nbsp;&nbsp;");
        result = result.replaceAll("  ", "&nbsp;&nbsp;");
        result = result.replaceAll("\n ", "<br>&nbsp;");
        result = result.replaceAll("\n\t", "<br>&nbsp;&nbsp;&nbsp;&nbsp;");
        result = result.replaceAll("\r", "").replaceAll("\n", "<br>");
        return result;
    }

    private boolean matchBoth(Matcher matcherHyperlink, Matcher matcherA, int index) {
        haveA = matcherA.find(index);
        haveHyperlink = matcherHyperlink.find(index);
        return haveA || haveHyperlink;
    }

    private int copyAHref(String text, Matcher matcher, StringBuilder result, int index) {
        result.append(text.substring(index, matcher.start()));
        String a = matcher.group(1);
        result.append(a);
        return matcher.end();
    }

    private int replaceHyperlinkWithAHref(String text, Matcher matcher, StringBuilder result, int index) {
        result.append(text.substring(index, matcher.start()));
        String link = matcher.group(1);
        String linkText = linkTextReplacement != null ? linkTextReplacement : truncateLinkText(link);
        String replacement =
                "<a href=\"" + link + "\">"
                + linkText
                + "</a>";
        result.append(replacement);
        return matcher.end();
    }

    private String truncateLinkText(String text) {
        if (truncateLinkTextAt > 0 && text.length() > truncateLinkTextAt) {
            return text.substring(0, truncateLinkTextAt) + "...";
        }
        return text;
    }
}
