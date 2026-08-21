# Descope Device Authorization Flow — Native Samples

Sample apps demonstrating the OAuth 2.0 **Device Authorization Grant**
(RFC 8628) against Descope, on the platforms it was actually designed for:
phones, tablets, and TVs with no keyboard and no convenient way to type a
password. A user gets a short code on the device's screen and approves it
from any other device — a phone, a laptop — that does have one.

This is the native counterpart to Descope's web-based
[`device-auth-flow-demo`](https://github.com/descope-cx/device-auth-flow-demo),
and follows the same spirit as Auth0's native quickstarts
([iOS/Swift, Android/Kotlin](https://auth0.com/docs/quickstart/native/device/01-login))
— minimal, idiomatic code a developer can read top-to-bottom to see exactly
how to wire this up against Descope.

```
ios/       SwiftUI app — iOS 16+ AND tvOS 17+ (Apple TV)        — see ios/README.md
android/   Jetpack Compose app — phones, tablets, AND Android TV/Google TV — see android/README.md
```

Both platforms are actually two targets each: a phone/tablet touch UI and a
TV/D-pad UI, sharing 100% of the networking and state-machine code — only
the screen layer differs (10-foot type sizes on TV, focus-navigable
buttons, tablet-adaptive column widths). All four form factors implement
the identical flow against the identical Descope REST endpoints, so you can
compare them side by side.

## Getting started

You need three things before either app will actually sign someone in: a
Descope project, that project's ID pasted into one config file per
platform, and Device Authentication turned on for the project. That's it —
there's no separate app/client to register.

### 1. Get your Descope Project ID

Log into the [Descope Console](https://app.descope.com), open your
project (or create one), and go to **Project Settings**. Copy the
**Project ID** — it looks like `P2xxxxxxxxxxxxxxxxxxxxxxxxxxxx`.

You'll use this same value for both the app's `projectId` and its
`clientId` — see "Why is `clientId` just my Project ID?" below if that
looks wrong.

### 2. Enable Device Authentication

This is a project-level auth method, and it has to be turned on before the
device endpoint will respond to anything:

1. In the Console, go to **Authentication Methods**.
2. Find **Device Authentication** and enable it.
3. Descope will associate a flow with it (the hosted pages a user sees
   when they type in the code and approve it) — the default one works out
   of the box. If you want to customize it, see
   [Device Authentication](https://docs.descope.com/auth-methods/device-auth)
   for the screen/action sequence it needs (user code entry → login → the
   mandatory "Device Flow Approval" action → a confirmation screen).

If you skip this step, the app's Sign In button will show:
`"Device flow settings disabled for this project"`.

### 3. Confirm your region

Most Descope projects use the default US API host
(`https://api.descope.com`). If your project is in a different region or
uses a custom domain, find the right host in
[Multi-regional projects](https://docs.descope.com/management/project-settings/multi-regional) —
you'll set this in the same config file as your Project ID.

### 4. Paste your Project ID into the app

This is the only code change required:

- **iOS / tvOS** — open
  [`ios/DeviceAuthDemo/Config/DescopeConfig.swift`](ios/DeviceAuthDemo/Config/DescopeConfig.swift)
  and set:
  ```swift
  static let projectId = "<ProjectId>"  // ← your Project ID
  ```
- **Android** — open
  [`android/app/src/main/java/com/descope/deviceauthdemo/Config.kt`](android/app/src/main/java/com/descope/deviceauthdemo/Config.kt)
  and set:
  ```kotlin
  const val projectId: String = "<ProjectId>"  // ← your Project ID
  ```

If your project isn't on the default US region (step 3), also change
`baseURL`/`baseUrl` in that same file to one of the other `Region` cases,
or your own custom domain string.

Everything else — `clientId`, the device/token endpoint URLs — is derived
from `projectId` automatically. If you forget this step and run the app
anyway, it'll tell you: *"Set your Descope Project ID in
DescopeConfig.swift / Config.kt before running this demo"* instead of
failing with a confusing network error.

### 5. Run it

Pick a platform:

| Platform | Requirements | Run |
|---|---|---|
| **iOS** | Xcode 15+ | Open `ios/DeviceAuthDemo.xcodeproj`, scheme `DeviceAuthDemo`, pick an iOS Simulator, hit ▶ |
| **Apple TV** | Xcode 15+, tvOS Simulator runtime | Same project, switch scheme to `DeviceAuthDemoTV`, pick an Apple TV Simulator, hit ▶ |
| **Android / tablet** | Android Studio (Koala+) or JDK 17 + Android SDK | Open `android/` in Android Studio and hit ▶, or `cd android && ./gradlew installDebug` |
| **Android TV / Google TV** | Same as above + an Android TV emulator or device | Same APK — install it on an Android TV AVD/device instead of a phone one |

See [`ios/README.md`](ios/README.md) and [`android/README.md`](android/README.md)
for platform-specific detail (project structure, how the TV/tablet
adaptation works, how to set up a TV emulator).

### 6. Try the flow

1. Tap **Sign In**. The app calls Descope and shows a short code (e.g.
   `WDJB-MJHT`) plus a QR code.
2. On a *different* device — your phone, a browser tab — either scan the QR
   code or open the printed URL and type in the code.
3. Log in (Descope will send you a one-time code by email by default) and
   approve the request.
4. Back on the original device, the app polls in the background and flips
   to a "Signed in" screen within a few seconds of you approving, showing
   the subject and access token it received.

## How the flow works

1. **Sign In** — the app calls `POST /oauth2/v1/{projectId}/device` with
   your project's `client_id`. Descope returns a `user_code`, a
   `verification_uri`, and a `device_code`.
2. **Approve** — the app displays the `user_code` (plus a QR code encoding
   `verification_uri_complete`, if returned) and tells the user to visit
   the verification URL from another device to log in and approve it.
   Meanwhile it polls `POST /oauth2/v1/{projectId}/token` with the
   `device_code` grant on the interval Descope specifies, handling
   `authorization_pending`, `slow_down`, `access_denied`, and
   `expired_token` per the RFC 8628 spec.
3. **Signed in** — once approved, the token endpoint returns an access
   token (and, with the `openid` scope, an ID token). The app decodes the
   ID token's claims to show who's signed in.

This is the flow documented at
[Device Authentication](https://docs.descope.com/auth-methods/device-auth).
Both apps are thin native clients directly around those two REST
endpoints — there's no Descope SDK involved, since Descope's Swift/Kotlin
SDKs don't currently wrap this specific grant.

### Why is `clientId` just my Project ID?

Both apps authenticate as your project's **Generic OIDC Application** — a
Federated App (`ssoApplication` in Descope's Management SDK) that every
project has by default. This is the pattern
[Descope's Device Authentication docs](https://docs.descope.com/auth-methods/device-auth)
describe, and per
[Descope's OIDC Federated Applications docs](https://docs.descope.com/identity-federation/applications/oidc-apps),
its `client_id` is literally **your Project ID** — that's the documented
value, not a placeholder trick. It's also why there's no separate
Inbound App / OAuth client to register in the Console before this works.

### A note on project-scoped endpoints

Descope's docs show the device/token paths as plain `/oauth2/v1/device`
and `/oauth2/v1/token`. In practice, those endpoints are **project-scoped**
— confirmed against a live project's own OIDC discovery document at
`{baseURL}/{projectId}/.well-known/openid-configuration`, which lists
`token_endpoint`/`authorization_endpoint` as `oauth2/v1/{projectId}/...`.
Both apps' `Config` already build the URL this way — this is just context
for why the code doesn't match the docs' example literally.

### A note on the ID token

Both apps decode the ID token's JWT payload locally just to display the
signed-in user's name/email — **they do not verify the signature**. That's
fine for a demo showing the shape of the flow, but a real app must verify
the token (e.g. against your Descope project's JWKS, typically from a
backend) before trusting any claim in it.

## Troubleshooting

**"Device flow settings disabled for this project"** — you skipped step 2
above; enable Device Authentication in Console → Authentication Methods.

**"Set your Descope Project ID in DescopeConfig.swift / Config.kt..."** —
you skipped step 4; `projectId` is still the `<ProjectId>` placeholder.

**The sign-in code expires before I can approve it** — the `user_code`
is only valid for the `expires_in` window Descope returns (a few minutes);
just tap **Sign In** again to get a fresh one.

**Consent/scope errors when requesting `openid profile email`** — both
`Config` files ship with real scopes on by default and this works against
a standard Descope project. If your project's Device Authentication flow
has been customized, double-check its "Update user consent" step is wired
to approve the scopes the client requests; that's a flow-configuration
detail in your project, not something the sample code controls.

## Repo layout

| Path | What it is |
|---|---|
| [`ios/`](ios/) | SwiftUI app, generated with [XcodeGen](https://github.com/yonaskolb/XcodeGen) from `ios/project.yml` — two targets, `DeviceAuthDemo` (iOS/iPadOS) and `DeviceAuthDemoTV` (tvOS) |
| [`android/`](android/) | Gradle/Kotlin app using Jetpack Compose, OkHttp, ZXing (QR code), and `androidx.tv:tv-material` — one module, one APK, runs on phones, tablets, and Android TV/Google TV |

See each directory's README for platform-specific setup, project structure,
and how the TV/tablet layouts work.
