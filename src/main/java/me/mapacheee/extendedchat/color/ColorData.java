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