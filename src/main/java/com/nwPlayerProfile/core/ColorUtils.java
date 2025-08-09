package com.nwPlayerProfile.core;

import com.nwPlayerProfile.NwPlayerProfile; // ถ้าจำเป็น
import me.clip.placeholderapi.PlaceholderAPI; // ต้องมี
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor; // ต้องมี
import org.bukkit.Bukkit; // ต้องมี
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection(); // ใช้ LegacyComponentSerializer
    private static final Pattern HEX_PATTERN = Pattern.compile("(&#)([0-9a-fA-F]{6})");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("[&§][0-9a-fk-or]"); // รวม & และ §

    // คง sendMessage ไว้ตามที่คุณต้องการ
    public static void sendMessage(Player player, String rawMessage) {
        // ใช้ translateColorCodes เพื่อประมวลผล MiniMessage และแปลงกลับเป็น Legacy String ก่อนส่ง
        String finalMessage = translateColorCodes(rawMessage, player);
        player.sendMessage(finalMessage);
    }

    /**
     * แปลงข้อความที่มีสีและรูปแบบต่างๆ (Minecraft Color Codes, Hex Colors, MiniMessage)
     * รวมถึงรองรับ PlaceholderAPI placeholders หากมี player object ถูกส่งมา
     * เป็นข้อความที่ Minecraft สามารถแสดงผลได้ในรูปแบบ Legacy String
     *
     * @param text ข้อความต้นฉบับ
     * @param player (Optional) The player for whom to parse placeholders. Can be null if no player-specific placeholders are needed.
     * @return ข้อความที่แปลงแล้วในรูปแบบ String ที่ Minecraft แสดงผลได้ (Legacy String)
     */
    public static String translateColorCodes(String text, Player player) {
        if (text == null) return "";

        String processedText = text;

        // 1. ประมวลผล PlaceholderAPI
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            processedText = PlaceholderAPI.setPlaceholders(player, processedText); // ใช้ PlaceholderAPI โดยตรง
        }

        // 2. แปลง Hex Color Codes (§x§RRGGBB) เป็น MiniMessage format
        processedText = translateLegacyHexColors(processedText);

        // 3. แปลง Hex Color Codes (เช่น &#FF0000) เป็น MiniMessage format
        processedText = translateHexColors(processedText);

        // 4. แปลง Legacy Color Codes (เช่น &c, §l) เป็น MiniMessage format
        processedText = translateLegacyToMiniMessage(processedText);

        // 5. แปลง MiniMessage syntax เป็น Component
        Component component;
        try {
            component = MINI_MESSAGE.deserialize(processedText);
        } catch (Exception e) {
            // หาก MiniMessage ล้มเหลว ให้ fallback ไปใช้ ChatColor.translateAlternateColorCodes
            // เพื่อให้ข้อความยังคงมีสีอยู่บ้าง แต่จะเสียรูปแบบ MiniMessage ที่ซับซ้อนไป
            // NwPlayerProfile.getPlugin(NwPlayerProfile.class).getLogger().warning("Error deserializing MiniMessage: " + e.getMessage() + " for text: " + processedText); // Debugging
            return ChatColor.translateAlternateColorCodes('&', text); // ใช้ text เดิม เพราะ processedText อาจมี MiniMessage tags แล้ว
        }

        // 6. แปลง Component กลับเป็น Legacy Text เพื่อให้ Minecraft แสดงผลได้
        // นี่คือจุดสำคัญที่ทำให้โค้ดทำงานได้บน Purpur 1.21.7 (ซึ่งต้องการ Legacy String)
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Overload สำหรับเรียกใช้โดยไม่มี Player (สำหรับข้อความที่ไม่ต้องการ Placeholder)
     *
     * @param text ข้อความต้นฉบับ
     * @return ข้อความที่แปลงแล้ว
     */
    public static String translateColorCodes(String text) {
        return translateColorCodes(text, null);
    }

    /**
     * แปลงรายการข้อความที่มีสีและรูปแบบต่างๆ รวมถึงรองรับ PlaceholderAPI placeholders
     *
     * @param lines รายการข้อความต้นฉบับ
     * @param player (Optional) The player for whom to parse placeholders. Can be null if no player-specific placeholders are needed.
     * @return รายการข้อความที่แปลงแล้ว (List of Legacy Strings)
     */
    public static List<String> translateColorCodes(List<String> lines, Player player) {
        List<String> coloredLines = new ArrayList<>();
        for (String line : lines) {
            coloredLines.add(translateColorCodes(line, player));
        }
        return coloredLines;
    }

    /**
     * Overload สำหรับเรียกใช้รายการข้อความโดยไม่มี Player
     *
     * @param lines รายการข้อความต้นฉบับ
     * @return รายการข้อความที่แปลงแล้ว (List of Legacy Strings)
     */
    public static List<String> translateColorCodes(List<String> lines) {
        return translateColorCodes(lines, null);
    }

    // เมธอดส่วนตัวสำหรับแปลง Hex Codes
    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(2);
            String miniMessageColor = "<color:#" + hex + ">";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(miniMessageColor));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String translateLegacyHexColors(String text) {
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String matchedHex = matcher.group();
            StringBuilder hexCode = new StringBuilder();
            for (int i = 2; i < matchedHex.length(); i += 2) {
                hexCode.append(matchedHex.charAt(i + 1));
            }
            String miniMessageColor = "<color:#" + hexCode.toString() + ">";
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(miniMessageColor));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * แปลง Legacy Color Codes (เช่น &c, &l) และ (§c, §l) เป็น MiniMessage format
     *
     * @param text ข้อความที่มี Legacy Color Codes
     * @return ข้อความที่แปลงเป็น MiniMessage format
     */
    private static String translateLegacyToMiniMessage(String text) {
        // ใช้ Map เพื่อจัดการการแทนที่
        // การแทนที่ที่ยาวกว่า (เช่น &r) ควรมาก่อน
        String[][] mappings = {
                {"&r", "<reset>"}, {"§r", "<reset>"},
                {"&k", "<obfuscated>"}, {"§k", "<obfuscated>"},
                {"&l", "<bold>"}, {"§l", "<bold>"},
                {"&m", "<strikethrough>"}, {"§m", "<strikethrough>"},
                {"&n", "<underlined>"}, {"§n", "<underlined>"},
                {"&o", "<italic>"}, {"§o", "<italic>"},
                {"&0", "<black>"}, {"§0", "<black>"},
                {"&1", "<dark_blue>"}, {"§1", "<dark_blue>"},
                {"&2", "<dark_green>"}, {"§2", "<dark_green>"},
                {"&3", "<dark_aqua>"}, {"§3", "<dark_aqua>"},
                {"&4", "<dark_red>"}, {"§4", "<dark_red>"},
                {"&5", "<dark_purple>"}, {"§5", "<dark_purple>"},
                {"&6", "<gold>"}, {"§6", "<gold>"},
                {"&7", "<gray>"}, {"§7", "<gray>"},
                {"&8", "<dark_gray>"}, {"§8", "<dark_gray>"},
                {"&9", "<blue>"}, {"§9", "<blue>"},
                {"&a", "<green>"}, {"§a", "<green>"},
                {"&b", "<aqua>"}, {"§b", "<aqua>"},
                {"&c", "<red>"}, {"§c", "<red>"},
                {"&d", "<light_purple>"}, {"§d", "<light_purple>"},
                {"&e", "<yellow>"}, {"§e", "<yellow>"},
                {"&f", "<white>"}, {"§f", "<white>"}
        };

        String result = text;
        for (String[] map : mappings) {
            result = result.replace(map[0], map[1]);
        }
        return result;
    }

    /**
     * Utility สำหรับกรอง legacy code ออกจากข้อความ (ไม่รวม MiniMessage tags)
     */
    public static String stripLegacyCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)§[0-9A-FK-ORX]", "").replaceAll("(?i)&[0-9A-FK-ORX]", "");
    }

    /**
     * ล้างโค้ดสีทั้งหมดจากข้อความ ทั้ง Legacy, Hex, MiniMessage
     *
     * @param input ข้อความต้นฉบับที่มีโค้ดสี
     * @return ข้อความที่ล้างโค้ดสีแล้ว
     */
    public static String stripColorCodes(String input) {
        if (input == null) return "";

        // ล้าง Legacy Color Codes
        String result = input.replaceAll("(?i)&[0-9A-FK-OR]", "")
                .replaceAll("(?i)§[0-9A-FK-OR]", "")
                .replaceAll("(?i)§x(§[0-9A-F]){6}", "");

        // ล้าง Hex Color Codes
        result = result.replaceAll("(?i)&?#[0-9A-F]{6}", "");

        // ล้าง MiniMessage tags
        result = result.replaceAll("(?i)<[^>]+>", "");

        return result;
    }

    // เมธอดสำหรับ Deserialize Component โดยตรง (ถ้าจำเป็นต้องใช้)
    // แต่ควรใช้ translateColorCodes ที่คืนค่าเป็น String เพื่อเลี่ยงปัญหา
    public static Component deserializeLoreToComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static List<Component> deserializeLoreToComponents(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(ColorUtils::deserializeLoreToComponent)
                .collect(Collectors.toList());
    }

    public static Component deserializeTextToComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

}