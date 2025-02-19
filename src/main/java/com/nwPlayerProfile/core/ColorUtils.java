package com.nwPlayerProfile.core;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ColorUtils {

    public static final String WITH_DELIMITER = "((?<=%1$s)|(?<=%1$s))";

    public static String translateColorCodes(String text) {
        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));
        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                i++;
                if (texts[i].charAt(0) == '#') {
                    finalText.append(ChatColor.of(texts[i].substring(0, 7)) + texts[i].substring(7));
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }

        return finalText.toString();
    }

    public static List<String> translateColorCodes(List<String> lines) {
        List<String> coloredLines = new ArrayList<>();
        for (String line : lines) {
            coloredLines.add(translateColorCodes(line));
        }
        return coloredLines;
    }
}
