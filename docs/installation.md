# Installation Guide

## Installing on Your Device

### Method 1: Install APK via ADB

1. Enable Developer Options on your Android device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings > Developer Options
   - Enable "USB Debugging"

2. Connect your device via USB or wireless ADB

3. Verify device connection:
```bash
adb devices
```

4. Install the APK:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Install APK Directly on Device

1. Copy the APK file to your device:
   - Transfer `app/build/outputs/apk/debug/app-debug.apk` to your device via USB, cloud storage, or email

2. Enable installation from unknown sources:
   - Go to Settings > Security (or Apps)
   - Enable "Install unknown apps" for your file manager or browser

3. Open the APK file on your device and follow the installation prompts

## Setting as Default Launcher

After installation:

1. Press the Home button on your device

2. Android will show a launcher selection dialog with options like:
   - SaintJohn (this app)
   - Your previous launcher

3. Select "SaintJohn"

4. Choose "Always" to set it as the default launcher
   - Or choose "Just once" to try it temporarily

### Changing Default Launcher Later

If you want to switch back to your previous launcher:

1. Go to Settings > Apps > Default Apps > Home App
   - Or Settings > Home > Select Home App (varies by Android version)

2. Select your preferred launcher from the list

### Reverting to Previous Launcher

If SaintJohn is not working correctly:

1. Connect to ADB and run:
```bash
adb shell pm clear com.jonaylor.saintjohn
```

2. Then uninstall:
```bash
adb uninstall com.jonaylor.saintjohn
```

3. Your previous launcher should automatically become active
