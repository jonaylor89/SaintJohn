# Google Play Store Distribution

## Prerequisites

1. Google Play Developer Account (one-time fee of $25)
2. Signed release APK or App Bundle (AAB)
3. App assets (icon, screenshots, descriptions)

## Preparing the Release Build

### 1. Generate a Signing Key

Create a keystore file to sign your app:

```bash
keytool -genkey -v -keystore saintjohn-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias saintjohn-key
```

Follow the prompts to set:
- Keystore password
- Key password
- Your name and organization details

Store this keystore file securely and never commit it to version control.

### 2. Configure Signing in Gradle

Create or edit `keystore.properties` in the project root:

```properties
storeFile=/path/to/saintjohn-release-key.jks
storePassword=your_keystore_password
keyAlias=saintjohn-key
keyPassword=your_key_password
```

Add to `.gitignore`:
```
keystore.properties
*.jks
```

Update `app/build.gradle.kts` signing configuration (if not already present):

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3. Build the Release Bundle

Build an Android App Bundle (recommended by Google):

```bash
./gradlew bundleRelease
```

The bundle will be at: `app/build/outputs/bundle/release/app-release.aab`

Alternatively, build a release APK:

```bash
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Uploading to Google Play Store

### 1. Create Play Store Listing

1. Go to [Google Play Console](https://play.google.com/console)
2. Click "Create App"
3. Fill in:
   - App name: "Saint John"
   - Default language
   - App or game: App
   - Free or paid: Free

### 2. Complete Store Listing

Navigate to "Store Presence > Main Store Listing" and provide:

**Required:**
- App name
- Short description (80 characters max)
- Full description (4000 characters max)
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG/JPG)
- At least 2 screenshots per supported device type
- Application type: Applications
- Category: Personalization

**Available Screenshots:**
Screenshots are available in `web/assets/`:
- `root_page_screenshot.jpg` - Home screen with widgets (weather, calendar, notes)
- `drawer_page_screenshot.jpg` - App drawer with organized folders
- `chat_page_screenshot.jpg` - Chat interface with LLM conversation

These vertical phone screenshots showcase the three main pages of the app and are suitable for Play Store listing.

**Privacy Policy:**
- Required if you collect user data (API keys are stored locally)
- Host a privacy policy URL or use Play Console's generator

### 3. Content Rating

1. Go to "Policy > App Content > Content Rating"
2. Complete the questionnaire
3. Submit for rating (free, takes a few minutes)

### 4. Target Audience

1. Go to "Policy > App Content > Target Audience"
2. Select age groups (likely 13+)

### 5. Upload Release

1. Go to "Release > Production"
2. Click "Create new release"
3. Upload the AAB file from step 3 above
4. Add release notes
5. Review and roll out

### 6. Set Up App Access

If your app requires API keys to function:

1. Go to "Policy > App Content > App Access"
2. Provide test credentials or explain that users need their own API keys
3. Add instructions for obtaining API keys

## Testing Before Production

### Internal Testing

1. Go to "Release > Testing > Internal Testing"
2. Create a release with your AAB
3. Add testers via email
4. Share the opt-in URL with testers

### Closed Testing

For a larger group of testers:

1. Go to "Release > Testing > Closed Testing"
2. Create a test track
3. Add testers or create a list
4. Release the AAB to this track

## Post-Launch

### Update Process

For subsequent releases:

1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`
2. Build new AAB: `./gradlew bundleRelease`
3. Go to Play Console > Production
4. Create new release
5. Upload AAB and add release notes
6. Roll out update

### Monitoring

- Check crash reports in Play Console
- Monitor reviews and ratings
- Track installation metrics

## Important Notes

- Google Play review typically takes 1-3 days
- App must comply with [Google Play Policies](https://play.google.com/about/developer-content-policy/)
- Launcher apps are allowed but must provide clear value
- Store your keystore and passwords securely - losing them means you cannot update your app
- Consider adding an app update mechanism or directing users to the Play Store for updates
