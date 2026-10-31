# Developer Guide & Engineering Process

This document provides a comprehensive guide to the architecture, development process, build pipeline, testing, and hardware deployment of the **CleanYourTV** launcher.

---

## 1. Architecture & Design Principles

CleanYourTV is a high-performance, ultra-lean native home screen for Android TV, Google TV, and Fire TV devices. It is built in pure Java against Android SDK 34 (with a minimum SDK floor of API 26 / Android 8.0), weighing under 600 KB and designed to run smoothly on budget TVs with as little as 1 GB of RAM.

### Hybrid Design Language (Apple tvOS + Samsung One UI)
The launcher combines the best aspects of Apple tvOS and Samsung One UI:
- **Equidistant Dynamic Dock Band**: The first row (the dock) features a frosted glass band with strictly uniform padding (`pad = 14 * density` / 28px) on all four sides (top, bottom, left, right). The outer corner radius dynamically matches the concentric geometry of the tile corners (`radius = radiusPx(cardRadius, pad)`). The band dynamically bounds only the apps actually present in the dock, ignoring empty/invisible slots.
- **Top Control Center**:
  - **Inputs Selector (`btnInputs`)**: Direct switching between TV input sources (HDMI 1/2/3, AV, Composite).
  - **Live In-App Wi-Fi Scanner (`btnWifi`)**: Performs real-time Wi-Fi network scans, displaying live signal bars, lock indicators, frequency (2.4 GHz / 5 GHz), and connecting directly without leaving the launcher.
  - **AirPlay & Cast Receiver Controller (`btnCast`)**: Fast access to AirPlay, Google Cast, Miracast, and DLNA receiver management.
  - **Tile Reorder Shortcut (`btnReorder`)**: One-touch entry into visual drag-and-drop tile reordering mode.
  - **Minimalist Gear Settings (`btnSettings`)**: Clean hybrid popup providing straightforward access to Launcher Settings and Native System Settings.
- **Prominent Clock & Date Typography**: High-contrast, oversized clock header with crisp date typography designed for optimal 10-foot viewing distance.
- **Samsung One UI 9 Style "Now Bar"**: Floating interactive media card displaying real-time playback information, album artwork, animated sound equalizer, track progress bar, playback controls (previous, play/pause, next), and audio output routing.
- **Persistent Launcher Interception**: `HomeKeyService` Accessibility Service intercepts Home key and navigation events to ensure the launcher remains the default home screen without falling back to stock OEM launchers (Amazon Fire TV, Google TV, Android TV).
- **Smooth 60+ FPS Spring Physics & Glass Highlight**: Floating focus highlight with spring animation interpolators and GPU-accelerated transforms.

---

## 2. Codebase Organization

The project is structured under the `launcher/` directory:

```
launcher/
├── app/
│   ├── build.gradle                          # Android application build configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml           # Permissions, activities, receivers, services
│       │   ├── java/com/gathin/tvhome/
│       │   │   ├── MainActivity.java         # Core launcher activity, topbar controls, dialogs
│       │   │   ├── DockDecoration.java       # Equidistant glass dock band ItemDecoration
│       │   │   ├── AppAdapter.java           # Grid RecyclerView adapter with focus animations
│       │   │   ├── HomeKeyService.java       # Accessibility service for Home key interception
│       │   │   ├── MediaNotificationListener.java # Notification listener for Now Bar media state
│       │   │   ├── AppLibraryDialog.java     # App drawer and tile management interface
│       │   │   ├── DockSlots.java            # Slot allocation & focus routing logic
│       │   │   ├── GlassView.java            # Spring-animated glass highlight view
│       │   │   ├── TopFade.java              # Fading edge shader for scrolling rows
│       │   │   ├── Order.java                # Tile reordering persistence model
│       │   │   ├── Config.java               # Runtime configuration & theme parser
│       │   │   └── SystemSettings.java       # OEM & Android system settings dispatcher
│       │   └── res/
│       │       ├── layout/activity_main.xml  # Primary UI layout (clock, topbar, grid, now bar)
│       │       ├── layout-iw/activity_main.xml # Mirrored RTL layout (Hebrew)
│       │       ├── values/config.xml         # XML configuration parameters & switches
│       │       ├── values/strings.xml        # Localization strings (30 languages supported)
│       │       └── drawable/                 # Vector drawables, glass pills, icons
│       └── test/java/com/gathin/tvhome/
│           ├── DockCornersTest.java          # Concentric corner radius & padding tests
│           ├── DockSlotsTest.java            # Slot filling & order calculation tests
│           └── OrderTest.java                # Tile ordering & persistence unit tests
├── build.gradle                              # Root buildscript
├── gradle.properties                         # JVM arguments and AndroidX flags
└── gradlew / gradlew.bat                     # Gradle wrapper (Gradle 8.9)
```

---

## 3. Prerequisites & Environment Setup

Building the project requires standard Android development tooling:

| Requirement | Supported Version | Installation |
|---|---|---|
| **JDK** | 17 to 22 (Java 17 recommended) | `winget install EclipseAdoptium.Temurin.17.JDK` or `brew install openjdk@17` |
| **Android SDK Platform** | API 34 (`android-34`) | `sdkmanager "platforms;android-34"` |
| **Android Build-Tools** | 34.0.0 | `sdkmanager "build-tools;34.0.0"` |
| **Gradle** | 8.9 | Bundled via Gradle Wrapper (`gradlew` / `gradlew.bat`) |
| **ADB** | Latest Platform Tools | `sdkmanager "platform-tools"` |

Set your `JAVA_HOME` and `ANDROID_HOME` environment variables:
```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

# macOS / Linux
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
export ANDROID_HOME="$HOME/Android/Sdk"
```

---

## 4. Build Pipeline

Navigate to the `launcher/` directory:

### Debug Build
```bash
cd launcher
./gradlew assembleDebug
```
Output artifact: `launcher/app/build/outputs/apk/debug/app-debug.apk`

### Release Build
```bash
cd launcher
./gradlew assembleRelease
```
Output artifact: `launcher/app/build/outputs/apk/release/app-release.apk`

---

## 5. Automated Testing & Verification

Unit tests validate core layout math, concentric corner calculation, slot ordering, and configuration serialization:

```bash
cd launcher
./gradlew testReleaseUnitTest
```

Key test suites:
- `DockCornersTest`: Validates concentric outer corner radii (`radiusPx(cardRadius, pad)`) and equidistant padding across 4 to 7 column layouts.
- `DockSlotsTest`: Tests dock slot allocation with filled vs. empty dock slots under various column configurations.
- `OrderTest`: Tests serialization, rank mapping, and persistence of customized app positions.

---

## 6. Real-Device Hardware Deployment & ADB Workflow

CleanYourTV is designed and tested directly on physical television hardware over Wi-Fi ADB.

### 1. Enable Developer Options & Network Debugging on the TV
1. On your TV, navigate to **Settings** → **Device Preferences** (or **System**) → **About**.
2. Highlight **Build** (or **Android TV OS build**) and click **OK** 7 times until Developer Mode is unlocked.
3. Go back to **Developer options** and enable **USB debugging** and **Network debugging** (or **Wireless debugging**).
4. Note your TV's IP address (e.g., `192.168.1.50` or `10.0.0.6`).

### 2. Connect via ADB
```bash
adb connect <TV_IP>:5555
adb devices
```

### 3. Deploy the Launcher APK
```bash
adb -s <TV_IP>:5555 install -r launcher/app/build/outputs/apk/release/app-release.apk
```

### 4. Grant Runtime Permissions & Accessibility Service
For full functionality (live Wi-Fi scanning, media state monitoring, and Home key interception):
```bash
# Wi-Fi scanning permissions
adb -s <TV_IP>:5555 shell pm grant com.gathin.tvhome android.permission.ACCESS_FINE_LOCATION
adb -s <TV_IP>:5555 shell pm grant com.gathin.tvhome android.permission.NEARBY_WIFI_DEVICES

# Persistent launcher interception via Accessibility Service
adb -s <TV_IP>:5555 shell pm grant com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
adb -s <TV_IP>:5555 shell settings put secure enabled_accessibility_services com.gathin.tvhome/.HomeKeyService

# Launch the activity
adb -s <TV_IP>:5555 shell "am start -n com.gathin.tvhome/.MainActivity"
```

### 5. Inspecting Layout & Capturing Screenshots
Verify live layout bounds, view hierarchy, and visual alignment:
```bash
# Dump top activity view hierarchy
adb -s <TV_IP>:5555 shell dumpsys activity top

# Capture and pull live screenshot
adb -s <TV_IP>:5555 shell screencap -p /sdcard/screen.png
adb -s <TV_IP>:5555 pull /sdcard/screen.png ./screen.png
```

---

## 7. Configuration & Customization (`config.xml`)

The launcher behavior is centrally configurable via [`launcher/app/src/main/res/values/config.xml`](../launcher/app/src/main/res/values/config.xml):

| Configuration Key | Type | Default | Description |
|---|---|---|---|
| `dock_row` | `bool` | `true` | Enables dedicated dock band for the first row of apps. |
| `dock_band` | `bool` | `true` | Draws the frosted glass band behind the dock row. |
| `dock_even_padding` | `bool` | `false` | When true, scales padding dynamically to inter-tile spacing. |
| `fill_dock` | `bool` | `true` | Fills free dock slots with grid apps; false leaves slots empty. |
| `dock_opacity` | `integer` | `100` | Opacity percentage (30, 50, 70, 100) for the dock band. |
| `dock_glass` | `bool` | `true` | Enables white gradient glass tinting on the dock band. |
| `columns` | `integer` | `5` | Number of columns in the grid (4, 5, 6, or 7). |
| `rows_below_dock` | `integer` | `1` | Number of visible rows below the dock band. |
| `now_bar_enabled` | `bool` | `true` | Enables the Samsung One UI 9 style floating Now Bar media widget. |
| `reduced_motion` | `bool` | `false` | Disables spring physics and heavy animations for low-end GPUs. |

---

## 8. Contributing & Git Standards

- **Commit Format**: Follow conventional commits (`feat(ui): ...`, `fix(dock): ...`, `refactor(topbar): ...`, `docs: ...`).
- **Performance Budget**: The APK size must remain under 600 KB. Do not introduce large third-party dependencies, heavy image assets, or continuous background wakelocks.
- **Concentric Geometry**: All nested and adjacent UI containers must maintain uniform equidistant spacing and concentric corner radii (`R_outer = R_inner + padding`).
