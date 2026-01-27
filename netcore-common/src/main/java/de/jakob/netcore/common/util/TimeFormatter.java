package de.jakob.netcore.common.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFormatter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static String formatPattern = "[%d% Day(s) ][%h% Hour(s) ][%m% Minute(s) ][%s% Second(s)]";
    private static final Pattern BLOCK_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Pattern PLURAL_PATTERN = Pattern.compile("\\((.*?)\\)");

    public static void setFormatPattern(String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            formatPattern = pattern;
        }
    }

    public static String formatPlaytime(long millis) {
        return formatPlaytime(millis, formatPattern);
    }

    public static String formatPlaytime(long millis, String pattern) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if (pattern.contains("%d%")) {
            days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);
        }

        if (pattern.contains("%h%")) {
            hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);
        }

        if (pattern.contains("%m%")) {
            minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
        }

        if (pattern.contains("%s%")) {
            seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        }

        List<String> blocks = new ArrayList<>();
        Matcher matcher = BLOCK_PATTERN.matcher(pattern);
        while (matcher.find()) {
            blocks.add(matcher.group(1));
        }

        StringBuilder sb = new StringBuilder();
        boolean anyShown = false;

        for (String block : blocks) {
            String processedBlock = processBlock(block, days, hours, minutes, seconds, false);
            if (processedBlock != null) {
                sb.append(processedBlock);
                anyShown = true;
            }
        }
        
        if (!anyShown) {

            String targetToken = null;
            if (pattern.contains("%s%")) targetToken = "%s%";
            else if (pattern.contains("%m%")) targetToken = "%m%";
            else if (pattern.contains("%h%")) targetToken = "%h%";
            else if (pattern.contains("%d%")) targetToken = "%d%";

            if (targetToken != null) {
                for (String block : blocks) {
                    if (block.contains(targetToken)) {
                        String processedBlock = processBlock(block, days, hours, minutes, seconds, true);
                        if (processedBlock != null) {
                            sb.append(processedBlock);
                        }
                        break;
                    }
                }
            }
        }

        if (sb.length() == 0) {
             return "0 Seconds"; 
        }

        return sb.toString().trim();
    }

    private static String processBlock(String block, long d, long h, long m, long s, boolean force) {
        boolean show = force;
        long value = 0;

        if (block.contains("%d%")) {
            if (d > 0) { show = true; }
            value = d;
            block = block.replace("%d%", String.valueOf(d));
        } else if (block.contains("%h%")) {
            if (h > 0) { show = true; }
            value = h;
            block = block.replace("%h%", String.valueOf(h));
        } else if (block.contains("%m%")) {
            if (m > 0) { show = true; }
            value = m;
            block = block.replace("%m%", String.valueOf(m));
        } else if (block.contains("%s%")) {
            if (s > 0) { show = true; }
            value = s;
            block = block.replace("%s%", String.valueOf(s));
        }

        if (show) {
            Matcher matcher = PLURAL_PATTERN.matcher(block);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                if (value == 1) {
                    matcher.appendReplacement(sb, "");
                } else {
                    matcher.appendReplacement(sb, matcher.group(1));
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        return null;
    }

    public static String format(Date date) {
        return dateFormat.format(date);
    }
}