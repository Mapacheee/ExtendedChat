package me.mapacheee.extendedchat.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.configurate.Container;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.color.gui.ColorMenuGUI;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.config.EcMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.paper.util.sender.Source;

@CommandComponent
public final class ColorCommand {

    private final ColorService colorService;
    private final Container<EcConfig> config;
    private final Container<EcMessages> messages;

    @Inject
    public ColorCommand(ColorService colorService, Container<EcConfig> config, Container<EcMessages> messages) {
        this.colorService = colorService;
        this.config = config;
        this.messages = messages;
    }

    @Command("color")
    public void colorMenu(Source source) {
        if (source.source() instanceof Player player) {
            if (!config.get().colorEnabled()) {
                return;
            }
            if (!player.hasPermission("extendedchat.color")) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        messages.get().prefix() + messages.get().colorNoPermission()));
                return;
            }
            new ColorMenuGUI(messages.get()).open(player);
        }
    }

    @Command("color reset")
    public void colorReset(Source source) {
        if (source.source() instanceof Player player) {
            if (!config.get().colorEnabled()) {
                return;
            }
            colorService.clearColors(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    messages.get().prefix() + messages.get().colorRemoved()));
        }
    }
}