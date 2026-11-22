# QuickTouch Assist

Minimalist monochrome dark assistive-touch Android app built with Kotlin and Jetpack Compose. Works 100% offline with zero Firebase or cloud dependencies.

## Features

- **Main Control Hub**:
  - Live status & quick shortcuts for **Floating Overlay Permission** and **Accessibility Service**
  - **Custom Floating Icon** picker (uses Android Photo Picker, auto-crops and stores locally in internal storage)
  - **Quick Actions Grid**: Volume Up, Mute, Lock Screen, Power Menu, Home, Settings
  - **Brightness Presets**: 0%, 25%, 60%, 100%
- **Floating Bubble & Two-Stage Tray (`SYSTEM_ALERT_WINDOW`)**:
  - Draggable circular floating bubble with smooth snap-to-edge behavior
  - **Compact Horizontal Pill Tray** extending beside the bubble with outline quick-action icons + Expand
  - **Expanded Full Control Panel** with Lock, Power, Bright, Timeout (`30s`, `2m`, `10m`), Settings, Home, `STREAM_MUSIC` Volume slider (`Mute` / `50%` / `100%`), and Brightness slider (`0%` / `25%` / `60%` / `100%`)

## Download Ready-to-Install APK from GitHub

Once pushed to GitHub, the included GitHub Actions workflow (`.github/workflows/android-build.yml`) automatically builds `QuickTouch-Assist-debug.apk`:

1. Open your repository on GitHub and click **Releases** (right sidebar) → download **`QuickTouch-Assist-debug.apk`** under **QuickTouch Assist — Latest Build**.
   - Alternatively, open the **Actions** tab → select the latest **Build QuickTouch Assist APK** run → download **`QuickTouch-Assist-debug-apk`** from **Artifacts**.
2. Transfer/open the APK on your Android phone and allow installing unknown apps when prompted.

## Clone & Build Locally from GitHub

```bash
git clone <your-github-repo-url>
cd <repo-folder>
./gradlew assembleDebug
```

- On Windows: `gradlew.bat assembleDebug`
- Or open the cloned folder directly in **Android Studio** and press **Run**.
- The debug signing keystore (`debug.keystore`) is automatically restored from `debug.keystore.base64` during Gradle configuration if not already present.

## On-Device Permissions Setup

After installing on your Android/MIUI device:
1. **Floating Overlay Permission**: Tap the card in the app to enable *Display over other apps*.
2. **Accessibility Service**: Tap the card to enable *QuickTouch Assist* in Accessibility settings (required for Lock Screen and Power Menu global actions).
3. **Modify System Settings** *(prompted on first brightness/timeout change)*: Allow *Modify system settings* so brightness and screen timeout controls can update system values.
