# Sai SMS App — Documentation Index

**Sai Computer Education — Student Management System (SMS)**  
Android app package: `com.saicomputer.sms`

This documentation set describes the full app architecture, features, navigation, and—most importantly—**how data flows from APIs, where it is stored, and when fetches are triggered**.

---

## Quick Facts

| Aspect | Detail |
|--------|--------|
| Architecture | Single-module Android app, MVVM, Jetpack Compose |
| Backend | Google Apps Script (`/exec` endpoint) — single RPC-style POST |
| Local DB | **None** (no Room/SQLite) |
| Persistence | Encrypted session token + theme preference only |
| Data pattern | **Network-first** — server is source of truth |
| Offline | Not supported — all features require network |
| Background sync | None (no WorkManager) |

---

## Documentation Files

| # | File | What it covers |
|---|------|----------------|
| 1 | [Architecture Overview](./01-architecture-overview.md) | Tech stack, package layers, MVVM flow, DI setup |
| 2 | [API & Networking](./02-api-and-networking.md) | Request/response envelope, all 56 API actions, error handling |
| 3 | [Storage & Repositories](./03-storage-and-repositories.md) | What is persisted locally, all 14 repositories, composite fetches |
| 4 | [**Data Fetch Triggers**](./04-data-fetch-triggers.md) | **When each API is called** — app start, screen load, user actions, mutations |
| 5 | [Features & Navigation](./05-features-and-navigation.md) | All screens, routes, bottom nav, feature areas |
| 6 | [Auth, Roles & Startup](./06-auth-roles-startup.md) | Login flow, session lifecycle, permissions, bootstrap sequence |

---

## Data Flow at a Glance

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────────────┐
│   Screen    │────▶│  ViewModel   │────▶│ Repository  │────▶│ ApiClient → POST     │
│  (Compose)  │◀────│  (StateFlow) │◀────│  (suspend)  │◀────│ Google Apps Script   │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────────────┘
                           │                                        │
                           │                                        ▼
                    In-memory UI state                    { ok, data | error }
                    (filters, lists, forms)               sessionToken in body
```

**Storage layers:**

| Layer | What | Lifetime |
|-------|------|----------|
| `SessionManager` | Auth token + current `User` | Survives app restart (token encrypted) |
| `SettingsRepository` | Institute settings list | In-memory `StateFlow` until refresh |
| ViewModels | UI state, filter values, list buffers | Screen lifetime; some filters in `SavedStateHandle` |
| Paging 3 | Student list pages | In-memory via `cachedIn(viewModelScope)` |
| `ThemePreferences` | Light/dark/system | Plain SharedPreferences |

---

## Key Source Paths

```
app/src/main/java/com/saicomputer/sms/
├── SmsApplication.kt              # Hilt entry point
├── MainActivity.kt                # Compose root
├── core/
│   ├── network/ApiClient.kt       # All API calls go through here
│   ├── session/SessionManager.kt  # Token + user
│   └── permission/Permissions.kt  # Role-based UI gates
├── data/
│   ├── repo/                      # 14 repositories
│   ├── model/                     # Domain types (JSON-serializable)
│   └── dto/                       # Request/response DTOs
├── feature/                       # Screens + ViewModels per domain
├── navigation/                    # Routes, NavHost, bootstrap
└── di/                            # Hilt modules (Network, Storage)
```

---

## Domain Model (Server-Side)

The backend (Google Sheets via Apps Script) owns all business data. The app mirrors this structure in Kotlin models:

```
User (staff) ──▶ AuditLogEntry

Student ──< Enrollment >── Course
              │
              ├── Installment[]     (installment billing)
              ├── EnrollmentTopic[] (progress tracking)
              ├── Payment[]
              │     └── Receipt (optional)
              └── Certificate (optional)

Course ──< CourseTopic[]

SettingEntry[]  (institute config)
```

See [Storage & Repositories](./03-storage-and-repositories.md) for entity details.

---

## Reading Guide

- **New to the project?** Start with [Architecture Overview](./01-architecture-overview.md), then [Features & Navigation](./05-features-and-navigation.md).
- **Debugging a data issue?** Go directly to [Data Fetch Triggers](./04-data-fetch-triggers.md) and find the screen/action.
- **Adding a new API action?** Read [API & Networking](./02-api-and-networking.md) and [Storage & Repositories](./03-storage-and-repositories.md).
- **Working on auth/permissions?** See [Auth, Roles & Startup](./06-auth-roles-startup.md).

---

*Generated from codebase analysis — June 2026*
