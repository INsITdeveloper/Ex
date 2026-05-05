# 🎬 InsDrama - Android Drama Streaming App

Aplikasi streaming drama Indonesia dengan UI modern seperti TikTok.

## 🚀 Features

- ✅ TikTok-style vertical scroll navigation
- ✅ Search & filter by genre
- ✅ Auto-play next episode
- ✅ Swipe up/down to change drama
- ✅ Modern Material Design 3 UI
- ✅ Responsive & smooth animations
- ✅ Episode list with quick navigation
- ✅ Drama detail page
- ✅ Offline cover caching
- ✅ **DOWNLOAD episodes for offline viewing** 🆕
- ✅ **Download progress indicator** 🆕
- ✅ **Manage downloaded episodes** 🆕
- ✅ **Auto-detect offline/online mode** 🆕

## 📱 Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1+) or later
- JDK 17+
- Android SDK 34+

### Steps

1. **Open Project**
   ```
   Open Android Studio → File → Open → Select insdrama-android folder
   ```

2. **Sync Gradle**
   ```
   File → Sync Project with Gradle Files
   ```

3. **Build APK**
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK (signed)
   ./gradlew assembleRelease
   ```

4. **APK Location**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   app/build/outputs/apk/release/app-release.apk
   ```

## 🎨 App Info

- **Name:** InsDrama
- **Package:** com.ins.insdrama
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Logo:** http://cdnins.insjay.biz.id/logo.svg
- **API:** https://raw.githubusercontent.com/INsITdeveloper/Drama-extension/main/datadrama.json

## 📂 Project Structure

```
app/src/main/
├── java/com/ins/insdrama/
│   ├── MainActivity.kt
│   ├── adapter/
│   │   ├── DramaAdapter.kt
│   │   └── EpisodeAdapter.kt
│   ├── model/
│   │   ├── Drama.kt
│   │   └── Episode.kt
│   ├── api/
│   │   └── ApiClient.kt
│   └── ui/
│       ├── HomeFragment.kt
│       ├── DetailFragment.kt
│       └── PlayerFragment.kt
├── res/
│   ├── layout/
│   ├── drawable/
│   ├── values/
│   └── navigation/
└── AndroidManifest.xml
```

## 🎯 Features Detail

### Home Screen
- Vertical scroll like TikTok
- Cover image with title overlay
- Genre badges
- Play button
- Search icon

### Detail Screen
- Drama info (title, description, genres)
- Episode list
- Related dramas

### Player Screen
- Full-screen video player
- Next/Previous episode buttons
- Genre filter while watching
- Auto-play next episode

## 📄 License

MIT License - Created for INs IT developer
