<p align="center">
  <img width="1003" alt="SimpleClans — a simple plugin for clans and placeholders" src="zdjecia-spigot/2343229f-cb56-4d11-a08d-6a7f9a0d751a.png" />
</p>

<p align="center">
  <img alt="Version 2.1.0" src="https://img.shields.io/badge/version-2.1.0-65c900" />
  <img alt="Minecraft 1.21.1" src="https://img.shields.io/badge/Minecraft-1.21.1-65c900" />
  <img alt="Java 21" src="https://img.shields.io/badge/Java-21-orange" />
  <a href="LICENSE"><img alt="License MIT" src="https://img.shields.io/badge/license-MIT-blue" /></a>
</p>

<p align="center">
  <a href="https://www.spigotmc.org/resources/simpleclans-pl.125629/">Spigot</a> ·
  <a href="#-commands">Commands</a> ·
  <a href="#-placeholderapi">Placeholders</a> ·
  <a href="https://github.com/tremeq/SimpleClans-2.0.0/issues">Report a bug</a>
</p>

## 📢 About SimpleClans

**Clans, alliances and PvP statistics in one plugin.** Create a clan, invite your friends, build alliances and compete in the rankings. Clan and alliance chats keep team conversations together, while friendly-fire settings control combat between teammates.

**Two languages included:** 🇵🇱 Polish (default) and 🇬🇧 English. Messages, help and command suggestions follow the configured language. Both Polish and English command aliases always work.

The plugin is called `SimpleClan` internally, so its data folder is `plugins/SimpleClan/`. This README describes **version 2.1.0**, including the changes since 2.0.0.

## 📦 Requirements & Dependencies

- **Server:** Spigot/Paper, targeting Minecraft **1.21.1**. Integration tests were run on Paper 1.21.1.
- **Java:** 21 or newer.
- **Optional:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for clan tags, statistics and rankings in other plugins. SimpleClan works without it.
- **Storage:** local YAML files; no database setup required.

---

<img width="870" alt="Features" src="zdjecia-spigot/Ciasno%20przycięty%20zielony%20baner%20FEATURES.png" />

### Core Features

- **Clan management** — create clans, invite players and manage a leader → deputy → member hierarchy. Defaults: 10 members, 2 deputies and names of 3–16 characters.
- **✨ NEW: Alliances** — send, accept, decline or cancel requests, break alliances and view allied clans. Defaults: 3 allies per clan and a 5-minute request timeout. Mutual requests form an alliance automatically.
- **Clan chat** — send a single message with `/clan chat`, or toggle clan chat with `/clan cc`.
- **✨ NEW: Alliance chat** — `/clan ac <message>` reaches your clan and its direct allies; `/clan ac` toggles alliance chat mode.
- **Friendly-fire control** — leaders can toggle PvP inside their clan. A separate setting controls damage between allies.
- **✨ IMPROVED: Combat protection** — covers melee, arrows, tridents, harmful splash and lingering potions, Fire Aspect/Flame, and damage from TNT or tamed animals when attributed to a player.
- **✨ NEW: Leadership transfer** — `/clan leader <player>` transfers ownership to a member after confirmation. Leaders must transfer ownership or disband before leaving.
- **✨ NEW: Disband confirmation** — `/clan disband` and `/clan admin disband` require a second command within a configurable timeout.
- **✨ NEW: Invitation controls** — decline invitations, choose between multiple invites and let old invitations expire. Player names are matched exactly.
- **✨ NEW: Player and clan statistics** — kills, PvP deaths and KDR, plus clan rankings by kills, KDR or member count. Repeated kills of the same player are limited by a configurable cooldown.
- **✨ NEW: Administration commands** — reload settings, remove clans, add or remove members, change leaders and reset statistics from the game or console.
- **Custom clan tags** — configurable tag format, 10 default colors and a color preview. Use PlaceholderAPI to display tags in compatible chat, tab or scoreboard plugins.
- **Offline member management** — promote, demote and kick existing members even when they are offline.
- **✨ IMPROVED: Persistent data** — clans and player statistics survive restarts; background saves and atomic file replacement reduce disk work on the server thread.
- **Configurable messages** — Polish and English language files include chat formats, role names and help. Missing message keys are added when a language is loaded after an update.

### How Statistics Work

Only kills by another player count. Deaths from mobs, falls or other environmental causes do not affect these statistics. Kills within the same clan or between allied clans do not count, even if friendly fire is enabled.

By default, the same killer/victim pair counts once every **300 seconds**. A kill rejected by this cooldown adds neither a kill nor a death. Individual statistics remain with the player when they change clans; clan statistics belong to the clan. KDR uses kills divided by deaths, or the number of kills when deaths are zero.

---

<img width="870" alt="Commands" src="zdjecia-spigot/Zielony%20baner%20COMMANDS%20w%20stylu%20Minecrafta.png" />

## 💬 Commands

Main aliases: `/clan`, `/klan`, `/klany`, `/clans`. `<argument>` is required; `[argument]` is optional. The table shows English commands and the matching Polish subcommand for `/klan`.

### Player Commands

| Command | Polish subcommand | Action |
| --- | --- | --- |
| `/clan create <name>` | `stworz` | Create a clan. |
| `/clan invite <player>` | `zapros` | Invite an online player; leader or deputy. |
| `/clan accept [clan]` | `akceptuj` | Accept an invitation; specify a clan if there are several. |
| `/clan deny [clan]` | `odrzuc` | Decline an invitation. |
| `/clan leave` | `opusc` | Leave your clan; leaders must transfer ownership or disband. |
| `/clan disband [confirm]` | `rozwiaz [potwierdz]` | Request and confirm deletion; leader only. |
| `/clan leader <player> [confirm]` | `lider <gracz> [potwierdz]` | Request and confirm leadership transfer; leader only. |
| `/clan list` | `lista` | List your clan's members and roles. |
| `/clan info [clan]` | `info` | Show members, allies, PvP settings and statistics. |
| `/clan promote <player>` | `zastepca` | Appoint a deputy; leader only. `/clan mod` also works. |
| `/clan demote <player>` | `degraduj` | Remove deputy status; leader only. |
| `/clan kick <player>` | `wyrzuc` | Remove a member; deputies cannot kick leaders or other deputies. |
| `/clan chat <message>` | `chat` | Send one message to your clan. |
| `/clan cc [message]` | `cc` | Toggle clan chat, or send a single message. |
| `/clan ac [message]` | `sc` | Toggle alliance chat, or send a single message. |
| `/clan color [color]` | `kolor` | Preview colors or change the tag color; leader only. |
| `/clan pvp <on/off>` | `pvp` | Toggle friendly fire inside your clan; leader only. |
| `/clan top [kills/kdr/members]` | `ranking [zabojstwa/kdr/czlonkowie]` | Show the clan ranking; defaults to kills. |
| `/clan stats [player]` | `staty` | Show individual kills, deaths, KDR and clan. |
| `/clan help` | `pomoc` | Show help in the configured language. |

To confirm an action, first run it without `confirm`, then repeat it with `confirm` (or `potwierdz`) within **30 seconds** by default. For example:

```text
/clan leader Steve
/clan leader Steve confirm
```

### Alliance Commands — New in 2.1.0

Use `/clan ally` or `/klan sojusz`. Managing alliances requires the clan leader and `simpleclan.alliance`; members with that permission can view the list.

| Command | Polish equivalent | Action |
| --- | --- | --- |
| `/clan ally invite <clan>` | `/klan sojusz zapros <klan>` | Send an alliance request. |
| `/clan ally accept [clan]` | `/klan sojusz akceptuj [klan]` | Accept a pending request. |
| `/clan ally deny [clan]` | `/klan sojusz odrzuc [klan]` | Decline a pending request. |
| `/clan ally cancel <clan>` | `/klan sojusz anuluj <klan>` | Cancel an outgoing request. |
| `/clan ally break <clan>` | `/klan sojusz zerwij <klan>` | End an alliance. |
| `/clan ally list` | `/klan sojusz lista` | Show allies and, for leaders/deputies, pending requests. |

Alliances are mutual and apply only between the two clans involved. Disbanding a clan also removes its alliances. Set `alliance.enabled: false` to disable alliance commands, alliance chat and allied friendly-fire protection.

### Admin Commands — New in 2.1.0

All commands below require `simpleclan.admin` and also work from the server console without the leading `/`.

| Command | Polish equivalent | Action |
| --- | --- | --- |
| `/clan admin reload` | `/klan admin przeladuj` | Reload configuration and the selected language. |
| `/clan admin disband <clan> [confirm]` | `/klan admin rozwiaz <klan> [potwierdz]` | Delete a clan after confirmation. |
| `/clan admin kick <player>` | `/klan admin wyrzuc <gracz>` | Remove a member; if the leader is removed, choose a successor or disband an empty clan. |
| `/clan admin add <clan> <player>` | `/klan admin dodaj <klan> <gracz>` | Add a known player without a clan, even above the member limit. |
| `/clan admin setleader <clan> <player>` | `/klan admin lider <klan> <gracz>` | Make an existing member the leader. |
| `/clan admin resetstats clan <clan>` | `/klan admin resetstaty klan <klan>` | Reset clan kills and deaths. |
| `/clan admin resetstats player <player>` | `/klan admin resetstaty gracz <gracz>` | Reset individual kills and deaths. |

The console can also use `/clan info <clan>`, `/clan top`, `/clan stats <player>` and `/clan help`.

## 🔑 Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `simpleclan.create` | Create a clan. | Everyone |
| `simpleclan.invite` | Invite players, subject to clan role checks. | Everyone |
| `simpleclan.chat` | Use clan and alliance chat. | Everyone |
| `simpleclan.chat.color` | Use `&` and `&#RRGGBB` colors in clan/alliance messages. | Operators |
| `simpleclan.alliance` | Access alliance commands, subject to clan role checks. | Everyone |
| `simpleclan.admin` | Use administrative commands. | Operators |

Permissions do not replace clan roles: a member with `simpleclan.invite` still needs to be a leader or deputy to invite someone.

---

## 🔌 PlaceholderAPI

The expansion is included in SimpleClan and registers automatically when PlaceholderAPI is installed. No separate eCloud expansion is needed. Use these placeholders in a compatible chat, tab, scoreboard or hologram plugin.

### Clan & Player Placeholders

| Placeholder | Value |
| --- | --- |
| `%simpleclan_tag%` | Colored clan tag; empty without a clan. |
| `%simpleclan_tag_spaced%` | Colored tag followed by a space; empty without a clan. |
| `%simpleclan_name%` | Clan name. |
| `%simpleclan_leader%` | Leader's name. |
| `%simpleclan_members%` | Member count. |
| `%simpleclan_max_members%` | Configured member limit. |
| `%simpleclan_online%` | Online member count. |
| `%simpleclan_has_clan%` | Whether the player belongs to a clan. |
| `%simpleclan_is_leader%` | Whether the player is the leader. |
| `%simpleclan_is_moderator%` | Whether the player is a deputy. |
| `%simpleclan_is_leader_or_mod%` | Whether the player is a leader or deputy. |
| `%simpleclan_moderators_count%` | Deputy count. |
| `%simpleclan_role%` | Role text from the selected language file. |
| `%simpleclan_created%` | Clan creation date. |
| `%simpleclan_tag_color%` | Clan tag color code. |
| `%simpleclan_pvp_enabled%` | Whether clan friendly fire is enabled. |
| `%simpleclan_kills%` | Clan kills. |
| `%simpleclan_deaths%` | Clan PvP deaths. |
| `%simpleclan_kdr%` | Clan KDR, formatted to two decimal places. |
| `%simpleclan_rank%` | Clan position in the kills ranking. |
| `%simpleclan_allies%` | Allied clan names, separated by commas. |
| `%simpleclan_allies_count%` | Ally count. |
| `%simpleclan_max_allies%` | Configured ally limit; `∞` when unlimited. |
| `%simpleclan_clan_chat%` | Whether clan chat mode is enabled. |
| `%simpleclan_chat_mode%` | `public`, `clan` or `ally`. |
| `%simpleclan_player_kills%` | Individual kills, including players without a clan. |
| `%simpleclan_player_deaths%` | Individual PvP deaths. |
| `%simpleclan_player_kdr%` | Individual KDR, formatted to two decimal places. |

### Ranking Placeholders — New in 2.1.0

```text
%simpleclan_top_<position>_<field>%
%simpleclan_top_<type>_<position>_<field>%
```

- **Type:** `kills` (default), `kdr` or `members`.
- **Position:** starts at `1`.
- **Field:** `name`, `tag`, `leader`, `kills`, `deaths`, `kdr` or `members`.

Examples: `%simpleclan_top_1_name%`, `%simpleclan_top_kdr_1_kdr%`, `%simpleclan_top_members_3_tag%`. Ranking placeholders do not require a player context, making them suitable for holograms. Missing positions return `-` for text, `0` for counts and `0.00` for KDR.

---

## 📥 Installation & Configuration

1. Obtain the plugin JAR from [Spigot](https://www.spigotmc.org/resources/simpleclans-pl.125629/) or build this source version using the instructions below.
2. Place the JAR in your server's `plugins/` folder. Add PlaceholderAPI if you want to use placeholders.
3. Start the server to generate `plugins/SimpleClan/`.
4. Edit `config.yml` and the selected file in `lang/`, then run `/clan admin reload`.

### Main Settings

These are the defaults for a fresh installation. The complete configuration, including available tag colors, is in [config.yml](src/main/resources/config.yml).

```yaml
language: "pl" # pl or en

clan:
  max-members: 10
  max-name-length: 16
  min-name-length: 3
  max-moderators: 2
  invite-expire-seconds: 120
  tag-format: "{color}[{clan}]&r"

chat:
  log-to-console: true

pvp:
  default-enabled: false
  message-cooldown-seconds: 3

alliance:
  enabled: true
  max-allies: 3 # -1 = unlimited
  friendly-fire: false
  request-expire-seconds: 300

stats:
  kill-cooldown-seconds: 300 # 0 = no cooldown

ranking:
  top-clans: 10
  broadcast-kills: false

confirmation-timeout-seconds: 30

storage:
  autosave-interval-seconds: 60
```

Messages, help, chat formats and role names live in [lang/pl.yml](src/main/resources/lang/pl.yml) and [lang/en.yml](src/main/resources/lang/en.yml). Change `language` to `en` for English output and command suggestions. Reloading updates settings and language, but does not reload clan/player data from disk.

### Updating from 2.0.0

1. Stop the server and back up `plugins/SimpleClan/`.
2. Replace the old SimpleClan JAR with `SimpleClan-2.1.0.jar`, leaving only one version installed.
3. Start the server. Existing clans are loaded from `clans.yml`; missing configuration values and missing keys in the selected language file are added automatically.
4. Review chat formatting in `lang/pl.yml` or `lang/en.yml`. The old `chat.format`, `chat.leader-role` and `chat.moderator-role` settings in `config.yml` are no longer used; transfer any custom formats to the language file.

Existing settings and translations are kept. For example, an existing `ranking.broadcast-kills: true` remains enabled, even though the new-install default is `false`. Existing message layouts are not replaced with the new layouts; compare them with the bundled language files if you want the additional fields.

Clan data remains in `clans.yml`; individual statistics are stored separately in `players.yml`. New player statistics start at zero rather than being reconstructed from old clan totals.

---

## 🔧 Recent Updates & Bug Fixes

### v2.1.0 — Current Source Version

**New features**

- ✅ Alliances, request expiry, configurable limits and alliance chat.
- ✅ Leadership transfer, disband confirmation and invitation rejection.
- ✅ Administrative commands usable from both the game and console.
- ✅ Player statistics, clan deaths/KDR and rankings by kills, KDR or members.
- ✅ Additional clan/player placeholders and configurable ranking placeholders.

**Fixes & improvements**

- ✅ Broader friendly-fire protection for projectiles, potions, fire and player-attributed indirect damage.
- ✅ Exact player-name matching and offline promotion/demotion.
- ✅ Leadership changes remove the new leader's deputy role.
- ✅ New clans respect `pvp.default-enabled`.
- ✅ Clan chat mode respects cancelled chat events from other plugins; message colors require permission.
- ✅ Kill farming protection excludes repeated, same-clan and allied kills from statistics.
- ✅ Background and atomic saves, with pending writes completed during shutdown.
- ✅ Missing language keys are added automatically; help and command suggestions support Polish and English.

See [CHANGELOG.md](CHANGELOG.md) for the full version history.

## 🛠️ Building from Source

Requires JDK 21+ and Maven.

```bash
git clone https://github.com/tremeq/SimpleClans-2.0.0.git
cd SimpleClans-2.0.0
mvn clean package
```

For the 2.1.0 source version, the compiled plugin is `target/SimpleClan-2.1.0.jar`.

## 🐛 Support & License

Report bugs or request features through [GitHub Issues](https://github.com/tremeq/SimpleClans-2.0.0/issues). Include the plugin version, server version, steps to reproduce and relevant console errors.

Created by **[TremeQ](https://github.com/tremeq)**. Licensed under the [MIT License](LICENSE).
