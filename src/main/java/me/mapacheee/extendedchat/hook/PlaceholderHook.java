package me.mapacheee.extendedchat.hook;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

@Service
public final class PlaceholderHook {

    private final Logger logger;
    private final boolean enabled;
    private final Plugin papiPlugin;
    private final ColorService colorService;
    private ExtendedChatExpansion expansion;

    @Inject
    public PlaceholderHook(Logger logger, ColorService colorService) {
        this.logger = logger;
        this.colorService = colorService;
        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        this.papiPlugin = plugin != null ? plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") : null;
        this.enabled = papiPlugin != null && papiPlugin.isEnabled();

        if (enabled) {
            logger.info("PlaceholderAPI detected and enabled");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @OnEnable
    public void onEnable() {
        if (!enabled) {
            return;
        }
        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        if (plugin == null) {
            return;
        }
        expansion = new ExtendedChatExpansion(plugin, colorService);
        expansion.register();
    }

    @OnDisable
    public void onDisable() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    public String setPlaceholders(Player player, String text) {
        if (!enabled || player == null || text == null) {
            return text;
        }
        try {
            String resolved = PlaceholderAPI.setPlaceholders(player, text);
            resolved = ColorData.normalizeLegacyHex(resolved);
            resolved = ColorData.normalizeLegacyCodes(resolved);
            resolved = ColorData.normalizeSectionHex(resolved);
            return ColorData.normalizeSectionCodes(resolved);
        } catch (Exception e) {
            logger.warn("Failed to set placeholders for {}", player.getName(), e);
            return text;
        }
    }
}