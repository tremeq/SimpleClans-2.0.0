# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-10-02

### Added
- 🤝 Alliances: `/clan ally invite|accept|deny|cancel|break|list` (PL: `/klan sojusz zapros|akceptuj|odrzuc|anuluj|zerwij|lista`), ally limit, request expiry, friendly-fire protection between allies
- 💬 Alliance chat: `/clan ac [message]` (PL: `/klan sc`) - one-shot message or toggle mode
- 👑 Leadership transfer: `/clan leader <player>` with confirmation
- ✅ Disband confirmation: `/clan disband confirm` (PL: `/klan rozwiaz potwierdz`)
- ❌ Declining invitations: `/clan deny [clan]`, multiple invitations at once, invitation expiry
- 🛠️ Admin commands (also from console): `reload`, `disband`, `kick`, `add`, `setleader`, `resetstats`
- 📊 Player statistics (kills, deaths, KDR) stored in `players.yml`, `/clan stats [player]`
- 📊 Clan deaths and KDR, ranking by kills / KDR / members
- 🔌 New placeholders: `deaths`, `kdr`, `rank`, `online`, `role`, `tag_spaced`, `allies`, `allies_count`, `max_allies`, `chat_mode`, `player_kills`, `player_deaths`, `player_kdr`, `top_<n>_<field>`, `top_<type>_<n>_<field>`
- 🔒 `simpleclan.chat.color` and `simpleclan.alliance` permissions
- 🌍 Missing messages are added automatically to existing language files after an update

### Fixed
- Friendly fire protection now covers arrows, tridents, potions (splash & lingering), fire aspect / flame, TNT and tamed wolves
- Clan chat no longer bypasses mutes from other plugins
- Messages typed in clan chat mode no longer leak to public chat after being kicked / leaving
- Exact player name matching (no more prefix matches like `Ad` -> `Adam123`)
- Promote / demote work for offline members
- Correct error message for clan names with invalid characters
- `pvp.default-enabled` is now respected
- Leader can no longer accidentally become a deputy after leadership change
- Players can no longer use color codes in clan chat without permission
- Thread-safety of clan data used by async chat and PlaceholderAPI
- Data is saved asynchronously and atomically instead of on every kill on the main thread
- Kill farming protection (cooldown per killer/victim pair, no stats for clan/ally kills)
- Hardcoded texts (`Brak`, `Unknown`, `TOP 10`, Polish help in English) moved to language files

### Changed
- Clan leader must transfer leadership or disband the clan instead of leaving (no random successor)
- Kill broadcast is disabled by default
- Chat formats and role names moved from `config.yml` to language files

## [2.0.0] - 2025-11-04

### Added
- 💬 Clan chat system with dedicated chat command (`/clan chat`)
- 🔄 Auto clan chat toggle feature (`/clan cc`)
- ⚔️ PvP toggle system for friendly fire control
- 🎨 Tag color customization system with 10 available colors
- 📊 Clan ranking system based on kill statistics
- 🏆 Kill tracking and statistics for each clan
- 📢 Configurable kill broadcast on chat
- 👤 Deputy demotion feature
- 🚫 Ability to kick offline players
- 📅 Clan creation date tracking and display
- 🌍 Full multi-language system (Polish & English)
- 🔌 Extended PlaceholderAPI support (15 placeholders total)
- 📝 Comprehensive configuration options
- 🎯 Role-based chat formatting (Leader, Deputy, Member)

### Changed
- ⬆️ Updated to Minecraft 1.21+
- ⬆️ Updated to Java 21
- 🔧 Improved permission system
- 📚 Enhanced language file structure
- 🎨 Better color code handling
- 💾 Optimized data storage and loading

### Fixed
- 🐛 Fixed import issues in LangManager
- 🔒 Improved member management permissions
- 💬 Fixed chat message formatting
- 🎯 Better placeholder resolution

## [1.0.0] - 2025-06-01

### Added
- 🎉 Initial release
- 🏰 Basic clan creation and management
- 📨 Simple invitation system
- 👑 Leader and member hierarchy
- 📋 Member list display
- ℹ️ Clan information command
- 🚪 Leave and disband clan functionality
- 🔐 Basic permission system
- 🇵🇱 Polish language support
- 🔌 PlaceholderAPI integration (7 placeholders)

[2.1.0]: https://github.com/yourusername/SimpleClan/releases/tag/v2.1.0
[2.0.0]: https://github.com/yourusername/SimpleClan/releases/tag/v2.0.0
[1.0.0]: https://github.com/yourusername/SimpleClan/releases/tag/v1.0.0
