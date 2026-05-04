package me.mapacheee.extendedchat.color;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorData {

    private String nameColor;
    private String messageColor;
    private List<String> gradientColors;

    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern LEGACY_CODE_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");
    private static final Pattern SECTION_HEX_PATTERN = Pattern.compile("(?i)§x(§[0-9a-f]){6}");
    private static final Pattern SECTION_CODE_PATTERN = Pattern.compile("(?i)§([0-9a-fk-or])");

    public ColorData() {
        this.nameColor = "<white>";
        this.messageColor = "<white>";
        this.gradientColors = null;
    }

    public ColorData(String nameColor, String messageColor) {
        this.nameColor = nameColor;
        this.messageColor = messageColor;
        this.gradientColors = null;
    }

    public ColorData(String nameColor, String messageColor, List<String> gradientColors) {
        this.nameColor = nameColor;
        this.messageColor = messageColor;
        this.gradientColors = gradientColors;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setNameColor(String nameColor) {
        this.nameColor = nameColor;
    }

    public String getMessageColor() {
        return messageColor;
    }

    public void setMessageColor(String messageColor) {
        this.messageColor = messageColor;
    }

    public List<String> getGradientColors() {
        return gradientColors;
    }

    public void setGradientColors(List<String> gradientColors) {
        this.gradientColors = gradientColors;
    }

    public boolean hasGradient() {
        return gradientColors != null && gradientColors.size() >= 2;
    }

    public String applyNameColor(String name) {
        return applyColor(name, nameColor);
    }

    public String applyMessageColor(String message) {
        if (hasGradient()) {
            return applyGradient(message);
        }
        return applyColor(message, messageColor);
    }

    public static String applyColor(String text, String colorTag) {
        return colorTag + text + "<reset>";
    }

    public static String translateLegacy(String text) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(
                LegacyComponentSerializer.legacySection().deserialize(text));
    }

    public static String normalizeLegacyHex(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = LEGACY_HEX_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int lastIndex = 0;
        matcher.reset();
        while (matcher.find()) {
            builder.append(text, lastIndex, matcher.start());
            builder.append("<#").append(matcher.group(1).toLowerCase()).append(">" );
            lastIndex = matcher.end();
        }
        builder.append(text, lastIndex, text.length());
        return builder.toString();
    }

    public static String normalizeLegacyCodes(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = LEGACY_CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int lastIndex = 0;
        matcher.reset();
        while (matcher.find()) {
            builder.append(text, lastIndex, matcher.start());
            builder.append(legacyToMiniMessage(matcher.group(1)));
            lastIndex = matcher.end();
        }
        builder.append(text, lastIndex, text.length());
        return builder.toString();
    }

    public static String normalizeSectionHex(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = SECTION_HEX_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int lastIndex = 0;
        matcher.reset();
        while (matcher.find()) {
            builder.append(text, lastIndex, matcher.start());
            String hex = matcher.group().replaceAll("(?i)§x|§", "");
            if (hex.length() == 6) {
                builder.append("<#").append(hex.toLowerCase()).append(">" );
            }
            lastIndex = matcher.end();
        }
        builder.append(text, lastIndex, text.length());
        return builder.toString();
    }

    public static String normalizeSectionCodes(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        Matcher matcher = SECTION_CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        int lastIndex = 0;
        matcher.reset();
        while (matcher.find()) {
            builder.append(text, lastIndex, matcher.start());
            builder.append(legacyToMiniMessage(matcher.group(1)));
            lastIndex = matcher.end();
        }
        builder.append(text, lastIndex, text.length());
        return builder.toString();
    }

    private static String legacyToMiniMessage(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return switch (code.toLowerCase()) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obfuscated>";
            case "l" -> "<bold>";
            case "m" -> "<strikethrough>";
            case "n" -> "<underlined>";
            case "o" -> "<italic>";
            case "r" -> "<reset>";
            default -> "";
        };
    }

    private String applyGradient(String message) {
        if (gradientColors == null || gradientColors.size() < 2) {
            return applyColor(message, messageColor);
        }

        StringBuilder result = new StringBuilder();
        String[] chars = message.split("");
        int colorCount = gradientColors.size();

        for (int i = 0; i < chars.length; i++) {
            int colorIndex = (i * (colorCount - 1)) / Math.max(1, message.length() - 1);
            colorIndex = Math.min(colorIndex, colorCount - 1);
            result.append(gradientColors.get(colorIndex)).append(chars[i]);
        }

        return result.toString() + "<reset>";
    }

    public static boolean isValidHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9A-Fa-f]{6}$");
        return pattern.matcher(hex).matches();
    }

    public static String formatHexColor(String hex) {
        if (!hex.startsWith("#")) {
            return "<#" + hex.toLowerCase() + ">";
        }
        String clean = hex.substring(1);
        if (isValidHexColor(clean)) {
            return "<#" + clean.toLowerCase() + ">";
        }
        return null;
    }
}
