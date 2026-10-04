# ExtendedChat

ExtendedChat is a chat plugin for Paper and Folia built on the Winter Framework. It provides configurable chat formats, staff chat, private messaging, chat filters, and persistent player colors.

## Features

- Permission-based chat formats with weighted priorities.
- Staff chat with direct messages and a toggle mode.
- Private messages with aliases and reply support.
- Configurable spam cooldowns, link filters, and link allowlists.
- Player name and message colors, including HEX colors and gradients.
- Command tab-completion filtering with allowlist and blocklist modes.
- Optional PlaceholderAPI integration.
- Configuration reloads without restarting the server.

## Commands

| Command | Description |
| --- | --- |
| `/extendedchat reload` | Reload the plugin configuration and messages. |
| `/staffchat` or `/sc` | Toggle staff chat. |
| `/staffchat <message>` or `/sc <message>` | Send a staff message directly. |
| `/msg <target> <message>` | Send a private message. Aliases: `/w`, `/tell`. |
| `/reply <message>` or `/r <message>` | Reply to the last player who messaged you. |
| `/color` | Open the color selection menu. |
| `/color reset` | Clear your saved colors. |

## Permissions

| Permission | Description |
| --- | --- |
| `extendedchat.admin` | Allows configuration reloads. |
| `extendedchat.staff` | Default permission for sending and receiving staff chat. Configurable through `staff-chat-permission`. |
| `extendedchat.color` | Allows access to the color menu and preset colors. |
| `extendedchat.color.hex` | Allows custom HEX colors. |
| `extendedchat.color.gradients` | Allows custom gradients when gradients are enabled. |
| `extendedchat.color.ingame` | Allows MiniMessage tags in chat messages. |
| `extendedchat.antispam.bypass` | Bypasses the chat cooldown. |
| `extendedchat.antilink.bypass` | Bypasses the link filter. |

Permissions for individual chat formats are defined in `chat-formats`.

## PlaceholderAPI

| Placeholder | Value |
| --- | --- |
| `%extendedchat_name_color%` | The player's name color in MiniMessage format. |
| `%extendedchat_message_color%` | The player's message color in MiniMessage format. |
| `%extendedchat_name_color_legacy%` | The player's name color in legacy format. |
| `%extendedchat_message_color_legacy%` | The player's message color in legacy format. |

Legacy gradient placeholders return the first gradient color. Placeholders from other installed expansions can also be used in chat formats.
