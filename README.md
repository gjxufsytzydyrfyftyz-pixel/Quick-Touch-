# QuickTouch Assist (By Monkey Group)

Minimalist space-black monochrome assistive-touch Android app built with Kotlin and Jetpack Compose. Works 100% offline with zero Firebase or cloud dependencies.

## Features (v3)

- **Built-In Signature Splash Screen**: Monochrome sunglasses monkey emblem with *"Made by Monkey"* pulse & fade transition on pure `#000000` black.
- **Space-Black Main Hub**:
  - Monkey Group header badge + **QuickTouch Assist** branding
  - Luminous circular **Floating Bubble Preview Orb** with **Change Bubble Icon** (Android Photo Picker, locally stored) and **Reset**
  - Three slim capsule-pill permission toggles: **Floating Overlay Permission**, **Accessibility Service**, and **Notification Access** (`ACTION_NOTIFICATION_LISTENER_SETTINGS`)
  - 3×2 circular quick-action buttons: **Volume Up**, **Mute**, **Lock Screen**, **Power Menu**, **Home**, and **Settings**
  - Outlined **OPEN CONTROL TRAY** capsule button
- **Floating Bubble, Quick Tray & Mini Music Player**:
  - Strict 1:1 circular draggable floating bubble (`56dp`) with snap-to-edge behavior
  - Horizontal quick-action pill tray beside the bubble + **Expanded Full Control Panel** (Lock, Power, Bright, Timeout `30s`/`2m`/`10m`, Volume & Brightness sliders)
  - Conditional **Mini Music Player** card directly beneath the pill tray powered offline by `MediaSessionManager` and `NotificationListenerService` (visible only when media is actively playing)

## Download Signed Release APK from GitHub

Every push to `main`/`master` or `v*` tag runs `.github/workflows/android-build.yml`, automatically generates a release keystore, builds `app-release-signed.apk` (`android:debuggable="false"`), and publishes a public GitHub Release:

1. Open your repository's **Releases** page on GitHub (or `/releases/latest`).
2. Download **`app-release-signed.apk`** and install it on your Android device.

## Clone & Build Locally

```bash
git clone <your-github-repo-url>
cd <repo-folder>
./gradlew assembleDebug
```

To build a signed release APK locally:

```bash
keytool -genkeypair -v -keystore my-upload-key.jks -storetype PKCS12 \
  -alias upload -storepass YourPass -keypass YourPass \
  -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Monkey Group,O=Monkey Group,C=US"
KEYSTORE_PATH="$PWD/my-upload-key.jks" STORE_PASSWORD=YourPass KEY_PASSWORD=YourPass ./gradlew assembleRelease
```
