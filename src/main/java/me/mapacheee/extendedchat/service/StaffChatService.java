package me.mapacheee.extendedchat.service;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.hook.PlaceholderHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class StaffChatService {

    private final Container<EcConfig> config;
    private final ColorService colorService;
    private final PlaceholderHook placeholderHook;
    private final Logger logger;
    private final Set<UUID> toggledPlayers = ConcurrentHashMap.newKeySet();
    private final boolean papiEnabled;

    @Inject
    public StaffChatService(
            Container<EcConfig> config,
            ColorService colorService,
            PlaceholderHook placeholderHook,
            Logger logger
    ) {
        this.config = config;
        this.colorService = colorService;
        this.placeholderHook = placeholderHook;
        this.logger = logger;
        this.papiEnabled = placeholderHook.isEnabled();
    }

    public boolean isEnabled() {
        return config.get().staffChatEnabled();
    }

    public String getStaffPermission() {
        return config.get().staffChatPermission();
    }

    public boolean isToggled(UUID uuid) {
        return toggledPlayers.contains(uuid);
    }

    public boolean toggleStaffChat(UUID uuid) {
        if (toggledPlayers.contains(uuid)) {
            toggledPlayers.remove(uuid);
            return false;
        } else {
            toggledPlayers.add(uuid);
            return true;
        }
    }

    public void removeToggle(UUID uuid) {
        toggledPlayers.remove(uuid);
    }

    public void sendStaffMessage(Player sender, String message) {
        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        sender.getScheduler().run(plugin, task -> {
            EcConfig cfg = config.get();
            if (!cfg.staffChatEnabled()) {
                return;
            }

            if (!sender.hasPermission(cfg.staffChatPermission())) {
                return;
            }

            String plainMessage = message;
            if (!sender.hasPermission("extendedchat.color.ingame")) {
                plainMessage = MiniMessage.miniMessage().escapeTags(plainMessage);
            }

            ColorData colorData = colorService.getColorData(sender);
            String formattedName = colorData.applyNameColor(sender.getName());
            String formattedMessage = colorData.applyMessageColor(plainMessage);

            String format = cfg.staffChatFormat();

            if (papiEnabled) {
                format = placeholderHook.setPlaceholders(sender, format);
            }

            String translatedFormat = ColorData.translateLegacy(format);

            String finalFormat = translatedFormat
                    .replace("<player_name>", formattedName)
                    .replace("<message>", formattedMessage);

            Component formatted = MiniMessage.miniMessage().deserialize(finalFormat);

            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(cfg.staffChatPermission())) {
                        player.getScheduler().run(plugin, t -> player.sendMessage(formatted), null);
                    }
                }
            });
        }, null);
    }
}