package me.mapacheee.extendedchat.color;

import com.google.inject.Inject;
import com.thewinterframework.configurate.Container;
import com.thewinterframework.paper.listener.ListenerComponent;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.mapacheee.extendedchat.color.gui.ColorMenuGUI;
import me.mapacheee.extendedchat.color.gui.ColorPickerGUI;
import me.mapacheee.extendedchat.config.EcConfig;
import me.mapacheee.extendedchat.config.EcMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@ListenerComponent
public final class ColorListener implements Listener {

    private final ColorService colorService;
    private final Container<EcConfig> config;
    private final Container<EcMessages> messages;

    @Inject
    public ColorListener(ColorService colorService, Container<EcConfig> config, Container<EcMessages> messages) {
        this.colorService = colorService;
        this.config = config;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!config.get().colorEnabled()) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        EcMessages msgs = messages.get();

        if (holder instanceof ColorMenuGUI) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getRawSlot() == 11) {
                new ColorPickerGUI(ColorService.ColorTarget.NAME, msgs).open(player);
            } else if (event.getRawSlot() == 15) {
                new ColorPickerGUI(ColorService.ColorTarget.MESSAGE, msgs).open(player);
            }
            return;
        }

        if (holder instanceof ColorPickerGUI picker) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slot = event.getRawSlot();

            if (slot == 36) {
                new ColorMenuGUI(msgs).open(player);
                return;
            }

            if (slot == 40) {
                if (picker.getTarget() == ColorService.ColorTarget.NAME) {
                    colorService.setNameColor(player, "<white>");
                } else {
                    colorService.setMessageColor(player, "<white>");
                }
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        msgs.prefix() + msgs.colorRemoved()));
                player.closeInventory();
                return;
            }

            if (slot == 31) {
                ClickType click = event.getClick();
                ColorService.InputType inputType = click.isLeftClick()
                        ? ColorService.InputType.HEX
                        : ColorService.InputType.GRADIENT;

                if (inputType == ColorService.InputType.GRADIENT && !config.get().colorGradientsEnabled()) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + msgs.colorNoPermissionGradient()));
                    return;
                }

                String permission = inputType == ColorService.InputType.HEX
                        ? "extendedchat.color.hex" : "extendedchat.color.gradients";
                if (!player.hasPermission(permission)) {
                    String noPermMsg = inputType == ColorService.InputType.HEX
                            ? msgs.colorNoPermissionHex() : msgs.colorNoPermissionGradient();
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + noPermMsg));
                    return;
                }

                colorService.setPendingInput(player.getUniqueId(),
                        new ColorService.ColorInputSession(picker.getTarget(), inputType));
                player.closeInventory();

                String msg = inputType == ColorService.InputType.HEX
                        ? msgs.colorPromptHex() : msgs.colorPromptGradient();
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        msgs.prefix() + msg));

                String titleText = inputType == ColorService.InputType.HEX
                        ? msgs.titleHexInput() : msgs.titleGradientInput();
                String subtitleText = inputType == ColorService.InputType.HEX
                        ? msgs.titleHexInputSubtitle() : msgs.titleGradientInputSubtitle();
                Title title = Title.title(
                        MiniMessage.miniMessage().deserialize(titleText),
                        MiniMessage.miniMessage().deserialize(subtitleText)
                );
                player.showTitle(title);
                return;
            }

            String color = getColorFromSlot(slot);
            if (color != null) {
                if (!player.hasPermission("extendedchat.color")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + msgs.colorNoPermission()));
                    return;
                }

                String colorTag = "<" + color + ">";
                if (picker.getTarget() == ColorService.ColorTarget.NAME) {
                    colorService.setNameColor(player, colorTag);
                } else {
                    colorService.setMessageColor(player, colorTag);
                }
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        msgs.prefix() + msgs.colorUpdated()));
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatInput(AsyncChatEvent event) {
        if (!config.get().colorEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        EcMessages msgs = messages.get();

        if (colorService.hasPendingInput(uuid)) {
            event.setCancelled(true);
            ColorService.ColorInputSession session = colorService.getPendingInput(uuid);
            String input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

            if (session.inputType() == ColorService.InputType.GRADIENT && !config.get().colorGradientsEnabled()) {
                colorService.removePendingInput(uuid);
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        msgs.prefix() + msgs.colorNoPermissionGradient()));
                return;
            }

            if (input.equalsIgnoreCase("cancelar") || input.equalsIgnoreCase("cancel")) {
                colorService.removePendingInput(uuid);
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        msgs.prefix() + msgs.colorCancelled()));
                return;
            }

            String colorTag;
            if (session.inputType() == ColorService.InputType.HEX) {
                if (input.startsWith("#")) {
                    input = input.substring(1);
                }
                if (!ColorData.isValidHexColor(input)) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + msgs.colorInvalidHex()));
                    return;
                }
                colorTag = ColorData.formatHexColor(input);
            } else {
                String[] parts = input.split(",| ");
                if (parts.length < 2) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + msgs.colorInvalidGradient()));
                    return;
                }
                String c1 = parts[0].startsWith("#") ? parts[0].substring(1) : parts[0];
                String c2 = parts[1].startsWith("#") ? parts[1].substring(1) : parts[1];
                if (!ColorData.isValidHexColor(c1) || !ColorData.isValidHexColor(c2)) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            msgs.prefix() + msgs.colorInvalidGradient()));
                    return;
                }
                colorTag = "<gradient:#" + c1 + ":#" + c2 + ">";
            }

            if (session.target() == ColorService.ColorTarget.NAME) {
                colorService.setNameColor(player, colorTag);
            } else {
                colorService.setMessageColor(player, colorTag);
            }

            colorService.removePendingInput(uuid);
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                    msgs.prefix() + msgs.colorUpdated()));
        }
    }

    private String getColorFromSlot(int slot) {
        return switch (slot) {
            case 10 -> "red";
            case 11 -> "gold";
            case 12 -> "yellow";
            case 13 -> "green";
            case 14 -> "dark_green";
            case 15 -> "aqua";
            case 16 -> "dark_aqua";
            case 19 -> "blue";
            case 20 -> "dark_purple";
            case 21 -> "light_purple";
            case 22 -> "white";
            case 23 -> "gray";
            case 24 -> "dark_gray";
            case 25 -> "black";
            default -> null;
        };
    }
}