package me.mapacheee.extendedchat.service;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.color.ColorData;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.config.EcMessages;
import me.mapacheee.extendedchat.hook.PlaceholderHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class PrivateMessageService {

    private final Container<EcConfig> config;
    private final Container<EcMessages> messages;
    private final ColorService colorService;
    private final FilterService filterService;
    private final PlaceholderHook placeholderHook;
    private final Logger logger;
    private final boolean papiEnabled;
    private final Map<UUID, UUID> lastMessagedBy = new ConcurrentHashMap<>();

    @Inject
    public PrivateMessageService(
            Container<EcConfig> config,
            Container<EcMessages> messages,
            ColorService colorService,
            FilterService filterService,
            PlaceholderHook placeholderHook,
            Logger logger
    ) {
        this.config = config;
        this.messages = messages;
        this.colorService = colorService;
        this.filterService = filterService;
        this.placeholderHook = placeholderHook;
        this.logger = logger;
        this.papiEnabled = placeholderHook.isEnabled();
    }

    public void sendPrivateMessage(Player sender, String targetName, String message) {
        if (!config.get().privateMessagesEnabled()) {
            return;
        }
        if (!filterService.canSendMessage(sender, message)) {
            return;
        }

        String plainMessage = message;
        if (!sender.hasPermission("extendedchat.color.ingame")) {
            plainMessage = MiniMessage.miniMessage().escapeTags(plainMessage);
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sendPlayerMessage(sender, MiniMessage.miniMessage().deserialize(
                    messages.get().prefix() + messages.get().msgPlayerNotFound()));
            return;
        }

        if (target.equals(sender)) {
            sendPlayerMessage(sender, MiniMessage.miniMessage().deserialize(
                    messages.get().prefix() + messages.get().msgSelfMessage()));
            return;
        }

        ColorData senderColors = colorService.getColorData(sender);

        lastMessagedBy.put(target.getUniqueId(), sender.getUniqueId());

        sendPlayerMessage(target, formatReceived(sender.getName(), plainMessage, target, senderColors));
        sendPlayerMessage(sender, formatSent(target.getName(), plainMessage, sender));
    }

    public void sendReply(Player sender, String message) {
        if (!config.get().privateMessagesEnabled()) {
            return;
        }
        UUID lastUuid = lastMessagedBy.get(sender.getUniqueId());
        if (lastUuid == null) {
            sendPlayerMessage(sender, MiniMessage.miniMessage().deserialize(
                    messages.get().prefix() + messages.get().msgNoReply()));
            return;
        }

        Player target = Bukkit.getPlayer(lastUuid);
        if (target == null || !target.isOnline()) {
            sendPlayerMessage(sender, MiniMessage.miniMessage().deserialize(
                    messages.get().prefix() + messages.get().msgPlayerNotFound()));
            return;
        }

        sendPrivateMessage(sender, target.getName(), message);
    }

    private void sendPlayerMessage(Player player, Component component) {
        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        if (plugin == null) {
            player.sendMessage(component);
            return;
        }
        player.getScheduler().run(plugin, task -> player.sendMessage(component), null);
    }

    public UUID getLastMessagedBy(UUID uuid) {
        return lastMessagedBy.get(uuid);
    }

    private Component formatSent(String targetName, String message, Player sender) {
        ColorData colorData = colorService.getColorData(sender);
        Component messageComponent = MiniMessage.miniMessage().deserialize(
                colorData.applyMessageColor(message));
        Component targetComponent = MiniMessage.miniMessage().deserialize(targetName);

        String format = config.get().privateMsgSentFormat();
        if (papiEnabled) {
            format = placeholderHook.setPlaceholders(sender, format);
        }

        TagResolver resolver = TagResolver.builder()
                .resolver(Placeholder.component("target_name", targetComponent))
                .resolver(Placeholder.component("message", messageComponent))
                .build();
        return MiniMessage.miniMessage().deserialize(format, resolver);
    }

    private Component formatReceived(String senderName, String message, Player target, ColorData senderColors) {
        Component nameComponent = MiniMessage.miniMessage().deserialize(
                senderColors.applyNameColor(senderName));
        Component messageComponent = MiniMessage.miniMessage().deserialize(
                senderColors.applyMessageColor(message));

        String format = config.get().privateMsgReceivedFormat();
        if (papiEnabled) {
            format = placeholderHook.setPlaceholders(target, format);
        }

        TagResolver resolver = TagResolver.builder()
                .resolver(Placeholder.component("sender_name", nameComponent))
                .resolver(Placeholder.component("message", messageComponent))
                .build();
        return MiniMessage.miniMessage().deserialize(format, resolver);
    }
}

