# Releasing to Google Play

## One-time setup

1. **Create an upload keystore** (keep it safe — losing it means losing update rights unless you enroll in Play App Signing, which is recommended and the default for new apps):

   ```sh
   keytool -genkeypair -v -keystore upload-keystore.jks -alias upload \
       -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Create `keystore.properties` in the repo root** (git-ignored, never commit it):

   ```properties
   storeFile=upload-keystore.jks
   storePassword=<store password>
   keyAlias=upload
   keyPassword=<key password>
   ```

3. **Google Play Console**: create the app entry (developer account required, one-time fee).

## Every release

1. Bump `versionCode` (must always increase) and `versionName` in `app/build.gradle.kts`.
2. Build the signed app bundle (Play requires `.aab`, not `.apk`):

   ```sh
   ./gradlew bundleRelease
   ```

   Output: `app/build/outputs/bundle/release/app-release.aab`
3. Smoke-test the release build on a device first: `./gradlew installRelease`
   (R8 code shrinking is enabled for release — verify creating boxes, planting,
   editing, and deleting all still work).
4. Upload the `.aab` in Play Console and roll out.

## Play Console listing checklist (first submission)

- App name, short (max 30 chars) and full (max 4000 chars) description
- Screenshots: at least 2 phone screenshots (the emulator screenshots work fine)
- App icon 512x512 PNG and a 1024x500 feature graphic
- Privacy policy URL — required even though this app collects no data.
  A ready-made page lives at `docs/privacy-policy.html`; enable GitHub Pages
  (repo Settings → Pages → deploy from branch `main`, folder `/docs`) and use
  `https://vilhelmineberg.github.io/X_Stream_Aeroponic_Propagator/privacy-policy.html`
  (requires the repo to be public)
- Data safety form: declare that no data is collected or shared
- Content rating questionnaire (this app rates "Everyone")
- App category: e.g. House & Home or Lifestyle
- Contact email (shown publicly): mikael@vilhelmineberg.se
