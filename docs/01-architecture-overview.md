# Architecture Overview

[← Back to Index](./README.md)

---

## Project Structure

Sai SMS is a **single Gradle module** (`:app`) Android application. There are no separate `:core` or `:feature` Gradle modules — those are logical package folders inside the app module.

```
Sai_App/
├── app/                          # Entire application
│   └── src/main/java/com/saicomputer/sms/
│       ├── SmsApplication.kt
│       ├── MainActivity.kt
│       ├── core/                 # Shared infrastructure
│       ├── data/                 # Models, DTOs, repositories
│       ├── feature/              # UI screens + ViewModels
│       ├── navigation/           # Routing + bootstrap
│       └── di/                   # Hilt dependency injection
├── gradle/libs.versions.toml     # Version catalog
├── build.gradle.kts              # Root plugins
└── settings.gradle.kts           # Includes only :app
```

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt (Dagger) |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Pagination | Paging 3 (students list only) |
| Images | Coil |
| Charts | MPAndroidChart (dashboard) |
| Security | EncryptedSharedPreferences (session token) |
| Build config | `BuildConfig.WEB_APP_URL` from `local.properties` |

---

## Architecture Pattern

**Single-activity MVVM** with feature-based packages:

```
┌─────────────────────────────────────────────────────────────────┐
│                        MainActivity                              │
│  SaiSmsTheme → Scaffold → SmsNavHost                            │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
   LoginScreen          DashboardScreen      StudentsListScreen
        │                     │                     │
        ▼                     ▼                     ▼
   LoginViewModel       DashboardViewModel   StudentsListViewModel
        │                     │                     │
        ▼                     ▼                     ▼
   AuthRepository       DashboardRepository   StudentsRepository
        │                     │                     │
        └─────────────────────┴─────────────────────┘
                              │
                              ▼
                         ApiClient
                              │
                              ▼
                    Google Apps Script /exec
```

### Layer Responsibilities

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **UI** | `feature/*/` | Compose screens, user interaction, observe ViewModel state |
| **ViewModel** | `feature/*/` | UI state (`StateFlow`), coroutine launches, filter logic |
| **Repository** | `data/repo/` | API calls, response mapping, minimal in-memory cache |
| **ApiClient** | `core/network/` | Request envelope, response parsing, auth error handling |
| **Session** | `core/session/` | Token persistence, current user state |
| **Navigation** | `navigation/` | Routes, NavHost, bottom shell, app bootstrap |

---

## Dependency Injection (Hilt)

| Component | Annotation | File |
|-----------|------------|------|
| Application | `@HiltAndroidApp` | `SmsApplication.kt` |
| Activity | `@AndroidEntryPoint` | `MainActivity.kt` |
| ViewModels | `@HiltViewModel` | `feature/**/*ViewModel.kt` |
| Repositories | `@Singleton` + `@Inject constructor` | `data/repo/*.kt` |
| ApiClient | `@Singleton` | `core/network/ApiClient.kt` |
| SessionManager | `@Singleton` | `core/session/SessionManager.kt` |

### Hilt Modules

| Module | File | Provides |
|--------|------|----------|
| `NetworkModule` | `di/NetworkModule.kt` | `Json`, `OkHttpClient`, `Retrofit`, `ApiService`, `@WebAppUrl` |
| `StorageModule` | `di/StorageModule.kt` | Encrypted `SharedPreferences` (`sms_secure_prefs`) |

Repositories are **not** bound via `@Binds` — Hilt constructor-injects them directly.

---

## Package Layers (Logical)

### `core/` — Shared Infrastructure

| Sub-package | Contents |
|-------------|----------|
| `core/network/` | `ApiService`, `ApiClient`, `ApiEnvelope`, `ApiException` |
| `core/session/` | `SessionManager` — token + user |
| `core/permission/` | `Permissions.kt`, `can()`, `PermissionGate` composable |
| `core/theme/` | `SaiSmsTheme`, `ThemePreferences`, `ThemeViewModel` |
| `core/ui/` | Shared composables: top bars, state views, shimmer, filters |

### `data/` — Data Layer

| Sub-package | Contents |
|-------------|----------|
| `data/model/` | Domain types (`Student`, `Enrollment`, `Payment`, etc.) — `@Serializable` for JSON |
| `data/dto/` | Request inputs and response wrappers |
| `data/repo/` | 14 repository classes + `paging/StudentsPagingSource` |

### `feature/` — Feature Modules (by domain)

| Package | Screens | ViewModels |
|---------|---------|------------|
| `feature/auth/` | Login, ChangePassword | `LoginViewModel`, `ChangePasswordViewModel` |
| `feature/dashboard/` | Dashboard | `DashboardViewModel` |
| `feature/students/` | List, Form, Detail, Documents | 4 ViewModels |
| `feature/courses/` | List, Form, Detail | 3 ViewModels |
| `feature/enrollments/` | List, Wizard, Detail | 3 ViewModels |
| `feature/payments/` | List, Form | 2 ViewModels |
| `feature/subscriptions/` | List | 1 ViewModel |
| `feature/receipts/` | List | 1 ViewModel |
| `feature/certificates/` | List | 1 ViewModel |
| `feature/audit/` | Audit log | 1 ViewModel |
| `feature/settings/` | Institute settings, User management | 2 ViewModels |
| `feature/exports/` | CSV exports | 1 ViewModel |

### `navigation/` — Routing

| File | Role |
|------|------|
| `Screen.kt` | Sealed route definitions |
| `SmsNavHost.kt` | NavHost, auth routing, bootstrap gate |
| `MainShell.kt` | Bottom navigation bar |
| `MoreScreen.kt` | Secondary menu (courses, receipts, etc.) |
| `AppViewModel.kt` | Session hydration on app start |

---

## Design Principles

1. **Thin client** — Google Sheets/Apps Script is the single source of truth. The Android app is a Compose UI over RPC calls.
2. **Network-first** — No offline cache. Every screen fetches fresh data from the server on load.
3. **Auth in body** — `sessionToken` rides in the JSON request envelope, not as an HTTP header.
4. **UX-only permissions** — `can(user, action)` gates UI elements; the server enforces authorization.
5. **Sensitive data on-demand** — Aadhaar numbers and document images are fetched only when explicitly requested, never cached locally.

---

## What Is NOT in the App

| Missing | Implication |
|---------|-------------|
| Room / SQLite | No local entity database |
| DataStore | No typed preferences beyond SharedPreferences |
| WorkManager | No background sync or scheduled jobs |
| Pull-to-refresh | Refresh via screen load, filter change, or retry button |
| Multi-module Gradle | Everything in `:app` |

---

## Related Docs

- [API & Networking](./02-api-and-networking.md) — how requests are built and sent
- [Data Fetch Triggers](./04-data-fetch-triggers.md) — when data is loaded
- [Features & Navigation](./05-features-and-navigation.md) — all screens and routes
