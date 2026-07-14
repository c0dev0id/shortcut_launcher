# Shortcut Executor

A minimal Android app that runs a specific shortcut and immediately exits.

## How It Works

- **First launch** — the app shows a short menu of action types:
  - **System shortcut** — opens Android's shortcut picker. Choose the shortcut you want (e.g. an Automate flow); the app saves its intent.
  - **App Activity** — pick an installed app, then type the activity class (e.g. `.SettingsActivity`). The app saves an explicit-component intent for it.
- **Every launch after that** — the app fires the saved intent and exits instantly. No UI shown.

The menu is designed to grow (more action types can be added later).

To reconfigure, clear the app's data (Settings → Apps → Shortcut Executor → Storage → Clear data) and launch again.

## Building the APK

### Prerequisites

- Android SDK (API 33)
- Java 8+
- Gradle 7.0+

### Build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Troubleshooting

- **Shortcut picker doesn't show my app** — only apps that register `ACTION_CREATE_SHORTCUT` appear in the picker.
- **Shortcut fails to launch** — the target app may have been uninstalled or updated. Clear data and reconfigure.
- **Build issues** — verify `ANDROID_HOME` is set and `chmod +x gradlew`.

## License

Provided as-is for personal and development use.
