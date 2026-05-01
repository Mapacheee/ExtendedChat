package me.mapacheee.extendedchat.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import me.mapacheee.extendedchat.service.PrivateMessageService;
import org.bukkit.Bukkit;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.List;

@CommandComponent
public final class PrivateMessageCommand {

    private final PrivateMessageService privateMessageService;

    @Inject
    public PrivateMessageCommand(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }

    @Suggestions("online-players")
    public List<String> suggestOnlinePlayers(CommandContext<Source> ctx, CommandInput input) {
        String prefix = input.peekString().toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> p.getName())
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
    }

    @Command("msg <target> <message>")
    public void sendMessage(Source source, @Argument(value = "target", suggestions = "online-players") String target, @Argument("message") String[] message) {
        if (source.source() instanceof org.bukkit.entity.Player player) {
            String msg = String.join(" ", message);
            privateMessageService.sendPrivateMessage(player, target, msg);
        }
    }

    @Command("w <target> <message>")
    public void sendMessageAlias(Source source, @Argument(value = "target", suggestions = "online-players") String target, @Argument("message") String[] message) {
        sendMessage(source, target, message);
    }

    @Command("tell <target> <message>")
    public void sendMessageAlias2(Source source, @Argument(value = "target", suggestions = "online-players") String target, @Argument("message") String[] message) {
        sendMessage(source, target, message);
    }

    @Command("reply <message>")
    public void sendReply(Source source, @Argument("message") String[] message) {
        if (source.source() instanceof org.bukkit.entity.Player player) {
            String msg = String.join(" ", message);
            privateMessageService.sendReply(player, msg);
        }
    }

    @Command("r <message>")
    public void sendReplyAlias(Source source, @Argument("message") String[] message) {
        sendReply(source, message);
    }
}