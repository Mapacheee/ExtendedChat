package me.mapacheee.extendedchat.color;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.service.annotation.Service;
import com.thewinterframework.service.annotation.lifecycle.OnDisable;
import com.thewinterframework.service.annotation.lifecycle.OnEnable;
import me.mapacheee.extendedchat.ExtendedChatPlugin;
import me.mapacheee.extendedchat.config.EcConfig;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Service
public final class ColorService {

    private final Container<EcConfig> config;
    private final Logger logger;
    private final Map<UUID, ColorData> colorCache = new ConcurrentHashMap<>();
    private final Map<UUID, ColorInputSession> pendingInputs = new ConcurrentHashMap<>();
    private File colorDataFile;
    private ConfigurationNode colorNode;
    private YamlConfigurationLoader loader;

    @Inject
    public ColorService(Container<EcConfig> config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @OnEnable
    public void onEnable() {
        if (!config.get().colorEnabled()) {
            return;
        }

        ExtendedChatPlugin plugin = ExtendedChatPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        colorDataFile = new File(plugin.getDataFolder(), "colors.yml");
        try {
            if (!colorDataFile.exists()) {
                colorDataFile.createNewFile();
            }
            loader = YamlConfigurationLoader.builder()
                    .file(colorDataFile)
                    .build();
            colorNode = loader.load();
            loadAllColors();
        } catch (Exception e) {
            logger.error("Failed to initialize color storage", e);
        }
    }

    @OnDisable
    public void onDisable() {
        saveAllColors();
    }

    private void loadAllColors() {
        if (colorNode == null) {
            return;
        }
        try {
            for (Object key : colorNode.childrenMap().keySet()) {
                UUID uuid = UUID.fromString(key.toString());
                ConfigurationNode playerNode = colorNode.node(key.toString());
                String nameColor = playerNode.node("nameColor").getString("<white>");
                String messageColor = playerNode.node("messageColor").getString("<white>");
                ColorData data = new ColorData(nameColor, messageColor);
                colorCache.put(uuid, data);
            }
            logger.info("Loaded {} player colors", colorCache.size());
        } catch (Exception e) {
            logger.error("Failed to load colors", e);
        }
    }

    public void saveAllColors() {
        if (colorNode == null || colorCache.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<UUID, ColorData> entry : colorCache.entrySet()) {
                String uuidStr = entry.getKey().toString();
                ConfigurationNode playerNode = colorNode.node(uuidStr);
                playerNode.node("nameColor").set(entry.getValue().getNameColor());
                playerNode.node("messageColor").set(entry.getValue().getMessageColor());
            }
            loader.save(colorNode);
        } catch (Exception e) {
            logger.error("Failed to save colors", e);
        }
    }

    public ColorData getColorData(Player player) {
        if (!config.get().colorEnabled()) {
            return new ColorData();
        }
        UUID uuid = player.getUniqueId();
        return colorCache.computeIfAbsent(uuid, key -> {
            loadPlayerColor(uuid);
            return colorCache.getOrDefault(uuid, new ColorData());
        });
    }

    private void loadPlayerColor(UUID uuid) {
        if (colorNode == null) {
            return;
        }
        try {
            ConfigurationNode playerNode = colorNode.node(uuid.toString());
            if (playerNode.virtual()) {
                return;
            }
            String nameColor = playerNode.node("nameColor").getString("<white>");
            String messageColor = playerNode.node("messageColor").getString("<white>");
            colorCache.put(uuid, new ColorData(nameColor, messageColor));
        } catch (Exception e) {
            logger.error("Failed to load color for {}", uuid, e);
        }
    }

    public void saveColorData(Player player, ColorData data) {
        if (!config.get().colorEnabled()) {
            return;
        }
        colorCache.put(player.getUniqueId(), data);

        if (colorNode != null) {
            try {
                ConfigurationNode playerNode = colorNode.node(player.getUniqueId().toString());
                playerNode.node("nameColor").set(data.getNameColor());
                playerNode.node("messageColor").set(data.getMessageColor());
                loader.save(colorNode);
            } catch (Exception e) {
                logger.error("Failed to save color for {}", player.getName(), e);
            }
        }
    }

    public void setNameColor(Player player, String color) {
        ColorData data = getColorData(player);
        data.setNameColor(color);
        saveColorData(player, data);
    }

    public void setMessageColor(Player player, String color) {
        ColorData data = getColorData(player);
        data.setMessageColor(color);
        saveColorData(player, data);
    }

    public void clearColors(Player player) {
        colorCache.remove(player.getUniqueId());
        if (colorNode != null) {
            try {
                colorNode.node(player.getUniqueId().toString()).set(null);
                loader.save(colorNode);
            } catch (Exception e) {
                logger.error("Failed to clear colors for {}", player.getName(), e);
            }
        }
    }

    public boolean hasPendingInput(UUID uuid) {
        return pendingInputs.containsKey(uuid);
    }

    public ColorInputSession getPendingInput(UUID uuid) {
        return pendingInputs.get(uuid);
    }

    public void setPendingInput(UUID uuid, ColorInputSession session) {
        pendingInputs.put(uuid, session);
    }

    public void removePendingInput(UUID uuid) {
        pendingInputs.remove(uuid);
    }

    public enum ColorTarget {
        NAME, MESSAGE
    }

    public enum InputType {
        HEX, GRADIENT
    }

    public record ColorInputSession(ColorTarget target, InputType inputType) {}
}