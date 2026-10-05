# 📺 NOHA Player

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-24%2B-blue.svg)](https://developer.android.com/about/dashboards)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> A lightweight, modern IPTV player for Android built with Jetpack Compose and Hilt. Stream your own playlists with style.

---

## ✨ Features

### 🎬 Playback
- **Media3 ExoPlayer** with HLS support and retry-friendly error handling
- Internal / External player toggle
- Auto-play last channel on app start
- Background playback support

### 📂 Playlist Management
- Load playlists via URL, Xtream Codes, or local file
- Recent playlists history
- Active playlist selector

### 🔍 Navigation & Discovery
- **Tabbed interface:** Favorites | All Channels | Categories
- Search channels (All tab)
- Category dropdown filter by group/country
- Category counts display
- Recently played channels list

### 🔒 Parental Controls & Privacy
- PIN-based parental lock/unlock
- Hide/unhide broken or unwanted channels
- No built-in channels — you control your content

### ⚙️ Settings & Customization
- Start on device boot (opt-in)
- Theme support with background images
- NOHA Player branding
- Disclaimer and privacy policy integration

### 📺 Cast Support
- Chromecast button stub (cast implementation ready)

---

## 📸 Screenshots

*Coming soon — add your screenshots here*

| Player | Channels | Settings |
|--------|----------|----------|
| ![Player](screenshots/player.png) | ![Channels](screenshots/channels.png) | ![Settings](screenshots/settings.png) |

---

## 🛠️ Requirements

- **Android Studio:** Hedgehog or newer
- **JDK:** 17 or higher
- **Android SDK:** API 24+ (Android 7.0)
- **Target SDK:** 34

---

## 🚀 Installation & Build

### Clone the repository
```bash
git clone https://github.com/Bonythomasv/nohaplayer.git
cd nohaplayer
```

### Build debug APK
```bash
./gradlew assembleDebug
```

### Or use Android Studio
1. Open project in Android Studio
2. Sync project with Gradle files
3. Run on device or emulator

> 💡 **Tip:** To test boot/start and autoplay features, enable them in the Settings dialog inside the app.

---

## 📱 Play Store Preparation

- [ ] Set app name and icon in Play Console
- [ ] Add screenshots (phone + tablet)
- [ ] Write compelling app description
- [ ] Provide privacy policy URL (see `PRIVACY_POLICY.md`)
- [ ] Generate signed AAB:
  ```
  Build > Generate Signed Bundle / APK > Android App Bundle
  ```
- [ ] Enable Play App Signing
- [ ] Add content ratings
- [ ] Declare ads status (none currently)
- [ ] Verify target SDK 34 compliance
- [ ] Roll out to internal testing → production

---

## 🗺️ Roadmap

### In Progress
- [ ] Playback resilience (1-3 retries for failed channels)
- [ ] EPG support (XMLTV/JTV)
- [ ] Recording support assessment

### Planned
- [ ] Enhanced channel categorization by country
- [ ] Improved search within categories
- [ ] Full Chromecast integration
- [ ] Android TV / Fire TV support
- [ ] Picture-in-Picture mode

See [`todo.md`](todo.md) for detailed task tracking.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## ⚖️ Legal

**No built-in channels provided.** This app is a player only — users must supply their own playlist URLs or files. Ensure you have the rights to any content you stream.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ using Jetpack Compose
</p>
