# NOHA Player

Lightweight IPTV player built with Jetpack Compose and Hilt. Supports HLS, favorites, playlists, categories, recents, parental PIN, start-on-boot, autoplay last, internal/external player toggle, cast button stub, and hide/remove broken channels. DataStore persists favorites, playlists, settings, and hidden items.

## What’s inside
- Media3 ExoPlayer (+ HLS) with retry-friendly playback handling
- Playlist manager (URL, Xtream, file), active playlist selector, recent playlists
- Tabs: Favorites / All / Categories, with All-tab search and category dropdown filter
- Category counts by group/country; recents list; favorites toggle
- Parental lock/unlock, start on boot, autoplay last channel, hide/unhide channels
- Disclaimer + privacy-policy stub; background image + NOHA Player branding

## Build & run
1) Android Studio (Hedgehog+), JDK 17, Android SDK 24+ / target 34  
2) `./gradlew assembleDebug` or use Android Studio Run on device/emulator  
3) To test boot/start and autoplay, enable in Settings dialog inside the app

## Play Store prep (short checklist)
- Set app name, icon, screenshots, and privacy policy URL in Play Console
- Generate signed AAB: `Build > Generate Signed Bundle / APK > Android App Bundle`
- Use Play App Signing; keep keystore/credentials safe
- Upload AAB, add content ratings, ads declaration (if any), target SDK 34, and roll out

## Legal
No built-in channels. User-supplied playlists only. Ensure you have rights to any content you stream.

## License
See LICENSE.

