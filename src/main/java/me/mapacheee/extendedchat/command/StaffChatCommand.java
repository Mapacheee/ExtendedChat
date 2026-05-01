package me.mapacheee.extendedchat.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.configurate.Container;
import me.mapacheee.extendedchat.config.EcMessages;
import me.mapacheee.extendedchat.service.StaffChatService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

@CommandComponent
public final class StaffChatCommand {

    private final StaffChatService staffChatService;
    private final Container<EcMessages> messages;

    @Inject
    public StaffChatCommand(StaffChatService staffChatService, Container<EcMessages> messages) {
        this.staffChatService = staffChatService;
        this.messages = messages;
    }

    @Command("staffchat")
    public void toggleStaffChat(Source source) {
        if (source.source() instanceof Player player) {
            if (!staffChatService.isEnabled()) {
                return;
            }
            if (!player.hasPermission(staffChatService.getStaffPermission())) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        messages.get().prefix() + messages.get().staffChatNoPermission()));
                return;
            }
            boolean enabled = staffChatService.toggleStaffChat(player.getUniqueId());
            String msg = messages.get().prefix() + (enabled
                    ? messages.get().staffChatEnabled()
                    : messages.get().staffChatDisabled());
            player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
        }
    }

    @Command("sc")
    public void toggleStaffChatAlias(Source source) {
        toggleStaffChat(source);
    }

    @Command("staffchat <message>")
    public void staffChatMessage(Source source, @Argument("message") String[] message) {
        if (source.source() instanceof Player player) {
            if (!staffChatService.isEnabled()) {
                return;
            }
            if (!player.hasPermission(staffChatService.getStaffPermission())) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        messages.get().prefix() + messages.get().staffChatNoPermission()));
                return;
            }
            String msg = String.join(" ", message);
            staffChatService.sendStaffMessage(player, msg);
        }
    }

    @Command("sc <message>")
    public void staffChatMessageAlias(Source source, @Argument("message") String[] message) {
        staffChatMessage(source, message);
    }
}