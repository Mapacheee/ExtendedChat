package me.mapacheee.extendedchat.hook;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

@Service
public final class PlaceholderHook {

    private final Logger logger;
    private final boolean enabled;
    private final Plugin papiPlugin;

    @Inject
    public PlaceholderHook(Logger logger) {
        this.logger = logger;
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

    public String setPlaceholders(Player player, String text) {
        if (!enabled || player == null || text == null) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            logger.warn("Failed to set placeholders for {}", player.getName(), e);
            return text;
        }
    }
}