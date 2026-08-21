# Descope Device Flow Demo — iOS (SwiftUI)

A minimal SwiftUI app demonstrating the OAuth 2.0 Device Authorization Grant
against Descope. See the [repo root README](../README.md) for the full
picture of what this demonstrates and how to configure a Descope project for
it.

## Requirements

- Xcode 15+ (targets iOS 16+)
- [XcodeGen](https://github.com/yonaskolb/XcodeGen) — only needed if you want
  to regenerate the `.xcodeproj` after editing `project.yml`; the generated
  project is already checked in, so you can skip this and just open it.

## Setup

1. Set your Descope `projectId` (and region, if needed) in
   [`DeviceAuthDemo/Config/DescopeConfig.swift`](DeviceAuthDemo/Config/DescopeConfig.swift).
   `clientId` is derived from it automatically — this demo authenticates as
   your project's Generic OIDC Application, the app type
   [Descope's Device Authentication docs](https://docs.descope.com/auth-methods/device-auth)
   describe using. See the
   [root README's Getting Started](../README.md#getting-started) for the
   full setup walkthrough (getting your Project ID, enabling Device
   Authentication), and its
   [Troubleshooting](../README.md#troubleshooting) section if anything
   doesn't work out of the box.
2. Open `DeviceAuthDemo.xcodeproj` in Xcode.
3. Pick an iOS Simulator (or a device) and hit Run.

If you change `project.yml` (e.g. to add a target or a dependency),
regenerate the project with:

```bash
xcodegen generate
```

## Project structure

```
DeviceAuthDemo/
  App/                 App entry point (DeviceAuthDemoApp.swift) — shared by both targets
  Config/              Descope project configuration (client_id, base URL)
  Models/              Codable request/response types for the device flow
  Services/            DeviceAuthService (the two HTTP calls) + a tiny JWT decoder
  ViewModels/           DeviceAuthViewModel — the state machine that drives
                        the sign-in → verification → signed-in flow and
                        owns the polling loop
  Views/                SignInView, VerificationView (user code + QR code),
                        ProfileView, and the ContentView that switches
                        between them — `#if os(tvOS)` blocks in each adjust
                        type size/padding for a 10-foot layout, same code
  Resources/            Assets.xcassets (iOS target only)
  TV/                   Assets.xcassets + Info-tvOS.plist for the tvOS target
```

## How the state machine works

`DeviceAuthViewModel.State` has five cases: `signedOut`, `startingFlow`,
`awaitingApproval` (carries the device authorization response and a live
countdown), `signedIn` (carries the token response and decoded ID token
claims), and `error`. `startSignIn()` kicks off a `Task` that calls
`DeviceAuthService.startDeviceAuthorization()`, then loops calling
`pollForToken(deviceCode:)` on the server-specified interval — backing off
by 5s on `slow_down`, stopping on `access_denied`/`expired_token`, and
resolving to `signedIn` on success. Cancelling out of the verification
screen (or signing out) cancels that `Task`.

## Apple TV (tvOS)

This project has a second target, **DeviceAuthDemoTV** (bundle id
`com.descope.deviceauthdemo.tv`), for exactly the use case device flow was
designed for: an Apple TV showing the code, approved from your phone or
laptop. It shares 100% of the networking/state-machine code with the iOS
target — `DeviceAuthService`, `DeviceAuthViewModel`, and the models don't
know or care what platform they're running on.

What's different is only the view layer: each of `SignInView`,
`VerificationView`, and `ProfileView` has `#if os(tvOS)` blocks that scale up
type size and padding for a 10-foot viewing distance. SwiftUI `Button`s are
focus-navigable via the Siri Remote automatically — no extra code needed
there.

To run it:

1. In Xcode, switch the scheme selector (top toolbar) from `DeviceAuthDemo`
   to **`DeviceAuthDemoTV`**.
2. Pick an Apple TV simulator (Xcode → Window → Devices and Simulators, or
   just create one — Apple TV / Apple TV 4K device types are usually
   preinstalled once you've downloaded a tvOS Simulator runtime from
   Xcode's Settings → Platforms).
3. Hit Run.

Note: the tvOS target's asset catalog (`DeviceAuthDemo/TV/Assets.xcassets`)
only has an `AccentColor` — no App Icon/Top Shelf Image brand assets, since
those need real artwork. Add a proper `App Icon & Top Shelf Image`
brand-assets catalog (and set `ASSETCATALOG_COMPILER_APPICON_NAME` in
`project.yml` back to it) before shipping this anywhere beyond the
Simulator.

## iPad

The `DeviceAuthDemo` target already builds for iPad — `TARGETED_DEVICE_FAMILY`
in `project.yml` includes both iPhone and iPad, and `project.yml` adds
`UISupportedInterfaceOrientations~ipad` so iPad supports all four
orientations (iPhone stays portrait-only). No separate target is needed;
iPad runs the same binary as iPhone.

What *is* iPad-specific: `SignInView`, `VerificationView`, and `ProfileView`
each read `@Environment(\.horizontalSizeClass)` and cap their content to a
500pt-wide column, centered, whenever the size class is `.regular` (iPad).
Without this, elements like the Sign In button — which use
`.frame(maxWidth: .infinity)` for a natural full-width look on iPhone —
would stretch across the entire iPad screen instead of reading as a
sensible, centered form.
