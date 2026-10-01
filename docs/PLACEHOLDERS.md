# SimpleClan Placeholders

This document describes all available PlaceholderAPI placeholders for SimpleClan.

## Requirements

To use these placeholders, you need to have [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed on your server.

## Available Placeholders

### Basic Information

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_name%` | Returns the player's clan name | `Warriors` |
| `%simpleclan_tag%` | Returns formatted clan tag with color | `§6[Warriors]§r` |
| `%simpleclan_tag_color%` | Returns the color code of the clan tag | `&6` |

### Leadership & Roles

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_leader%` | Returns the clan leader's name | `Steve` |
| `%simpleclan_is_leader%` | Returns if player is leader | `true` or `false` |
| `%simpleclan_is_moderator%` | Returns if player is deputy | `true` or `false` |
| `%simpleclan_is_leader_or_mod%` | Returns if player is leader or deputy | `true` or `false` |

### Member Statistics

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_members%` | Returns number of clan members | `5` |
| `%simpleclan_max_members%` | Returns maximum allowed members | `10` |
| `%simpleclan_moderators_count%` | Returns number of deputies | `2` |

### Clan Status

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_has_clan%` | Returns if player is in a clan | `true` or `false` |
| `%simpleclan_created%` | Returns clan creation date | `01.01.2025 12:00` |
| `%simpleclan_pvp_enabled%` | Returns if clan PvP is enabled | `true` or `false` |

### Combat Statistics

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_kills%` | Returns total clan kills | `42` |

### Chat System

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_clan_chat%` | Returns if auto clan chat is enabled | `true` or `false` |

## Usage Examples

### Chat Format with Placeholders

Add clan tags to your chat plugin (e.g., EssentialsChat):

```yaml
# EssentialsChat format
format: '%simpleclan_tag% &7{DISPLAYNAME}&r: {MESSAGE}'
```

Output: `§6[Warriors]§r §7Steve: Hello!`

### TAB Plugin Integration

Display clan information in TAB:

```yaml
# TAB plugin configuration
tabprefix: '%simpleclan_tag% '
tabsuffix: ' &7[%simpleclan_members%/%simpleclan_max_members%]'
```

### Scoreboard (Sidebar)

```yaml
# Scoreboard line examples
- '&6Clan: &f%simpleclan_name%'
- '&6Members: &f%simpleclan_members%/%simpleclan_max_members%'
- '&6Kills: &f%simpleclan_kills%'
- '&6Leader: &f%simpleclan_leader%'
```

### Conditional Placeholders

Using with PlaceholderAPI's conditional placeholders:

```yaml
# Show clan tag only if player has clan
%simpleclan_has_clan_true%{simpleclan_tag}%simpleclan_has_clan_false%&7[No Clan]
```

### LuckPerms Meta

Set prefix based on clan leadership:

```
/lp user Steve meta setprefix 100 "%simpleclan_tag% &6[Leader] "
```

### FeatherBoard Integration

```yaml
# FeatherBoard scoreboard
lines:
  clan:
    text:
    - '&6&lYour Clan'
    - '&fName: %simpleclan_name%'
    - '&fMembers: %simpleclan_members%/%simpleclan_max_members%'
    - '&fKills: %simpleclan_kills%'
    - '&fRole: {simpleclan_is_leader_true}&6Leader{simpleclan_is_leader_false}{simpleclan_is_moderator_true}&aDeputy{simpleclan_is_moderator_false}&7Member'
```

## Advanced Usage

### Custom MOTD with PAPI

Using PlaceholderAPI in server MOTD:

```yaml
# server.properties or plugin that supports PAPI in MOTD
motd: Welcome %player_name%! Clan: %simpleclan_name%
```

### DeluxeMenus Integration

Create a clan menu with dynamic information:

```yaml
gui_menus:
  clan_menu:
    menu_title: '&6Your Clan: %simpleclan_name%'
    size: 27
    items:
      info:
        material: PAPER
        slot: 13
        display_name: '&6Clan Information'
        lore:
        - '&fLeader: %simpleclan_leader%'
        - '&fMembers: %simpleclan_members%/%simpleclan_max_members%'
        - '&fKills: %simpleclan_kills%'
        - '&fPvP: %simpleclan_pvp_enabled%'
```

### BossBar with Clan Info

Using with plugins that support PAPI in BossBar:

```yaml
title: '&6Clan %simpleclan_name% &7- &fMembers: %simpleclan_members%/%simpleclan_max_members%'
```

## Placeholder Conditions

You can create conditions based on placeholder values:

### Example 1: Show different text based on role
```
{simpleclan_is_leader_true}&6★ LEADER{simpleclan_is_leader_false}{simpleclan_is_moderator_true}&aDeputy{simpleclan_is_moderator_false}&7Member
```

### Example 2: Show clan status
```
{simpleclan_has_clan_true}Member of %simpleclan_name%{simpleclan_has_clan_false}&cNo Clan
```

## Troubleshooting

### Placeholder shows as text

1. Ensure PlaceholderAPI is installed
2. Verify SimpleClan is loaded after PlaceholderAPI
3. Run `/papi parse me %simpleclan_name%` to test
4. Check console for errors

### Placeholder returns empty

1. Player might not be in a clan (check with `%simpleclan_has_clan%`)
2. Clan data might not be loaded yet (wait after join)
3. Check if SimpleClan is properly registered with PAPI using `/papi list`

### Colors not working

1. Ensure your chat plugin supports color codes
2. Use `&` for color codes in configs
3. Some plugins require special permission for colors

## Update Information

Last updated: 2025-11-04
SimpleClan Version: 2.0.0
Total Placeholders: 15

For more information, visit the [SimpleClan Wiki](https://github.com/yourusername/SimpleClan/wiki) or [Spigot Page](https://www.spigotmc.org/resources/simpleclans-pl.125629/).
