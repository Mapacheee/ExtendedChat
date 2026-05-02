package me.mapacheee.extendedchat.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
@Configurate("messages")
public record EcMessages(
        @Setting("prefix") String prefix,
        @Setting("reloaded") String reloaded,
        @Setting("staff-chat-no-permission") String staffChatNoPermission,
        @Setting("staff-chat-enabled") String staffChatEnabled,
        @Setting("staff-chat-disabled") String staffChatDisabled,
        @Setting("color-no-permission") String colorNoPermission,
        @Setting("color-no-permission-hex") String colorNoPermissionHex,
        @Setting("color-no-permission-gradient") String colorNoPermissionGradient,
        @Setting("color-prompt-hex") String colorPromptHex,
        @Setting("color-prompt-gradient") String colorPromptGradient,
        @Setting("color-invalid-hex") String colorInvalidHex,
        @Setting("color-invalid-gradient") String colorInvalidGradient,
        @Setting("color-updated") String colorUpdated,
        @Setting("color-removed") String colorRemoved,
        @Setting("color-cancelled") String colorCancelled,
        @Setting("gui-color-menu-title") String colorMenuTitle,
        @Setting("gui-color-picker-name-title") String colorPickerNameTitle,
        @Setting("gui-color-picker-message-title") String colorPickerMessageTitle,
        @Setting("gui-items-name-color") String colorNameItem,
        @Setting("gui-items-name-color-lore") List<String> colorNameItemLore,
        @Setting("gui-items-message-color") String colorMessageItem,
        @Setting("gui-items-message-color-lore") List<String> colorMessageItemLore,
        @Setting("gui-items-custom-color") String colorCustomItem,
        @Setting("gui-items-custom-color-lore") List<String> colorCustomItemLore,
        @Setting("gui-items-back") String colorBackItem,
        @Setting("gui-items-back-lore") List<String> colorBackItemLore,
        @Setting("gui-items-remove") String colorRemoveItem,
        @Setting("gui-items-remove-lore") List<String> colorRemoveItemLore,
        @Setting("msg-player-not-found") String msgPlayerNotFound,
        @Setting("msg-self-message") String msgSelfMessage,
        @Setting("msg-no-reply") String msgNoReply,
        @Setting("anti-spam-message") String antispamMessage,
        @Setting("anti-link-message") String antiLinkMessage,
        @Setting("title-hex-input") String titleHexInput,
        @Setting("title-hex-input-subtitle") String titleHexInputSubtitle,
        @Setting("title-gradient-input") String titleGradientInput,
        @Setting("title-gradient-input-subtitle") String titleGradientInputSubtitle
) {
    public static EcMessages defaults() {
        return new EcMessages(
                "<dark_gray>[<gold>ExtendedChat<dark_gray>] <gray>",
                "<green>Configuration reloaded.",
                "<red>You don't have permission to use staff chat.",
                "<green>Staff chat enabled.",
                "<red>Staff chat disabled.",
                "<red>You don't have permission to use colors.",
                "<red>You don't have permission to use hex colors.",
                "<red>You don't have permission to use gradient colors.",
                "<green>Enter a HEX color (e.g., #ff00aa) or 'cancel' to cancel.",
                "<green>Enter two HEX colors separated by comma (e.g., #ff00aa,#00ffaa) or 'cancel' to cancel.",
                "<red>Invalid HEX color. Use #RRGGBB.",
                "<red>Invalid gradient. Use two HEX colors: #RRGGBB,#RRGGBB.",
                "<green>Color updated.",
                "<green>Color removed.",
                "<red>Input cancelled.",
                "<#F2AE2E>✦ ExtendedChat Color",
                "✦ Name Color",
                "✦ Message Color",
                "<#5BF0FF>Name Color",
                List.of("<gray>Change the color of your", "<gray>name in chat.", "", "<yellow>Click to edit"),
                "<#29C54D>Message Color",
                List.of("<gray>Change the color of your", "<gray>messages.", "", "<yellow>Click to edit"),
                "<gradient:#F2AE2E:#F27B35>Custom",
                List.of("<gray>Left click for HEX", "<gray>Right click for Gradient"),
                "<gray>← Back",
                List.of("<gray>Go back to main menu"),
                "<red>Remove",
                List.of("<gray>Remove current color"),
                "<red>Player not found.",
                "<red>You cannot message yourself.",
                "<red>No one to reply to.",
                "<dark_gray>[<red>Anti-Spam<dark_gray>] <red>Wait a bit before sending another message.",
                "<dark_gray>[<red>Anti-Link<dark_gray>] <red>Links are not allowed.",
                "<#F2AE2E>HEX",
                "<gray>Type in chat or 'cancel'",
                "<gradient:#F2AE2E:#F27B35>Gradient",
                "<gray>Type in chat or 'cancel'"
        );
    }
}