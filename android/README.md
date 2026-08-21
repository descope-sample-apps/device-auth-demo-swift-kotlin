# Descope Device Flow Demo — Android (Kotlin + Compose)

A minimal Jetpack Compose app demonstrating the OAuth 2.0 Device
Authorization Grant against Descope. See the
[repo root README](../README.md) for the full picture of what this
demonstrates and how to configure a Descope project for it.

## Requirements

- Android Studio (Koala+) or a JDK 17 + Android SDK (compileSdk 35,
  build-tools 35) command-line setup — AGP 8.7+ and Kotlin 2.1+ (both pinned
  in the Gradle files already), needed for the `androidx.tv:tv-material`
  dependency used by the Android TV screens
- minSdk 26

## Setup

1. Set your Descope `projectId` (and region, if needed) in
   [`app/src/main/java/com/descope/deviceauthdemo/Config.kt`](app/src/main/java/com/descope/deviceauthdemo/Config.kt).
   `clientId` is derived from it automatically — this demo authenticates as
   your project's Generic OIDC Application, the app type
   [Descope's Device Authentication docs](https://docs.descope.com/auth-methods/device-auth)
   describe using. See the
   [root README's Getting Started](../README.md#getting-started) for the
   full setup walkthrough (getting your Project ID, enabling Device
   Authentication), and its
   [Troubleshooting](../README.md#troubleshooting) section if anything
   doesn't work out of the box.
2. Open the `android/` folder in Android Studio and run the `app`
   configuration, or from the command line:

```bash
./gradlew assembleDebug
./gradlew installDebug   # with a device/emulator connected
```

(`local.properties` pointing at your Android SDK is created automatically
by Android Studio; if you're building from the command line without it,
create one with `sdk.dir=/path/to/Android/sdk`.)

## Project structure

```
app/src/main/java/com/descope/deviceauthdemo/
  Config.kt              Descope project configuration (client_id, base URL)
  MainActivity.kt         Hosts the Compose content — picks the phone or TV
                          screens at runtime, see "Android TV" below
  model/                  Data classes for the device flow's request/response bodies
  network/                DeviceAuthService (the two HTTP calls, via OkHttp)
  util/                   A tiny JWT payload decoder + isTelevision() detector
  viewmodel/              DeviceAuthViewModel — the state machine that drives
                          the sign-in → verification → signed-in flow and
                          owns the polling loop
  ui/                     SignInScreen, VerificationScreen (user code + QR
                          code via ZXing), ProfileScreen, and the
                          DeviceAuthApp composable that switches between them
                          — the touch/phone layer
  ui/tv/                  TvSignInScreen, TvVerificationScreen, TvProfileScreen,
                          TvDeviceAuthApp, and TvTheme — the D-pad/Android TV
                          layer, built on androidx.tv:tv-material
```

## How the state machine works

`DeviceAuthUiState` is a sealed class with five cases: `SignedOut`,
`StartingFlow`, `AwaitingApproval` (carries the device authorization
response and a live countdown), `SignedIn` (carries the token response and
decoded ID token claims), and `Error`. `DeviceAuthViewModel.startSignIn()`
launches a coroutine that calls `DeviceAuthService.startDeviceAuthorization()`,
then loops calling `pollForToken(deviceCode)` on the server-specified
interval — backing off by 5s on `slow_down`, stopping on
`access_denied`/`expired_token`, and resolving to `SignedIn` on success.
Cancelling out of the verification screen (or signing out) cancels that
coroutine.

## Android TV / Google TV

This is **one APK that runs on both phones and Android TV / Google TV** —
there's no separate module or flavor. `AndroidManifest.xml` declares
`android.software.leanback` and `android.hardware.touchscreen` as not
required, adds a `LEANBACK_LAUNCHER` intent-filter category (what makes the
app show up on the TV home screen) alongside the normal `LAUNCHER` one, and
points `android:banner` at a 320×180 banner image
(`res/drawable-nodpi/tv_banner.png`) that Android TV's launcher shows
instead of a phone icon.

At runtime, `MainActivity` calls `Context.isTelevision()`
([`util/DeviceType.kt`](app/src/main/java/com/descope/deviceauthdemo/util/DeviceType.kt),
backed by `UiModeManager`) and renders either the phone `DeviceAuthApp` or
the TV `TvDeviceAuthApp` — same `DeviceAuthViewModel` either way, just a
different screen layer:

- **D-pad focus, not touch.** The TV screens use
  [`androidx.tv:tv-material`](https://developer.android.com/jetpack/androidx/releases/tv)
  instead of Compose Material3 — its `Button`/`OutlinedButton` render a
  visible focus ring for remote navigation, which plain Material3 buttons
  don't. Each screen also grabs initial focus with a `FocusRequester` so the
  primary action is highlighted the moment the screen appears (no D-pad
  press needed to discover it).
- **10-foot UI.** Larger type (`MaterialTheme.typography.headlineLarge`,
  `displayLarge`, etc.) and generous horizontal padding (96.dp) for
  couch-distance viewing.
- **Committed dark theme** (`TvTheme.kt`) rather than following system
  light/dark — Android TV UIs are almost always viewed in a dark room.

To try it: create an Android TV emulator (Android Studio → Device Manager
→ Create Device → **TV** category — 1080p is fine) or `avdmanager create avd
-k "system-images;android-36;android-tv;arm64-v8a"`, then
`./gradlew installDebug` with it running. The Compose preview tooling and
regular phone emulators are unaffected — `isTelevision()` only returns true
on an actual Leanback/TV device.

## Tablets

There's no separate tablet target or flavor — tablets run the same phone
screens (`ui/`), just with one adaptation: `SignInScreen`, `VerificationScreen`,
and `ProfileScreen` each wrap their content in a `Box` +
`Modifier.widthIn(max = 480.dp)`, centered. Without this, elements like the
Sign In button — which use `Modifier.fillMaxWidth()` for a natural
full-width look on a phone — would stretch across the entire tablet screen
instead of reading as a sensible, centered form.

To try it on a real tablet emulator: Android Studio → Device Manager →
Create Device → **Tablet** category (Pixel Tablet works well) — just be
aware the Play Store system images for tablet profiles can need 7GB+ of
free disk space to create; the non-Play-Store `google_apis` system image
for the same API level is a much smaller download if you're tight on
space.
