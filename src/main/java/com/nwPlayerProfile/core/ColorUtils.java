package com.nwPlayerProfile.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static void sendMessage(Player player, String rawMessage) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component component = miniMessage.deserialize(rawMessage);
        String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
        player.sendMessage(legacyText);
    }

    /**
     * แปลง legacy minecraft color codes (& หรือ §) เป็น MiniMessage tags
     * เช่น &c => <red>
     * ฟังก์ชันนี้แทนที่ด้วย tag เปิดเท่านั้น ไม่มีการปิด tag อัตโนมัติ
     * @param input ข้อความต้นฉบับที่อาจมี & หรือ § color codes
     * @return ข้อความที่แปลงเป็น MiniMessage format
     */
    public static String legacyToMiniMessage(String input) {
        if (input == null || input.isEmpty()) return "";

        // แปลง & เป็น § ให้เหมือนกันทั้งหมด
        String normalized = input.replace('&', '§');

        // Mapping legacy codes เป็น MiniMessage tags
        String[][] mappings = {
                {"§0", "<black>"},      {"§1", "<dark_blue>"},  {"§2", "<dark_green>"},
                {"§3", "<dark_aqua>"},  {"§4", "<dark_red>"},   {"§5", "<dark_purple>"},
                {"§6", "<gold>"},       {"§7", "<gray>"},       {"§8", "<dark_gray>"},
                {"§9", "<blue>"},       {"§a", "<green>"},      {"§b", "<aqua>"},
                {"§c", "<red>"},        {"§d", "<light_purple>"},{"§e", "<yellow>"},
                {"§f", "<white>"},
                {"§l", "<bold>"},       {"§o", "<italic>"},     {"§n", "<underlined>"},
                {"§m", "<strikethrough>"},{"§k", "<obfuscated>"},
                {"§r", "</reset>"}
        };

        for (String[] map : mappings) {
            normalized = normalized.replace(map[0], map[1]);
        }

        return normalized;
    }

    /**
     * ปลอดภัยในการ deserialize ข้อความทั้ง legacy และ MiniMessage
     * หากข้อความมี legacy code จะใช้ LegacyComponentSerializer
     * หากข้อความไม่มี legacy จะใช้ MiniMessage
     */
    public static Component parseWithAutoDetect(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // ตรวจสอบ legacy codes ทั้ง & และ §
        if (text.contains("§") || text.contains("&")) {
            // แปลง legacy เป็น MiniMessage tags ก่อน
            String mmText = legacyToMiniMessage(text);
            // จากนั้น deserialize ด้วย MiniMessage
            return MINI_MESSAGE.deserialize(mmText);
        }

        // ข้อความไม่มี legacy codes ใช้ MiniMessage ธรรมดา
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * แปลง List<String> ที่อาจมี legacy หรือ MiniMessage เป็น List<Component>
     */
    public static List<Component> parseWithAutoDetect(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(ColorUtils::parseWithAutoDetect)
                .collect(Collectors.toList());
    }

    /**
     * เมธอดเดิมสำหรับ return String กลับมาโดยไม่เปลี่ยนอะไร (MiniMessage format เท่านั้น)
     */
    public static String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text;
    }

    public static List<String> translateColorCodes(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(ColorUtils::translateColorCodes)
                .collect(Collectors.toList());
    }

    /**
     * Utility สำหรับกรอง legacy code ออกจากข้อความ
     */
    public static String stripLegacyCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)§[0-9A-FK-ORX]", "").replaceAll("(?i)&[0-9A-FK-ORX]", "");
    }
}
