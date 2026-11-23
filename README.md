# Sutoko Android

## Release workflow

1. Check the latest uploaded version code in Play Console. Update `versionCode` in
   `build.gradle` and the display name in `app/build.gradle`.
2. Validate the candidate with a debug build and run the relevant tests on an
   emulator or device. Disable the Gradle build and configuration caches.

   ```sh
   ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --no-build-cache --no-configuration-cache
   ```

3. Configure `ADMOB_APP_ID`, `ADMOB_REWARDED_CHAPTER_UNIT_ID`, and
   `ADMOB_INTERSTITIAL_CHAPTER_UNIT_ID` with the production values. Keep signing
   credentials and publisher configuration in local settings or the secret store.
4. Build a signed **release** Android App Bundle with the existing upload key.
   Check the bundle signature, package `fr.purpletear.sutoko`, version code and
   target SDK. Keep the R8 mapping that matches the bundle.
5. Commit the verified source and create an annotated `vX.Y.Z` tag on that
   commit. In Play Console, upload the signed bundle to the intended track and
   send the release for review. Record the commit, tag, version code and bundle
   SHA-256 together.
