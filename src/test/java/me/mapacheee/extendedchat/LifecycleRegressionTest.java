package me.mapacheee.extendedchat;

import com.thewinterframework.configurate.Container;
import me.mapacheee.extendedchat.color.ColorService;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.config.EcMessages;
import me.mapacheee.extendedchat.service.FilterService;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleRegressionTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleRegressionTest.class);

    @TempDir
    Path directory;

    @Test
    void filtersUseReloadedPatternsEvenWhenHookRunsBeforeContainerReload() throws Exception {
        Files.writeString(directory.resolve("config.yml"), filterConfig("blocked"));
        Container<EcConfig> config = Container.load(LOGGER, directory, EcConfig.class, "config");
        Container<EcMessages> messages = Container.load(LOGGER, directory, EcMessages.class, "messages");
        FilterService filters = new FilterService(config, messages, LOGGER);
        Player player = player(UUID.randomUUID());
        filters.updatePatterns();
        assertFalse(filters.canSendMessage(player, "blocked"));
        assertTrue(filters.canSendMessage(player, "different"));

        Files.writeString(directory.resolve("config.yml"), filterConfig("different"));
        filters.updatePatterns();
        assertTrue(config.reload());
        assertTrue(filters.canSendMessage(player, "blocked"));
        assertFalse(filters.canSendMessage(player, "different"));
    }

    @Test
    void colorsSurviveDisabledStartupAndConcurrentPlayerUpdates() throws Exception {
        UUID existing = UUID.randomUUID();
        Files.writeString(directory.resolve("config.yml"), "color-enabled: false\n");
        Files.writeString(directory.resolve("colors.yml"), existing + ":\n  nameColor: '<gold>'\n  messageColor: '<red>'\n");
        Container<EcConfig> config = Container.load(LOGGER, directory, EcConfig.class, "config");
        ColorService colors = new ColorService(config, LOGGER);
        colors.onEnable(directory);
        Files.writeString(directory.resolve("config.yml"), "color-enabled: true\ndefault-name-color: '<aqua>'\n");
        assertTrue(config.reload());
        assertEquals("<gold>", colors.getColorData(player(existing)).getNameColor());

        ArrayList<UUID> players = new ArrayList<>();
        ArrayList<Callable<Void>> updates = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            UUID uuid = UUID.randomUUID();
            players.add(uuid);
            updates.add(() -> {
                Player player = player(uuid);
                assertEquals("<aqua>", colors.getColorData(player).getNameColor());
                colors.setNameColor(player, "<green>");
                colors.setMessageColor(player, "<yellow>");
                return null;
            });
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            for (var result : executor.invokeAll(updates)) {
                result.get();
            }
        }
        colors.onDisable();
        ColorService restored = new ColorService(config, LOGGER);
        restored.onEnable(directory);
        for (UUID uuid : players) {
            assertEquals("<green>", restored.getColorData(player(uuid)).getNameColor());
            assertEquals("<yellow>", restored.getColorData(player(uuid)).getMessageColor());
        }
        assertEquals("<gold>", restored.getColorData(player(existing)).getNameColor());
    }

    private static String filterConfig(String pattern) {
        return "filters-enabled: true\nanti-spam-enabled: false\nanti-link-enabled: true\n"
                + "anti-link-regex-list: ['" + pattern + "']\nanti-link-whitelist: []\n";
    }

    private static Player player(UUID uuid) {
        return (Player) Proxy.newProxyInstance(Player.class.getClassLoader(), new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> uuid;
                    case "getName" -> "TestPlayer";
                    case "hasPermission" -> false;
                    case "sendMessage" -> null;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
