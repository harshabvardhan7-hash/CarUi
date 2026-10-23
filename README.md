# CarUI — CarPlay-inspired Android Launcher MVP

This is a native Kotlin + Jetpack Compose starter project for an Android Home/launcher replacement. It is a first MVP, not a complete infotainment OS.

## Included

- Android Home app entry point (`CATEGORY_HOME`)
- Dark, rounded-card dashboard inspired by modern car infotainment UIs
- Live clock
- Installed-app discovery and app grid
- Quick app shortcuts
- Launches installed applications using Android's package launch intents
- Opens a map app via a `geo:` intent, with a browser fallback
- Opens Android Settings and the dialer
- Responsive adaptive app grid for different screen sizes

## Not implemented yet

- Real MediaSession playback controls / live track metadata
- Live map embedded inside the dashboard or turn-by-turn guidance
- Live weather provider
- Notification reading or messaging replies
- Persistent Bluetooth / Wi-Fi / brightness toggles
- Steering-wheel, reverse-camera, CAN bus, vehicle data or OEM integrations
- Device Owner / kiosk configuration

The media controls in this starter open the selected music app; they do not yet control playback directly. The map card is a stylized dashboard card that opens the installed map app when tapped.

## Build in Android Studio

1. Install the latest stable Android Studio and Android SDK Platform 35.
2. Open the `CarUI` folder as a project.
3. Allow Gradle to sync and download dependencies.
4. Connect an Android phone or head unit with USB debugging enabled, or create an emulator.
5. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Android Studio will show the generated APK location, usually `app/build/outputs/apk/debug/app-debug.apk`.
7. Install the APK and choose **CarUI** as the Home app when Android prompts you. If it does not prompt, go to Android Settings > Apps > Default apps > Home app and select CarUI.

## Requirements

- JDK 17 (Android Studio's bundled JBR is recommended)
- Android SDK Platform 35
- Gradle plugin resolves through Android Studio/Gradle sync
- Minimum Android API: 26 (Android 8.0)

## Important device limitations

A normal APK can replace the Home launcher but cannot replace every system screen or bypass Android permissions. Some head units restrict app visibility or use proprietary vehicle APIs. Test on the intended hardware before treating this as a production app.
