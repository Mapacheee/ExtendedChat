package me.mapacheee.extendedchat.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.Map;

public final class ExtendedChatExpansion extends PlaceholderExpansion {

    private static final Map<String, String> LEGACY_COLORS = Map.ofEntries(
            Map.entry("black", "&0"),
            Map.entry("dark_blue", "&1"),
            Map.entry("dark_green", "&2"),
            Map.entry("dark_aqua", "&3"),
            Map.entry("dark_red", "&4"),
            Map.entry("dark_purple", "&5"),
            Map.entry("gold", "&6"),
            Map.entry("gray", "&7"),
            Map.entry("dark_gray", "&8"),
            Map.entry("blue", "&9"),
            Map.entry("green", "&a"),
            Map.entry("aqua", "&b"),
            Map.entry("red", "&c"),
            Map.entry("light_purple", "&d"),
            Map.entry("yellow", "&e"),
            Map.entry("white", "&f"),
            Map.entry("reset", "&r")
    );

    private final ExtendedChatPlugin plugin;
    private final ColorService colorService;

    public ExtendedChatExpansion(ExtendedChatPlugin plugin, ColorService colorService) {
        this.plugin = plugin;
        this.colorService = colorService;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "extendedchat";
    }

    @Override
    public @NonNull String getAuthor() {
        return "mapacheee";
    }

    @Override
    public @NonNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NonNull String params) {
        if (player == null) {
            return "";
        }
        if (player.getPlayer() == null) {
            return "";
        }

        ColorData data = colorService.getColorData(player.getPlayer());
        String key = params.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "name_color" -> safeValue(data.getNameColor());
            case "message_color" -> safeValue(data.getMessageColor());
            case "name_color_legacy" -> toLegacyColor(safeValue(data.getNameColor()));
            case "message_color_legacy" -> toLegacyColor(safeValue(data.getMessageColor()));
            default -> null;
        };
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String toLegacyColor(String tag) {
        if (tag == null || tag.isBlank()) {
            return "";
        }

        String clean = tag.trim();
        if (clean.startsWith("<") && clean.endsWith(">") && clean.length() > 2) {
            clean = clean.substring(1, clean.length() - 1);
        }

        if (clean.startsWith("gradient:")) {
            String[] parts = clean.substring("gradient:".length()).split(":");
            if (parts.length > 0 && parts[0].startsWith("#") && parts[0].length() == 7) {
                return "&#" + parts[0].substring(1).toLowerCase(Locale.ROOT);
            }
            return "";
        }

        if (clean.startsWith("#") && clean.length() == 7) {
            return "&#" + clean.substring(1).toLowerCase(Locale.ROOT);
        }

        String legacy = LEGACY_COLORS.get(clean);
        return legacy == null ? "" : legacy;
    }
}
