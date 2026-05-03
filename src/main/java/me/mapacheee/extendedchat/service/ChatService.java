package me.mapacheee.extendedchat.service;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.hook.PlaceholderHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.List;

@Service
public final class ChatService {

    private static final String DEFAULT_FORMAT = "<white><player_name><dark_gray>» <white><message>";

    private final Container<EcConfig> config;
    private final ColorService colorService;
    private final PlaceholderHook placeholderHook;
    private final Logger logger;
    private final boolean papiEnabled;

    @Inject
    public ChatService(
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

    public Component buildChatComponent(Player player, Component messageComponent) {
        try {
            if (messageComponent == null) {
                logger.warn("Message component is null for player {}", player.getName());
                return null;
            }

            String plainMessage = PlainTextComponentSerializer.plainText().serialize(messageComponent);
            if (plainMessage.isBlank()) {
                logger.warn("Message is empty or null for player {}", player.getName());
                return null;
            }

            if (!player.hasPermission("extendedchat.color.ingame")) {
                plainMessage = MiniMessage.miniMessage().escapeTags(plainMessage);
            }

            EcConfig cfg = config.get();
            if (!cfg.chatFormatEnabled()) {
                return null;
            }
            String format = resolveChatFormat(player, cfg);
            if (format == null || format.isBlank()) {
                format = DEFAULT_FORMAT;
            }

            if (papiEnabled && placeholderHook != null) {
                try {
                    format = placeholderHook.setPlaceholders(player, format);
                } catch (Exception e) {
                    logger.warn("Failed to set placeholders for {}", player.getName(), e);
                }
            }

            ColorData colorData = colorService.getColorData(player);

            Component nameComponent = MiniMessage.miniMessage().deserialize(
                    colorData.applyNameColor(player.getName()));
            Component messageColored = MiniMessage.miniMessage().deserialize(
                    colorData.applyMessageColor(plainMessage));

            TagResolver resolver = TagResolver.builder()
                    .resolver(Placeholder.component("player_name", nameComponent))
                    .resolver(Placeholder.component("message", messageColored))
                    .build();

            logger.debug("Building component from format: {}", format);
            Component component = MiniMessage.miniMessage().deserialize(format, resolver);
            logger.debug("Successfully built component for player {}", player.getName());
            return component;

        } catch (Exception e) {
            logger.error("Error building chat component for {}: {}", player.getName(), e.getMessage());
            logger.debug("Stack trace:", e);
            return null;
        }
    }

    private String resolveChatFormat(Player player, EcConfig cfg) {
        if (cfg == null || player == null) {
            return DEFAULT_FORMAT;
        }

        if (!cfg.chatFormatsEnabled()) {
            String fallback = cfg.chatFormat();
            if (fallback == null || fallback.isBlank()) {
                return DEFAULT_FORMAT;
            }
            return fallback;
        }

        List<EcConfig.ChatFormatEntry> entries = cfg.chatFormats();
        String selected = null;
        int bestWeight = Integer.MIN_VALUE;

        if (entries != null) {
            for (EcConfig.ChatFormatEntry entry : entries) {
                if (entry == null) {
                    continue;
                }
                String entryFormat = entry.format();
                if (entryFormat == null || entryFormat.isBlank()) {
                    continue;
                }
                String permission = entry.permission();
                if (permission == null || permission.isBlank() || player.hasPermission(permission)) {
                    int weight = entry.weight();
                    if (weight > bestWeight) {
                        bestWeight = weight;
                        selected = entryFormat;
                    }
                }
            }
        }

        if (selected != null) {
            return selected;
        }

        String fallback = cfg.chatFormat();
        if (fallback == null || fallback.isBlank()) {
            return DEFAULT_FORMAT;
        }
        return fallback;
    }

    public void broadcastChat(Component formatted) {
        if (formatted == null) {
            return;
        }
        Component finalFormatted = formatted.append(Component.text("\n"));
        try {
            Bukkit.getServer().getGlobalRegionScheduler().execute(
                    Bukkit.getPluginManager().getPlugins()[0],
                    () -> Bukkit.getOnlinePlayers().forEach(player ->
                            player.sendMessage(finalFormatted))
            );
        } catch (Exception e) {
            logger.error("Error broadcasting chat", e);
        }
    }
}