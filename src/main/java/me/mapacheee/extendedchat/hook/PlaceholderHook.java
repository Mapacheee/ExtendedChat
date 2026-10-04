package me.mapacheee.extendedchat.hook;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

@Service
public final class PlaceholderHook {

    private final Logger logger;
    private volatile boolean enabled;
    private final ColorService colorService;
    private volatile Integration integration;

    @Inject
    public PlaceholderHook(Logger logger, ColorService colorService) {
        this.logger = logger;
        this.colorService = colorService;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @OnEnable
    public void onEnable(Plugin plugin) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return;
        }
        logger.info("PlaceholderAPI detected and enabled");
        integration = new PlaceholderApiIntegration((ExtendedChatPlugin) plugin, colorService);
        enabled = true;
    }

    @OnDisable
    public void onDisable() {
        enabled = false;
        if (integration != null) {
            integration.unregister();
            integration = null;
        }
    }

    public String setPlaceholders(Player player, String text) {
        Integration current = integration;
        if (!enabled || current == null || player == null || text == null) {
            return text;
        }
        try {
            String resolved = current.resolve(player, text);
            resolved = ColorData.normalizeLegacyHex(resolved);
            resolved = ColorData.normalizeLegacyCodes(resolved);
            resolved = ColorData.normalizeSectionHex(resolved);
            return ColorData.normalizeSectionCodes(resolved);
        } catch (Exception e) {
            logger.warn("Failed to set placeholders for {}", player.getName(), e);
            return text;
        }
    }

    interface Integration {
        String resolve(Player player, String text);
        void unregister();
    }
}
