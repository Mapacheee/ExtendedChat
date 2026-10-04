package me.mapacheee.extendedchat.listener;

import com.google.inject.Inject;
import com.thewinterframework.paper.listener.ListenerComponent;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.mapacheee.extendedchat.service.ChatService;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.service.FilterService;
import me.mapacheee.extendedchat.service.StaffChatService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@ListenerComponent
public final class ChatListener implements Listener {

    private final ChatService chatService;
    private final StaffChatService staffChatService;
    private final FilterService filterService;
    private final ColorService colorService;

    @Inject
    public ChatListener(
            ChatService chatService,
            StaffChatService staffChatService,
            FilterService filterService,
            ColorService colorService
    ) {
        this.chatService = chatService;
        this.staffChatService = staffChatService;
        this.filterService = filterService;
        this.colorService = colorService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        colorService.removePendingInput(uuid);
        staffChatService.removeToggle(uuid);
        filterService.removePlayer(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (colorService.isEnabled() && colorService.hasPendingInput(player.getUniqueId())) {
            return;
        }
        if (!staffChatService.isEnabled()) {
            staffChatService.removeToggle(player.getUniqueId());
        } else if (staffChatService.isToggled(player.getUniqueId())) {
            Component messageComponent = event.message();
            String message = PlainTextComponentSerializer.plainText().serialize(messageComponent);
            if (!message.isBlank()) {
                event.setCancelled(true);
                staffChatService.sendStaffMessage(player, message);
            }
            return;
        }

        Component messageComponent = event.message();

        String message = PlainTextComponentSerializer.plainText().serialize(messageComponent);
        if (message.isBlank()) {
            return;
        }

        if (!filterService.canSendMessage(player, message)) {
            event.setCancelled(true);
            return;
        }

        Component formatted = chatService.buildChatComponent(player, messageComponent);

        if (formatted != null) {
            event.renderer((source, sourceDisplayName, chatMessage, viewer) -> formatted);
        }
    }
}
