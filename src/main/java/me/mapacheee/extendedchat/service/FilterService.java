package me.mapacheee.extendedchat.service;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.config.EcMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public final class FilterService {

    private final Container<EcConfig> config;
    private final Container<EcMessages> messages;
    private final Logger logger;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final List<Pattern> antiLinkPatterns = new ArrayList<>();

    @Inject
    public FilterService(Container<EcConfig> config, Container<EcMessages> messages, Logger logger) {
        this.config = config;
        this.messages = messages;
        this.logger = logger;
        updatePatterns();
    }

    public boolean canSendMessage(Player player, String message) {
        EcConfig cfg = config.get();
        if (!cfg.filtersEnabled()) {
            return true;
        }
        if (!cfg.antiSpamEnabled() && !cfg.antiLinkEnabled()) {
            return true;
        }

        if (cfg.antiSpamEnabled() && !player.hasPermission("extendedchat.antispam.bypass")) {
            long now = System.currentTimeMillis();
            long lastMessage = cooldowns.getOrDefault(player.getUniqueId(), 0L);
            long cooldownMs = (long) (cfg.antiSpamCooldown() * 1000);

            if (now - lastMessage < cooldownMs) {
                sendPlayerMessage(player, MiniMessage.miniMessage().deserialize(
                        messages.get().prefix() + messages.get().antispamMessage()));
                return false;
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        if (cfg.antiLinkEnabled() && !player.hasPermission("extendedchat.antilink.bypass")) {
            if (containsBlockedLink(message)) {
                sendPlayerMessage(player, MiniMessage.miniMessage().deserialize(
                        messages.get().prefix() + messages.get().antiLinkMessage()));
                return false;
            }
        }

        return true;
    }

    private void sendPlayerMessage(Player player, net.kyori.adventure.text.Component component) {
        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        if (plugin == null) {
            player.sendMessage(component);
            return;
        }
        player.getScheduler().run(plugin, task -> player.sendMessage(component), null);
    }

    private boolean containsBlockedLink(String message) {
        if (antiLinkPatterns.isEmpty()) {
            updatePatterns();
        }

        for (Pattern pattern : antiLinkPatterns) {
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String link = matcher.group();
                if (!isWhitelisted(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWhitelisted(String link) {
        String lowerLink = link.toLowerCase();
        for (String whitelisted : config.get().antiLinkWhitelist()) {
            if (lowerLink.contains(whitelisted.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void updatePatterns() {
        antiLinkPatterns.clear();
        EcConfig cfg = config.get();
        if (cfg.antiLinkRegexList() == null) {
            return;
        }
        for (String regex : cfg.antiLinkRegexList()) {
            try {
                antiLinkPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                logger.error("Invalid regex pattern: {}", regex, e);
            }
        }
    }
}