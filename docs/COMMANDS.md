# SimpleClan Commands

Complete command reference for SimpleClan plugin.

## Command Aliases

All commands can be used with the following aliases:
- `/clan`
- `/klan` (Polish)
- `/klany` (Polish plural)
- `/clans` (English plural)

## Command List

### Player Commands

#### Create Clan
```
/clan create <name>
/klan stworz <nazwa>
```
Creates a new clan with the specified name.

**Permission:** `simpleclan.create` (default: true)

**Requirements:**
- Player must not be in a clan
- Name must be between 3-16 characters (configurable)
- Name must be unique

**Example:**
```
/clan create Warriors
/klan stworz Wojownicy
```

---

#### Invite Player
```
/clan invite <player>
/klan zapros <gracz>
```
Invites a player to your clan.

**Permission:** `simpleclan.invite` (default: true)

**Requirements:**
- Must be clan leader or deputy
- Target player must be online
- Target player must not be in a clan
- Clan must not be full

**Example:**
```
/clan invite Steve
/klan zapros Steve
```

---

#### Accept Invitation
```
/clan accept
/klan akceptuj
```
Accepts a pending clan invitation.

**Permission:** None required

**Requirements:**
- Must have a pending invitation
- Must not be in a clan
- Clan must not be full

**Example:**
```
/clan accept
/klan akceptuj
```

---

#### Leave Clan
```
/clan leave
/klan opusc
```
Leave your current clan.

**Permission:** None required

**Requirements:**
- Must be in a clan
- Cannot be used by leader (use `/clan disband` instead)

**Example:**
```
/clan leave
/klan opusc
```

**Note:** If you're the leader, the clan must be disbanded using `/clan disband`

---

#### Disband Clan
```
/clan disband
/klan rozwiaz
```
Permanently dissolves your clan.

**Permission:** None required (Leader only)

**Requirements:**
- Must be clan leader

**Example:**
```
/clan disband
/klan rozwiaz
```

**Warning:** This action is permanent and cannot be undone!

---

#### List Members
```
/clan list
/klan lista
```
Shows all members of your clan with their roles.

**Permission:** None required

**Requirements:**
- Must be in a clan

**Example:**
```
/clan list
/klan lista
```

**Output:**
```
§8[§6SimpleClan§8] §6Members of Warriors:
§e• §6[Leader] §eSteve
§e• §a[Deputy] §eAlex
§e• §7[Member] §eNotch
```

---

#### Clan Information
```
/clan info [clan]
/klan info [klan]
```
Shows detailed information about a clan.

**Permission:** None required

**Requirements:**
- If no clan specified, you must be in a clan
- If clan specified, clan must exist

**Example:**
```
/clan info
/clan info Warriors
/klan info Wojownicy
```

**Output:**
```
§8[§6SimpleClan§8] §6Clan Information: Warriors
§7Leader: §eSteve
§7Deputies: §eAlex, John
§7Members: §e5§7/§e10
§7Created: §e01.01.2025 12:00
§7Tag Color: §6[Warriors]
§7PvP: §aEnabled
§7Kills: §e42
```

---

#### Promote to Deputy
```
/clan mod <player>
/clan moderator <player>
/klan zastepca <gracz>
```
Promotes a member to deputy rank.

**Permission:** None required (Leader only)

**Requirements:**
- Must be clan leader
- Target must be a clan member
- Target must not be leader
- Target must not already be deputy
- Clan must not have maximum deputies (default: 2)

**Example:**
```
/clan mod Steve
/klan zastepca Steve
```

---

#### Demote Deputy
```
/clan demote <player>
/klan degraduj <gracz>
```
Demotes a deputy to regular member.

**Permission:** None required (Leader only)

**Requirements:**
- Must be clan leader
- Target must be a deputy

**Example:**
```
/clan demote Steve
/klan degraduj Steve
```

---

#### Kick Member
```
/clan kick <player>
/klan wyrzuc <gracz>
```
Removes a member from the clan.

**Permission:** None required (Leader/Deputy)

**Requirements:**
- Must be clan leader or deputy
- Target must be a clan member
- Cannot kick yourself
- Cannot kick the leader
- Deputies can only kick regular members (not other deputies)

**Example:**
```
/clan kick Steve
/klan wyrzuc Steve
```

**Note:** This command works on offline players too!

---

#### Clan Chat
```
/clan chat <message>
/klan chat <wiadomość>
```
Sends a message to clan chat.

**Permission:** `simpleclan.chat` (default: true)

**Requirements:**
- Must be in a clan

**Example:**
```
/clan chat Hello everyone!
/klan chat Cześć wszystkim!
```

**Output:** (to clan members only)
```
§8[§6Clan§8] §6[Leader] §eSteve§7: §fHello everyone!
```

---

#### Toggle Clan Chat
```
/clan cc
/klan cc
```
Toggles automatic clan chat mode. When enabled, all messages are sent to clan chat.

**Permission:** `simpleclan.chat` (default: true)

**Requirements:**
- Must be in a clan

**Example:**
```
/clan cc
/klan cc
```

**Output:**
```
§8[§6SimpleClan§8] §aAuto clan chat enabled!
```

---

#### Change Tag Color
```
/clan color [color]
/klan kolor [kolor]
```
Changes the color of your clan's tag.

**Permission:** None required (Leader only)

**Requirements:**
- Must be clan leader
- Color must be from available colors list

**Available Colors:**
- `&6` - Gold (default)
- `&c` - Red
- `&a` - Green
- `&b` - Light Blue
- `&e` - Yellow
- `&d` - Pink
- `&9` - Blue
- `&5` - Purple
- `&f` - White
- `&7` - Gray

**Example:**
```
/clan color &c
/klan kolor &c
```

**Without argument shows preview:**
```
/clan color
```

**Output:**
```
§8[§6SimpleClan§8] §7Available colors:
§6Warriors§r, §cWarriors§r, §aWarriors§r, §bWarriors§r...
```

---

#### Toggle PvP
```
/clan pvp <on/off>
/klan pvp <on/off>
```
Enables or disables friendly fire between clan members.

**Permission:** None required (Leader only)

**Requirements:**
- Must be clan leader

**Arguments:**
- `on`, `true`, `tak`, `włącz` - Enable PvP
- `off`, `false`, `nie`, `wyłącz` - Disable PvP

**Example:**
```
/clan pvp off
/klan pvp off
```

**Output:** (to all clan members)
```
§8[§6SimpleClan§8] §cFriendly fire has been disabled!
```

---

#### Clan Ranking
```
/clan top
/clan ranking
/klan ranking
```
Shows the top clans ranked by kill count.

**Permission:** None required

**Requirements:** None

**Example:**
```
/clan top
/klan ranking
```

**Output:**
```
§8[§6SimpleClan§8] §6═══════ §lClan Ranking §6═══════
§e1. §6Warriors §7- §e42 kills
§e2. §6Dragons §7- §e35 kills
§e3. §6Knights §7- §e28 kills
§8[§6SimpleClan§8] §6════════════════════════════
```

---

#### Help
```
/clan help
/klan pomoc
```
Shows all available commands with descriptions.

**Permission:** None required

**Requirements:** None

**Example:**
```
/clan help
/klan pomoc
```

---

## Admin Commands

### Reload Configuration
```
/clan reload
/klan przeladuj
```
Reloads the plugin configuration and language files.

**Permission:** `simpleclan.admin` (default: op)

**Example:**
```
/clan reload
```

**Note:** This command is currently in development for future versions.

---

## Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `simpleclan.create` | Create clans | true |
| `simpleclan.invite` | Invite players | true |
| `simpleclan.chat` | Use clan chat | true |
| `simpleclan.admin` | Admin commands | op |

## Command Cooldowns

Currently, there are no cooldowns on commands. This may be added in a future update based on community feedback.

## Command Logging

All significant clan actions are logged to the server console:
- Clan creation
- Clan disbanding
- Member invitations
- Member joins/leaves
- Member kicks
- Rank changes

## Tips & Tricks

### Quick Commands
Use shorter aliases for faster commands:
- `/klan` instead of `/clan`
- `/clan cc` instead of `/clan clanchat`

### Tab Completion
All commands support tab completion. Press TAB while typing to see available options.

### Offline Players
The kick command works on offline players. Use the exact player name.

### Color Codes
When using color codes, always include the `&` prefix (e.g., `&6` not just `6`).

---

For more information, visit:
- [GitHub Repository](https://github.com/yourusername/SimpleClan)
- [Spigot Page](https://www.spigotmc.org/resources/simpleclans-pl.125629/)
- [PlaceholderAPI Documentation](docs/PLACEHOLDERS.md)
