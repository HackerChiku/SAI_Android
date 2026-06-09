# Auth, Roles & Startup

[← Back to Index](./README.md)

---

## Session Management

**File:** `core/session/SessionManager.kt`

| Property | Type | Persistence |
|----------|------|-------------|
| `token` | `String?` | Encrypted SharedPreferences (`sms_secure_prefs`, key `session_token`) |
| `currentUser` | `StateFlow<User?>` | In-memory only |
| `hasToken` | `Boolean` | Derived from token |

### Session Lifecycle

```
Login success
    → session.setSession(token, user)
    → token written to encrypted prefs
    → currentUser StateFlow updated

App restart (token exists)
    → token read from encrypted prefs
    → auth.me() validates and refreshes user

Logout / UNAUTHENTICATED
    → session.clear()
    → token removed from prefs
    → currentUser = null
```

### Auth Token in API Calls

The token is sent in the **request body**, not as an HTTP header:

```json
{ "action": "students.list", "sessionToken": "<token>", "payload": {} }
```

`ApiClient` reads `session.token` on every call. On `UNAUTHENTICATED` error, it calls `session.clear()` and the NavHost routes to Login.

---

## Authentication Flow

### Login

```
User enters email + password
    → LoginViewModel.login()
    → AuthRepository.login()
        → API: auth.login
        → session.setSession(token, user)
    → Navigate based on role:
        ├─ mustChangePassword → change_password
        ├─ Receptionist → students
        └─ Owner/Admin → dashboard
```

### Password Change

```
User submits current + new password
    → ChangePasswordViewModel
    → AuthRepository.changePassword()
        → API: auth.changePassword
        → API: auth.me (refresh user, clear mustChangePassword flag)
    → Navigate to role-based start destination
```

### Logout

```
User taps logout (profile menu)
    → AppViewModel.logout()
    → AuthRepository.logout()
        → API: auth.logout (best-effort, errors ignored)
        → session.clear()
    → Navigate to login
```

---

## App Startup Sequence

There is **no dedicated Splash Activity**. Bootstrap uses a shimmer skeleton in the NavHost.

```
[Android launches MainActivity]
        │
        ▼
[SmsApplication @HiltAndroidApp]
  Hilt initializes DI graph
  (NetworkModule, StorageModule, repos, SessionManager)
        │
        ▼
[MainActivity.onCreate]
  enableEdgeToEdge()
  Inject ThemePreferences
  setContent { SaiSmsTheme → Scaffold → SmsNavHost }
        │
        ▼
[SmsNavHost composes]
  hiltViewModel<AppViewModel>()
        │
        ▼
[AppViewModel.init → hydrate()]
  Read SessionManager.hasToken
        │
        ├─ token present ──► authRepository.me()
        │                         │
        │                    success → user set, loading = false
        │                    failure → session.clear(), user = null
        │
        └─ no token ──► loading = false, user = null
        │
        ▼
[bootstrap.loading == true?]
  YES → LoadingSkeleton (full-screen shimmer)
  NO  → compute startDestination:
        ├─ user == null        → login
        ├─ mustChangePassword  → change_password
        ├─ Receptionist        → students
        └─ Owner/Admin         → dashboard
        │
        ▼
[NavHost + optional MainShell]
  If logged in: profile menu (profile, logout)
  If route != login/change_password: bottom nav shell
        │
        ▼
[Feature screen loads]
  Each screen's ViewModel fetches data from API on its own
```

### Key Startup Files

| File | Role |
|------|------|
| `AndroidManifest.xml` | Declares `SmsApplication`, launcher `MainActivity` |
| `SmsApplication.kt` | `@HiltAndroidApp` entry |
| `MainActivity.kt` | Compose root, theme, snackbar, NavController |
| `navigation/AppViewModel.kt` | Token hydration via `auth.me` |
| `core/session/SessionManager.kt` | Token persistence + `currentUser` flow |
| `navigation/SmsNavHost.kt` | Bootstrap gate + routing |
| `core/ui/Shimmer.kt` | Loading skeletons |

---

## User Roles

**File:** `data/model/User.kt`

```kotlin
enum class UserRole { Owner, Admin, Receptionist }
```

| Role | Typical user | Start screen | Dashboard access |
|------|-------------|--------------|------------------|
| **Owner** | Institute owner | Dashboard | Yes |
| **Admin** | Manager | Dashboard | Yes |
| **Receptionist** | Front desk | Students list | No |

---

## Permission System

**File:** `core/permission/Permissions.kt`

- Map of action strings → allowed `UserRole` sets
- `can(user, action)` — returns true if user's role is in the allowed set
- **Unlisted actions default to ALLOW** for any authenticated user
- Gates are **UX-only** — the server enforces authorization

### PermissionGate Composable

Hides UI elements when `can(user, action)` returns false. Used throughout screens for buttons, menu items, and form fields.

### Role Capability Matrix

| Capability | Owner | Admin | Receptionist |
|------------|:-----:|:-----:|:------------:|
| Dashboard | Yes | Yes | No |
| Students — view/create/edit | Yes | Yes | Yes |
| Students — delete | Yes | No | No |
| Students — change status | Yes | Yes | No |
| Students — replace Aadhaar | Yes | Yes | No |
| Students — view docs | Yes | Yes | Yes |
| Courses — create/update | Yes | Yes | No |
| Courses — delete | Yes | No | No |
| Courses — view topics | Yes | Yes | Yes |
| Enrollments — create | Yes | Yes | Yes |
| Enrollments — mark complete | Yes | Yes | No |
| Enrollments — cancel | Yes | No | No |
| Enrollments — edit installments | Yes | Yes | No |
| Payments — record | Yes | Yes | Yes |
| Payments — void / edit billing month | Yes | Yes | No |
| Payments tab (bottom nav) | Yes | Yes | No |
| Receipts / certificates | Yes | Yes | No |
| Audit log | Yes | Yes | No |
| Settings / users / exports | Yes | No | No |
| Backdate toggle | Yes | No | No |
| Subscription extend/edit | Yes | Yes | No |

### Full Permission Map

| Action | Allowed roles |
|--------|---------------|
| `students.delete` | Owner |
| `students.changeStatus` | Owner, Admin |
| `students.getAadhaarNumber` | Owner, Admin, Receptionist |
| `students.getAadhaarBase64` | Owner, Admin, Receptionist |
| `students.getPhotoBase64` | Owner, Admin, Receptionist |
| `students.replacePhoto` | Owner, Admin, Receptionist |
| `students.replaceAadhaar` | Owner, Admin |
| `courses.create` | Owner, Admin |
| `courses.update` | Owner, Admin |
| `courses.delete` | Owner |
| `courses.topics.*` | Owner, Admin (Receptionist: list only) |
| `enrollments.markComplete` | Owner, Admin |
| `enrollments.cancel` | Owner |
| `enrollments.editInstallments` | Owner, Admin |
| `enrollments.setExcludedFromBilling` | Owner, Admin |
| `enrollments.topics.*` | Owner, Admin, Receptionist (unmark: Owner, Admin) |
| `payments.void` | Owner, Admin |
| `payments.editBillingMonth` | Owner, Admin |
| `subscriptions.*` | Owner, Admin (list: all roles) |
| `dashboard.*` | Owner, Admin |
| `receipts.*` | Owner, Admin |
| `certificates.*` | Owner, Admin |
| `settings.update` | Owner |
| `settings.uploadLogo` | Owner |
| `settings.uploadQR` | Owner |
| `users.*` | Owner |
| `audit.list` | Owner, Admin |
| `exports.*` | Owner |
| `system.backdate` | Owner (frontend-only, no backend route) |

---

## Theme Persistence

**File:** `core/theme/ThemePreferences.kt`

| Store | Key | Values |
|-------|-----|--------|
| Plain SharedPreferences (`sms_app_prefs`) | `theme_mode` | `light`, `dark`, `system` |

Theme is independent of auth session — persists across login/logout.

---

## Network Permissions

**AndroidManifest.xml:**
- `INTERNET`
- `ACCESS_NETWORK_STATE`

No background services, no WorkManager, no foreground services.

---

## Related Docs

- [Data Fetch Triggers](./04-data-fetch-triggers.md) — bootstrap and login fetch details
- [Features & Navigation](./05-features-and-navigation.md) — role-based navigation and bottom nav
- [API & Networking](./02-api-and-networking.md) — auth API actions
