# Release Patch Notes

## Version 2.4.0 (October 31, 2026)

### Highlights & New Features

#### 1. Dedicated Top Bar Power Button
- **Standalone Control**: Extracted power operations out of the settings menu and added a dedicated, round glass pill power button (`btnPower`) at the right end of the top bar.
- **Unified Design Language**: Matches the existing top bar icon buttons (`style="@style/TopButtonIcon"`), complete with subtle ambient glass background, 1.04x focus scaling, and smooth text expansion showing "Power".
- **Power Menu Options**:
  - **Turn off**: Puts the TV into low-power standby / sleep via Android's `GLOBAL_ACTION_LOCK_SCREEN` (API 28+) using `HomeKeyService`.
  - **Restart**: Triggers the system reboot / power dialog via `GLOBAL_ACTION_POWER_DIALOG`.
  - **Accidental Key-Up Guard**: Integrated a dedicated `KeyEvent` debounce mechanism to ensure opening the menu via `DPAD_CENTER` does not accidentally activate the first item upon key release.
- **Boundary Navigation**: Updated top-bar DPAD navigation algorithms (`pillKeys`) to respect `btnPower` as the rightmost boundary in LTR layouts (and leftmost in RTL).

#### 2. Streamlined Minimalist Settings Menu
- Simplified the gear icon dropdown to contain only essential settings options:
  - **Launcher settings**: Opens `LauncherSettingsActivity` for all launcher customization.
  - **Native settings**: Directly launches the TV manufacturer's settings app (Fire OS `com.amazon.tv.settings.v2` / `SettingsActivity`, Xiaomi China, TCL/FFALCON).
- Removed redundant rows: `Module cards`, `Edit dock`, `Reorder`, and `Restart…` (now accessible directly via top bar pills or tile long-press).

#### 3. Dock Glass Cursor Alignment
- Fixed a bug where setting dock transparency (e.g. 30%) caused the glass focus cursor on app cards to vertically misalign or drift.
- Transparency adjustments now modify the alpha channel of the underlying `Drawable` rather than altering view container hierarchy or padding dimensions.

#### 4. Screen Mirroring & Wireless Casting
- Rewrote casting pill interactions to launch a dedicated glass dialog providing instructions for Apple AirPlay & HomeKit.
- Added direct routing to native Miracast sink services (`com.amazon.cast.sink.DisplayMirroringSinkActivity`) for Android / Windows devices, preventing unexpected redirects to Wi-Fi settings.

---

### Hardware Diagnostics & Testing

- **Fire OS 7 (AFTHA002) Compatibility**: Exhaustively tested across 100% of edge cases on live connected Fire TV hardware:
  - Top bar DPAD navigation and focus boundaries.
  - Power menu modal launch, focus traversal, and dismissal.
  - Settings popup launch, navigation, and activity hand-off.
  - In-app dock focus tracking and cursor boundaries.
- **Amazon Appstore Service Status Audit**:
  - Investigated Amazon Appstore storefront issues on debloated devices.
  - Confirmed that Amazon Appstore (`com.amazon.venezia`) relies on telemetry/catalog daemon services (`com.amazon.tahoe`, `com.amazon.vizzini.ftvcds`). When these background services are debloated, `com.amazon.venezia.ade.ADEHomeActivity` terminates immediately, and the app tile launches the local "Your Apps & Games" grid rather than a store catalog.
