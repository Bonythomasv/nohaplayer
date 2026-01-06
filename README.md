# NohaPlayer

A modern Android IPTV streaming application that allows you to stream thousands of free channels from publicly available IPTV playlists.

## Features

- 📺 Stream thousands of free IPTV channels
- 🎨 Modern Material Design 3 UI built with Jetpack Compose
- 🔄 Automatic playlist fetching from iptv-org
- 🎬 Full-screen video playback with ExoPlayer
- 📱 Optimized for Android phones and tablets
- ⚡ Fast channel loading with efficient M3U parsing

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with StateFlow
- **Dependency Injection**: Hilt
- **Media Player**: ExoPlayer (Media3)
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil

## Project Structure

```
app/
├── data/
│   ├── model/          # Channel, Playlist data classes
│   ├── parser/         # M3U playlist parser
│   └── repository/     # Data repository and network service
├── domain/
│   └── usecase/        # Business logic (future)
├── ui/
│   ├── channel/        # Channel list screen
│   ├── player/         # Video player screen
│   └── theme/          # App theming
└── di/                 # Dependency injection modules
```

## Setup Instructions

1. **Clone or open the project** in Android Studio (Hedgehog or later)

2. **Sync Gradle** - Android Studio will automatically download dependencies

3. **Build the project** - Click "Build > Make Project" or press `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (Mac)

4. **Run the app** - Connect an Android device or start an emulator, then click "Run"

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24 (Android 7.0) or higher
- Target SDK 34 (Android 14)
- Java 17 or higher

## How It Works

1. The app fetches the M3U playlist from `https://iptv-org.github.io/iptv/index.m3u`
2. The M3U parser extracts channel information (name, logo, stream URL, etc.)
3. Channels are displayed in a scrollable list
4. Tapping a channel opens the video player
5. ExoPlayer handles streaming and playback

## Building for Release

1. Generate a signed APK or AAB:
   - Build > Generate Signed Bundle / APK
   - Follow the signing wizard

2. For Play Store distribution:
   - Generate an AAB (Android App Bundle)
   - Upload to Google Play Console
   - Ensure you comply with Play Store policies

## Future Enhancements

- EPG (Electronic Program Guide) integration
- Favorites/bookmarks
- Search functionality
- Channel categories/filtering
- Custom playlist URLs
- Chromecast support
- Picture-in-picture mode
- Background playback

## Legal Notice

This app provides access to publicly available IPTV streams. The app itself does not host any content. All streams are sourced from publicly available playlists. Users are responsible for ensuring they have the right to access the content they stream.

## License

This project is open source. See LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

