package me.mapacheee.extendedchat.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorService;
import org.bukkit.entity.Player;

final class PlaceholderApiIntegration implements PlaceholderHook.Integration {
    private final ExtendedChatExpansion expansion;

    PlaceholderApiIntegration(ExtendedChatPlugin plugin, ColorService colorService) {
        expansion = new ExtendedChatExpansion(plugin, colorService);
        expansion.register();
    }

    @Override
    public String resolve(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    @Override
    public void unregister() {
        expansion.unregister();
    }
}
