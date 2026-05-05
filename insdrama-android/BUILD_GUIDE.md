# 🎬 InsDrama - Build Guide

## 📋 Prerequisites

### Install Android Studio
1. Download from: https://developer.android.com/studio
2. Install Android Studio Hedgehog (2023.1.1) or newer
3. During installation, ensure:
   - Android SDK Platform 34 (Android 14)
   - Android SDK Build-Tools 34.0.0
   - Android Emulator (optional)

### Install JDK 17
```bash
# Windows: Download from https://adoptium.net/
# macOS: brew install openjdk@17
# Linux: sudo apt install openjdk-17-jdk
```

## 🚀 Quick Build

### Using Android Studio (Recommended)

1. **Open Project**
   - Launch Android Studio
   - Click "Open" → Navigate to `insdrama-android` folder
   - Click "OK"

2. **Sync Gradle**
   - Wait for Gradle sync to complete
   - If errors appear: File → Sync Project with Gradle Files

3. **Build Debug APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - APK location: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release APK** (for publishing)
   - Create signing key: Build → Generate Signed Bundle / APK
   - Select "APK" → Create new key store
   - Follow wizard to create signed APK

### Using Command Line

```bash
# Navigate to project directory
cd insdrama-android

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Gradle Sync Failed
```
# Solution:
File → Invalidate Caches / Restart → Invalidate and Restart
```

#### 2. SDK Not Found
```
# Solution:
Tools → SDK Manager → Install Android SDK Platform 34
```

#### 3. JDK Version Mismatch
```
# Solution:
File → Project Structure → SDK Location → Select JDK 17
```

#### 4. Network Error (API not reachable)
```
# Solution:
Check internet connection
Ensure AndroidManifest.xml has INTERNET permission
```

## 📱 Testing

### Using Android Emulator
1. Tools → AVD Manager → Create Virtual Device
2. Select device (e.g., Pixel 6)
3. Select system image (API 34 recommended)
4. Finish → Start emulator
5. Run app: Click green play button

### Using Physical Device
1. Enable Developer Options on phone (Settings → About → Tap Build Number 7 times)
2. Enable USB Debugging (Settings → Developer Options)
3. Connect phone via USB
4. Run app: Select your device from dropdown

## 📦 APK Locations

After build:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## 🎨 Customization

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">YourApp</string>
```

### Change Package Name
Edit `app/build.gradle.kts`:
```kotlin
applicationId = "com.yourpackage.app"
```

### Change App Icon
1. Replace files in `app/src/main/res/mipmap-*/ic_launcher.png`
2. Or use Image Asset Studio: Right-click res → New → Image Asset

### Change Colors
Edit `app/src/main/res/values/colors.xml`

## 🌐 API Configuration

Current API endpoint:
```
https://raw.githubusercontent.com/INsITdeveloper/Drama-extension/main/datadrama.json
```

To change API:
Edit `app/src/main/java/com/ins/insdrama/api/ApiClient.kt`

## 📤 Publish to Play Store

1. **Create Signing Key**
   ```bash
   keytool -genkey -v -keystore insdrama.keystore -alias insdrama -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure Signing**
   Edit `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../insdrama.keystore")
           storePassword = "your_password"
           keyAlias = "insdrama"
           keyPassword = "your_password"
       }
   }

   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
       }
   }
   ```

3. **Build Release APK**
   ```bash
   ./gradlew assembleRelease
   ```

4. **Upload to Play Console**
   - Go to https://play.google.com/console
   - Create new app → InsDrama
   - Upload APK to production track

## 📝 License

MIT License - Created for INs IT developer

## 🆘 Support

If you encounter any issues during build:
1. Check Android Studio error messages
2. Review this guide
3. Check Gradle logs (View → Tool Windows → Gradle)
4. Search error on StackOverflow

---

**Happy Coding! 🚀**
