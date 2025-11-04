# SimpleClan

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spigot](https://img.shields.io/badge/Spigot-Download-yellow.svg)](https://www.spigotmc.org/resources/simpleclans-pl.125629/)

> Advanced clan system with chat, PvP toggle, ranking and many more features!

## 🌍 Multi-Language Support

SimpleClan is fully translated and supports:
- 🇵🇱 **Polish (Polski)** - Complete translation, default language
- 🇬🇧 **English** - Full English support

All messages, commands, and descriptions are available in both languages!

## ✨ Features

### 🏰 Clan System
- Create and manage clans
- Invitation system for new members
- Hierarchy: Leader → Deputy (max 2) → Member
- Configurable member limit (default: 10)
- Configurable clan name length (3-16 characters)
- Clan disbanding by leader

### 💬 Clan Chat
- Dedicated chat for clan members (`/clan chat`)
- Auto clan chat toggle (`/clan cc`)
- Different colors for roles (Leader, Deputy, Member)
- Fully configurable message format
- Private communication between clan members

### ⚔️ PvP System
- Enable/disable friendly fire in clan
- Protection against clan member attacks (when disabled)
- Only leader can change PvP settings
- Member notification on status change
- Per-clan PvP configuration

### 🎨 Customization
- 10 available clan tag colors
- Format: **[ClanName]**
- Color preview before selection
- Only leader can change color
- Fully customizable tag format

### 📊 Ranking & Statistics
- Clan ranking by kills
- Kill tracking for each clan
- Configurable kill broadcast on chat
- Top 10 clans display (configurable)
- Real-time statistics updates

### 👥 Member Management
- Promote/demote deputies
- Kick members (even offline players)
- Member list with roles
- Detailed clan information
- Clan creation date tracking

## 📥 Installation

1. Download the latest `.jar` file from [Spigot](https://www.spigotmc.org/resources/simpleclans-pl.125629/)
2. Place it in your server's `plugins/` folder
3. (Optional) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder support
4. Start/restart your server
5. Configure the plugin in `plugins/SimpleClan/config.yml`
6. Set your preferred language (pl or en)
7. Customize messages in `plugins/SimpleClan/lang/` folder

## 📝 Commands

### English Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/clan create <name>` | Create a new clan | `simpleclan.create` |
| `/clan invite <player>` | Invite player to clan | `simpleclan.invite` |
| `/clan accept` | Accept invitation | - |
| `/clan leave` | Leave clan | - |
| `/clan disband` | Disband clan (leader only) | - |
| `/clan list` | List clan members | - |
| `/clan info [clan]` | Show clan information | - |
| `/clan mod <player>` | Promote to deputy (leader only) | - |
| `/clan demote <player>` | Demote deputy (leader only) | - |
| `/clan kick <player>` | Kick member | - |
| `/clan chat <message>` | Send message to clan chat | `simpleclan.chat` |
| `/clan cc` | Toggle auto clan chat | `simpleclan.chat` |
| `/clan color [color]` | Change tag color (leader only) | - |
| `/clan pvp <on/off>` | Toggle clan PvP (leader only) | - |
| `/clan top` | Show clan ranking | - |
| `/clan help` | Show help | - |

### Polish Commands
All commands also support Polish aliases:
- `/klan stworz <nazwa>` - Create clan
- `/klan zapros <gracz>` - Invite player
- `/klan akceptuj` - Accept invitation
- `/klan opusc` - Leave clan
- `/klan rozwiaz` - Disband clan
- `/klan lista` - List members
- `/klan info [klan]` - Clan info
- `/klan zastepca <gracz>` - Promote deputy
- `/klan degraduj <gracz>` - Demote deputy
- `/klan wyrzuc <gracz>` - Kick member
- `/klan chat <wiadomość>` - Clan chat
- `/klan cc` - Toggle clan chat
- `/klan kolor [kolor]` - Change color
- `/klan pvp <on/off>` - Toggle PvP
- `/klan ranking` - Show ranking
- `/klan pomoc` - Show help

**Command Aliases:** `/clan`, `/klan`, `/klany`, `/clans`

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `simpleclan.create` | Create clans | true |
| `simpleclan.invite` | Invite to clan | true |
| `simpleclan.chat` | Use clan chat | true |
| `simpleclan.admin` | Clan administration | op |

## 🔌 PlaceholderAPI

### Available Placeholders (15)

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%simpleclan_tag%` | Clan tag with color | `[Warriors]` |
| `%simpleclan_name%` | Clan name | `Warriors` |
| `%simpleclan_leader%` | Leader's nickname | `Steve` |
| `%simpleclan_members%` | Number of members | `5` |
| `%simpleclan_max_members%` | Maximum members | `10` |
| `%simpleclan_is_leader%` | Is player leader | `true`/`false` |
| `%simpleclan_is_moderator%` | Is player deputy | `true`/`false` |
| `%simpleclan_is_leader_or_mod%` | Is leader or deputy | `true`/`false` |
| `%simpleclan_moderators_count%` | Number of deputies | `2` |
| `%simpleclan_has_clan%` | Has clan | `true`/`false` |
| `%simpleclan_created%` | Creation date | `01.01.2025 12:00` |
| `%simpleclan_tag_color%` | Tag color code | `&6` |
| `%simpleclan_pvp_enabled%` | PvP status | `true`/`false` |
| `%simpleclan_kills%` | Clan kill count | `42` |
| `%simpleclan_clan_chat%` | Auto clan chat status | `true`/`false` |

## ⚙️ Configuration

### config.yml

```yaml
# Plugin language (pl, en)
language: "en"

# Clan settings
clan:
  max-members: 10
  max-name-length: 16
  min-name-length: 3
  max-moderators: 2
  tag-format: "{color}[{clan}]&r"

  # Available tag colors
  available-colors:
    - "&6" # Gold (default)
    - "&c" # Red
    - "&a" # Green
    - "&b" # Light Blue
    - "&e" # Yellow
    - "&d" # Pink
    - "&9" # Blue
    - "&5" # Purple
    - "&f" # White
    - "&7" # Gray

# Clan chat settings
chat:
  # Message format
  # {role} - role in clan (leader/deputy/empty)
  # {player} - player nickname
  # {message} - message content
  format: "&8[&6Clan&8] &r{role}&e{player}&7: &f{message}"

  # Leader role format
  leader-role: "&6[Leader] "

  # Deputy role format
  moderator-role: "&a[Deputy] "

# PvP settings
pvp:
  # Default PvP status for new clans
  default-enabled: false

# Ranking settings
ranking:
  # Number of clans to display
  top-clans: 10

  # Broadcast kills
  broadcast-kills: true
```

### Language Files

Language files are located in `plugins/SimpleClan/lang/`:
- `pl.yml` - Polish translations
- `en.yml` - English translations

All messages can be customized in these files.

## 💡 Usage Examples

### Creating and Managing Clan
```
/clan create Warriors          → Create clan "Warriors"
/clan invite Steve             → Invite Steve to your clan
/clan mod Steve                → Promote Steve to deputy
/clan color &c                 → Change tag color to red
/clan pvp off                  → Disable friendly fire
```

### Communication
```
/clan chat Hello everyone!     → Send message to clan chat
/clan cc                       → Toggle auto clan chat mode
```

### Statistics
```
/clan info                     → Show your clan info
/clan info Warriors            → Show Warriors clan info
/clan top                      → Show top 10 clans by kills
```

## 🎯 Perfect For

- 🇵🇱 **Polish servers** - Native Polish language support
- 🌍 **International servers** - Full English translation
- ⚔️ PvP servers with team mechanics
- 🏰 Survival servers with community features
- 💬 Servers needing organized team communication
- 📊 Competitive servers with ranking systems

## ⚠️ Requirements

- **Minecraft:** 1.21 or newer
- **Java:** 21 or newer
- **Server:** Spigot/Paper
- **PlaceholderAPI:** Optional (for placeholders)

## 🔧 Building from Source

```bash
git clone https://github.com/yourusername/SimpleClan.git
cd SimpleClan
mvn clean package
```

The compiled `.jar` file will be in the `target/` directory.

## 📜 Changelog

### Version 2.0.0
- ✨ Added clan chat system with toggle
- ✨ Added PvP toggle between clan members
- ✨ Added tag color system (10 colors)
- ✨ Added clan ranking by kills
- ✨ Added kill statistics tracking
- ✨ Extended PlaceholderAPI support (15 placeholders)
- ✨ Added deputy demotion feature
- ✨ Added offline player kick support
- ✨ Added clan creation date tracking
- 🌍 Full multi-language system (Polish & English)
- 🔧 Updated to Minecraft 1.21+
- 🔧 Improved permission system
- 🔧 Enhanced configuration options

### Version 1.0.0
- 🎉 Initial release
- Basic clan management
- Simple invitation system
- Leader and member hierarchy

## 🐛 Bug Reports & Support

Found a bug? Need help?
- Open an issue on [GitHub](https://github.com/yourusername/SimpleClan/issues)
- Visit [Spigot Discussion](https://www.spigotmc.org/resources/simpleclans-pl.125629/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**TremeQ**

## 🙏 Acknowledgments

- Thanks to all contributors
- Inspired by classic clan plugins
- Built with ❤️ for the Minecraft community

---

<div align="center">

### 🌟 If you enjoy this plugin, please star this repository! ⭐

**[Download](https://www.spigotmc.org/resources/simpleclans-pl.125629/)** • **[Report Bug](https://github.com/tremeq/SimpleClan/issues)** • **[Request Feature](https://github.com/tremeq/SimpleClan/issues)**

🇵🇱 Fully translated for Polish community | 🇬🇧 Complete English support

</div>

