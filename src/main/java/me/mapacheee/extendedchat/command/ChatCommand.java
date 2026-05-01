package me.mapacheee.extendedchat.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.ReloadServiceManager;
import me.mapacheee.extendedchat.config.EcMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

@CommandComponent
public final class ChatCommand {

    private final ReloadServiceManager reloadServiceManager;
    private final Container<EcMessages> messages;

    @Inject
    public ChatCommand(ReloadServiceManager reloadServiceManager, Container<EcMessages> messages) {
        this.reloadServiceManager = reloadServiceManager;
        this.messages = messages;
    }

    @Command("extendedchat reload")
    @Permission("extendedchat.admin")
    public void reload(Source source) {
        reloadServiceManager.reload();
        String msg = messages.get().prefix() + messages.get().reloaded();
        source.source().sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }
}