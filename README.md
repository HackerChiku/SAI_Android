# Sai Computer Education — Student Management System (Android)

Native Android client for the Sai Computer Education SMS. Built with Kotlin + Jetpack
Compose (Material 3), talking to the existing Google Apps Script `/exec` backend through a
single POST envelope.

## Tech stack

- **Language:** Kotlin 2.0, JDK 17
- **UI:** Jetpack Compose (Material 3), Navigation-Compose
- **Architecture:** MVVM (UI → ViewModel → Repository → ApiClient), unidirectional `StateFlow<UiState>`
- **DI:** Hilt (KSP)
- **Networking:** Retrofit + OkHttp (`followRedirects` for the Apps Script 302), `kotlinx.serialization`
- **Paging:** Paging 3 (students list)
- **Images/Files:** Coil, custom `ImageCompressor` (≤1200px, JPEG q85, base64), `WebView`/`DownloadManager`
- **Charts:** MPAndroidChart (line + pie on dashboard)
- **Secure storage:** AndroidX Security `EncryptedSharedPreferences` (session token)
- **Dates/Money:** `java.time` @ `Asia/Kolkata`, `NumberFormat` (en-IN) for Indian-grouped INR

### SDK

- `minSdk 26`, `targetSdk 35`, `compileSdk 35`
- `applicationId` / `namespace`: `com.saicomputer.sms`

## Setup

1. Clone the repo and open in Android Studio (Giraffe+ / AGP 8.13).
2. Configure the backend URL. The app reads `WEB_APP_URL` from `local.properties` and exposes
   it via `BuildConfig.WEB_APP_URL`. Add this line to `local.properties` (not committed to VCS):

   ```properties
   WEB_APP_URL=https://script.google.com/macros/s/<your-deployment-id>/exec
   ```

   You can alternatively set it via the `WEB_APP_URL` environment variable.
3. Sync Gradle and run.

```bash
./gradlew assembleDebug      # build debug APK
./gradlew installDebug       # install onto a connected device/emulator
```

## Backend contract

Every request is a single `POST` to `WEB_APP_URL` with `Content-Type: application/json` and body:

```json
{ "action": "students.list", "sessionToken": "<token-or-null>", "payload": { } }
```

Responses follow `{ "ok": true, "data": ... }` or `{ "ok": false, "error": { "code", "message" } }`.
`ApiClient` parses this envelope, surfaces friendly errors via `ApiException`, and clears the
session on `UNAUTHENTICATED` (routing back to Login).

## Project layout

```
core/        network, session, result (UiState), format, permission, media, validation, ui (shared composables), theme
data/        model (entities + enums), dto (typed inputs/responses), repo (one per domain) + repo/paging
feature/     auth, dashboard, students, courses, enrollments, payments, subscriptions,
             receipts, certificates, settings (institute + users), audit, exports
navigation/  Screen routes, SmsNavHost, MainShell (bottom nav), MoreScreen, AppViewModel
di/          NetworkModule, StorageModule, Qualifiers
```

## Roles & permissions

`Owner`, `Admin`, and `Receptionist`. UI surfaces are gated by `can(user, action)` in
`core/permission/Permissions.kt` (UX only — the server remains authoritative). Bottom-nav and
the "More" menu are filtered by role.

## Key behaviours

- Payments are immutable (void + record new; billing month editable only).
- Aadhaar numbers are masked everywhere except the audit-logged, uncached fetch when opening the
  student edit form (submit is blocked until the real number loads).
- All lists have Loading / Empty / Error+Retry states; filters persist in `SavedStateHandle`.
- Images are compressed before base64 upload; CSV exports share via FileProvider or DownloadManager.
- Forced change-password gate on first login (`mustChangePassword`).

## Documentation

Full app architecture, API data flow, storage, fetch triggers, navigation, and permissions
are documented in [`docs/README.md`](docs/README.md).

| Doc | Topic |
|-----|-------|
| [Architecture Overview](docs/01-architecture-overview.md) | Tech stack, MVVM layers, package structure |
| [API & Networking](docs/02-api-and-networking.md) | All 56 API actions, request/response envelope |
| [Storage & Repositories](docs/03-storage-and-repositories.md) | Local persistence, 14 repositories, composite fetches |
| [Data Fetch Triggers](docs/04-data-fetch-triggers.md) | **When each API is called** — bootstrap, screen load, user actions |
| [Features & Navigation](docs/05-features-and-navigation.md) | All screens, routes, bottom nav |
| [Auth, Roles & Startup](docs/06-auth-roles-startup.md) | Session lifecycle, permissions, startup sequence |

## Verification

`./gradlew assembleDebug` compiles cleanly. Smoke-test login → students list → dashboard
against the live `/exec` URL using a seeded Owner/Admin/Receptionist account.
