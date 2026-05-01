package me.mapacheee.extendedchat.config;

import com.thewinterframework.configurate.config.Configurate;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
@Configurate("config")
public record EcConfig(
        @Setting("chat-format") String chatFormat,
        @Setting("staff-chat-format") String staffChatFormat,
        @Setting("staff-chat-enabled") boolean staffChatEnabled,
        @Setting("staff-chat-permission") String staffChatPermission,
        @Setting("private-msg-sent-format") String privateMsgSentFormat,
        @Setting("private-msg-received-format") String privateMsgReceivedFormat,
        @Setting("anti-spam-enabled") boolean antiSpamEnabled,
        @Setting("anti-spam-cooldown") double antiSpamCooldown,
        @Setting("anti-link-enabled") boolean antiLinkEnabled,
        @Setting("anti-link-regex-list") List<String> antiLinkRegexList,
        @Setting("anti-link-whitelist") List<String> antiLinkWhitelist,
        @Setting("color-enabled") boolean colorEnabled,
        @Setting("color-gradients-enabled") boolean colorGradientsEnabled,
        @Setting("default-name-color") String defaultNameColor,
        @Setting("default-message-color") String defaultMessageColor
) {
    public static EcConfig defaults() {
        return new EcConfig(
                "<dark_gray>[<gray>ExtendedChat<dark_gray>] <white><player_name><dark_gray>» <white><message>",
                "<red>[STAFF] <player_name> <dark_gray>» <white><message>",
                true,
                "extendedchat.staff",
                "<gray>You → <gold><target_name> <white><message>",
                "<gold><sender_name> <gray>→ You <white><message>",
                true,
                1.0,
                true,
                List.of(
                        "(https?://|www\\.)[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
                        "(?i)(discord\\.gg|minecraft\\.server\\.me)"
                ),
                List.of("hypixel.net", "cubecraft.net"),
                true,
                true,
                "<white>",
                "<white>"
        );
    }
}