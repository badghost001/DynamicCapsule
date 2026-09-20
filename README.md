# Dynamic Capsule

Native Android 12+ prototype for a system-wide live activity capsule.

## Current slice

- Compose settings screen with dark glass preview
- User-controlled foreground overlay service
- Overlay permission flow
- Shared capsule state model
- Working 60-second demo timer
- Expand/collapse interaction in preview and overlay

## Build

Open this folder in Android Studio with JDK 17 and an Android SDK that includes API 35. Run the `app` configuration on an Android 12+ device or emulator.

The app needs the "Display over other apps" permission before the system-wide capsule can render. Media and call integrations are intentionally the next event modules; the timer is the first end-to-end source.
