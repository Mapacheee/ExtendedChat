package me.mapacheee.extendedchat.listener;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.extendedchat.config.EcConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@ListenerComponent
public final class CommandTabListener implements Listener {

    private final Container<EcConfig> config;

    @Inject
    public CommandTabListener(Container<EcConfig> config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSend(PlayerCommandSendEvent event) {
        EcConfig cfg = config.get();
        if (!cfg.commandTabEnabled()) {
            return;
        }

        Set<String> list = normalizeList(cfg.commandTabList());
        if (list.isEmpty()) {
            return;
        }

        boolean whitelist = "whitelist".equalsIgnoreCase(cfg.commandTabMode());

        event.getCommands().removeIf(cmd -> {
            String normalized = normalize(cmd);
            String stripped = stripNamespace(normalized);
            boolean match = list.contains(normalized) || list.contains(stripped);
            return whitelist != match;
        });
    }

    private Set<String> normalizeList(Iterable<String> items) {
        Set<String> result = new HashSet<>();
        if (items == null) {
            return result;
        }
        for (String item : items) {
            if (item == null || item.isBlank()) {
                continue;
            }
            result.add(normalize(item));
        }
        return result;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String stripNamespace(String value) {
        int idx = value.indexOf(':');
        if (idx == -1) {
            return value;
        }
        return value.substring(idx + 1);
    }
}

