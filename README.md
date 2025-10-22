# Saint John

Minimal Android Launcher

## Features

- Custom Android launcher with Weather, Calendar, and Notes widgets
- Integrated chat with multiple LLM providers (OpenAI, Anthropic, Google)
- Builtin LLM chat with several model options

## Building

### Prerequisites

- Android Studio with embedded JDK (Java 21)
- Android SDK
- Gradle 8.13

### Build Steps

1. Set Java home to Android Studio's embedded JDK:
```bash
export JAVA_HOME="$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

2. Build the project:
```bash
./gradlew build
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`

## Installing

### Install to Connected Device

1. Connect your Android device via USB or wireless ADB

2. Verify device connection:
```bash
adb devices
```

3. Install the APK:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

4. Launch the app:
```bash
adb shell am start -n com.jonaylor.saintjohn/.MainActivity
```

## Configuration

After installation:

1. Open the app and tap the Settings icon
2. Add your API keys for the LLM providers you want to use:
   - OpenAI API Key
   - Anthropic API Key
   - Google API Key
3. Select your preferred model from the model selector

## Project Structure

- `app/src/main/java/com/jonaylor/saintjohn/`
  - `presentation/` - UI layer (Jetpack Compose)
  - `domain/` - Business logic and models
  - `data/` - Data layer (repositories, API clients, database)
