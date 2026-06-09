# Data Fetch Triggers

[← Back to Index](./README.md)

This is the primary reference for **when API calls happen** — what triggers each fetch, which repository methods are invoked, and what happens to the data afterward.

---

## Trigger Categories

| Category | Description |
|----------|-------------|
| **Bootstrap** | App cold start — session hydration |
| **ViewModel init** | Screen first composition — automatic load |
| **Navigation** | `LaunchedEffect(id)` when entering detail/form screens |
| **User filter** | Search, status, period, date range changes |
| **User action** | Button tap — create, void, upload, export |
| **Mutation reload** | After successful write — `reload()` refreshes detail |
| **On-demand** | Explicit tap to view document/image |
| **Paging** | Scroll-triggered page loads (students only) |

---

## 1. App Bootstrap (Cold Start)

**ViewModel:** `AppViewModel` (`navigation/AppViewModel.kt`)  
**Trigger:** `init { hydrate() }` — runs once when NavHost composes

```
App launch
    │
    ├─ session.hasToken == false
    │       → bootstrap.loading = false, user = null
    │       → Navigate to Login
    │
    └─ session.hasToken == true
            → auth.me()  ────────────────── API: auth.me
            │
            ├─ success → session.setUser(user)
            │            bootstrap.loading = false
            │            → Route by role (see Auth doc)
            │
            └─ failure → session.clear()
                         → Navigate to Login
```

**While loading:** Full-screen shimmer skeleton (no dedicated Splash Activity).

**Storage effect:** Token read from encrypted prefs; user held in `SessionManager.currentUser` StateFlow.

**What does NOT happen on bootstrap:**
- No dashboard preload
- No student/course/enrollment list prefetch
- Feature screens fetch their own data when composed

---

## 2. Login

**ViewModel:** `LoginViewModel` (`feature/auth/LoginViewModel.kt`)  
**Trigger:** User submits email + password

| Step | API | Storage effect |
|------|-----|----------------|
| 1 | `auth.login` | `session.setSession(token, user)` — token written to encrypted prefs |
| 2 | Navigate | Role-based start destination (dashboard or students) |

**No additional prefetch after login.** The destination screen's ViewModel loads its own data.

---

## 3. Logout

**ViewModel:** `AppViewModel.logout()`  
**Trigger:** User taps logout in profile menu

| Step | API | Storage effect |
|------|-----|----------------|
| 1 | `auth.logout` (best-effort) | — |
| 2 | `session.clear()` | Token removed from encrypted prefs; user = null |
| 3 | Navigate to Login | — |

---

## 4. List Screens — Load on ViewModel Init

These screens fetch data automatically when the ViewModel is created (screen first composed).

### Dashboard

| | |
|---|---|
| **ViewModel** | `DashboardViewModel` |
| **Init trigger** | `init { load(period, initial=true) }` |
| **APIs** | `dashboard.summary(period)`, `dashboard.paymentPendingStudents()` |
| **Re-fetch triggers** | Period dropdown change (`setPeriod`), retry button (`retry()`) |
| **Filter persistence** | Period saved in `SavedStateHandle` |
| **Storage** | In-memory `DashboardUiState` only |

### Students List

| | |
|---|---|
| **ViewModel** | `StudentsListViewModel` |
| **Init trigger** | Paging flow starts on first composition |
| **API** | `students.list` (via `StudentsPagingSource`, pageSize=50) |
| **Re-fetch triggers** | Search change (300ms debounce), status filter, registration session filter |
| **Filter persistence** | Search, status, session in `SavedStateHandle` |
| **Storage** | Paging 3 in-memory cache via `cachedIn(viewModelScope)` |
| **Retry** | `retry()` invalidates paging source |

### Enrollments List

| | |
|---|---|
| **ViewModel** | `EnrollmentsListViewModel` |
| **Init trigger** | `init { loadCourses(); load() }` |
| **APIs (parallel)** | `enrollments.list`, `students.list`, `courses.list` |
| **Re-fetch triggers** | Server filter changes (status, billing type, course) |
| **Client-side** | Search and sort applied in-memory after fetch |
| **Storage** | Full list buffer in ViewModel state |

### Payments List

| | |
|---|---|
| **ViewModel** | `PaymentsListViewModel` |
| **Init trigger** | `init { load() }` |
| **APIs** | `payments.list` + `receipts.list` (composite in repository) |
| **Re-fetch triggers** | Server filter changes (status, billing month, session) |
| **Client-side** | Search and sort applied in-memory |
| **Storage** | Full list buffer in ViewModel state |

### Receipts List

| | |
|---|---|
| **ViewModel** | `ReceiptsViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `receipts.list` |
| **Re-fetch triggers** | Filter changes (date range, session) |
| **Client-side** | Search and sort in-memory |
| **On-demand** | `loadReceipt(receiptId)` → `receipts.get` when user opens PDF |

### Courses List

| | |
|---|---|
| **ViewModel** | `CoursesListViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `courses.list` |
| **Re-fetch triggers** | After create (navigate back) — manual `load()` |

### Certificates List

| | |
|---|---|
| **ViewModel** | `CertificatesViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `certificates.list` |
| **On-demand** | `loadCertificate(id)` → `certificates.get` when user opens PDF |

### Subscriptions List

| | |
|---|---|
| **ViewModel** | `SubscriptionsListViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `subscriptions.list` |
| **Re-fetch triggers** | Pending-only toggle |
| **Note** | Route exists but no menu link — legacy/orphan screen |

### Audit Log

| | |
|---|---|
| **ViewModel** | `AuditViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `audit.list` |
| **Re-fetch triggers** | Date range, action type, user filter changes |

### Institute Settings

| | |
|---|---|
| **ViewModel** | `SettingsViewModel` |
| **Init trigger** | `init { load() }` → `settingsRepository.refresh()` |
| **API** | `settings.getAll` |
| **Storage** | `SettingsRepository.settings` StateFlow updated |
| **After update** | `settings.update` → auto `refresh()` |

### User Management

| | |
|---|---|
| **ViewModel** | `UserManagementViewModel` |
| **Init trigger** | `init { load() }` |
| **API** | `users.list` |
| **Re-fetch triggers** | After create, update, or reset password |

---

## 5. Detail / Form Screens — Load on Navigation

These screens use `LaunchedEffect(id)` or `initialize(id)` when the user navigates to them.

### Student Detail

| | |
|---|---|
| **ViewModel** | `StudentDetailViewModel` |
| **Trigger** | `LaunchedEffect(studentId) { viewModel.load(id) }` |
| **APIs** | `students.get` → internally calls `students.get` + `payments.list` |
| **Reload after** | Status change, void payment → `reload()` |
| **On-demand** | `StudentDocumentsViewModel.loadPhoto/loadAadhaar` → `students.getPhotoBase64` / `students.getAadhaarBase64` |
| **On-demand** | `loadReceipt(receiptId)` → `receipts.get` |

### Student Form (Create / Edit)

| | |
|---|---|
| **ViewModel** | `StudentFormViewModel` |
| **Trigger (edit)** | `initialize(studentId)` → `students.get` + `students.getAadhaarNumber` |
| **Trigger (create)** | Empty form — no fetch |
| **Submit** | `students.create` or `students.update` |
| **Photo/Aadhaar upload** | `students.replacePhoto` / `students.replaceAadhaar` |

### Enrollment Detail

| | |
|---|---|
| **ViewModel** | `EnrollmentDetailViewModel` |
| **Trigger** | `LaunchedEffect(enrollmentId) { viewModel.load(id) }` |
| **API** | `enrollments.get` (flat + payments[]) |
| **Reload after** | Mark complete, cancel, edit installments, exclude from billing, extend subscription, mark/unmark topic, record payment |
| **On-demand** | `loadReceipt(receiptId)` → `receipts.get` |

### Enrollment Wizard

| | |
|---|---|
| **ViewModel** | `EnrollmentWizardViewModel` |
| **Trigger** | `initialize(studentId?)` on screen entry |
| **APIs on init** | `courses.list`; if studentId provided → `students.get` |
| **Step: preview** | User taps preview → `enrollments.preview` |
| **Step: submit** | User confirms → `enrollments.create` |
| **Mid-wizard** | Can navigate to `student_new` to create a student, then return |

### Course Detail

| | |
|---|---|
| **ViewModel** | `CourseDetailViewModel` |
| **Trigger** | `LaunchedEffect(courseId) { viewModel.load(id) }` |
| **APIs** | `courses.get` + `courses.topics.list` (parallel) |
| **Reload** | `reload()` after topic changes |

### Course Form (Create / Edit)

| | |
|---|---|
| **ViewModel** | `CourseFormViewModel` |
| **Trigger (edit)** | `initialize(courseId)` → `courses.get` + `settings.getAll` |
| **Trigger (create)** | `settings.getAll` only |
| **Submit** | `courses.create` or `courses.update` + `courses.topics.bulkSave` |

### Payment Form

| | |
|---|---|
| **ViewModel** | `PaymentFormViewModel` |
| **Trigger** | `initialize(enrollmentId)` → `enrollments.get` |
| **Submit** | `payments.create` |

---

## 6. User-Action Triggers (On-Demand)

These API calls happen only when the user explicitly performs an action.

| User action | API | Screen |
|-------------|-----|--------|
| View student photo | `students.getPhotoBase64` | Student detail |
| View student Aadhaar image | `students.getAadhaarBase64` | Student detail |
| View Aadhaar number (edit) | `students.getAadhaarNumber` | Student form |
| Open receipt PDF | `receipts.get` | Student detail, enrollment detail, payments list, receipts list |
| Open certificate PDF | `certificates.get` | Certificates list |
| Resend receipt email | `receipts.resendEmail` | Receipts list |
| Resend certificate email | `certificates.resendEmail` | Certificates list |
| Export students CSV | `exports.students` | Exports screen |
| Export payments CSV | `exports.payments` | Exports screen |
| Export enrollments CSV | `exports.enrollments` | Exports screen |
| Upload institute logo | `settings.uploadLogo` | Settings screen |
| Upload payment QR code | `settings.uploadQR` | Settings screen |
| Change password | `auth.changePassword` → `auth.me` | Change password screen |
| Enrollment wizard preview | `enrollments.preview` | Enrollment wizard |

---

## 7. Mutation → Reload Pattern

After a successful write operation, detail ViewModels call `reload()` to fetch fresh data.

| Screen | Mutations that trigger reload |
|--------|-------------------------------|
| **Student detail** | Change status, void payment |
| **Enrollment detail** | Mark complete, cancel, edit installments, exclude from billing, extend subscription, mark/unmark topic |
| **Course detail** | Topic bulk save (via form navigate-back) |
| **List screens** | Create/update/delete → `load()` called on navigate back or after dialog confirm |

**Pattern:**
```kotlin
viewModelScope.launch {
    repository.someMutation(input)
    reload()  // re-fetches via get/list
}
```

List screens do not auto-refresh when returning from a detail screen — the user must change a filter or navigate away and back.

---

## 8. Complete Trigger Matrix

| Screen | Initial load | Re-fetch triggers | APIs called |
|--------|-------------|-------------------|-------------|
| **App bootstrap** | App start | — | `auth.me` |
| **Login** | — | Form submit | `auth.login` |
| **Dashboard** | VM init | Period change, retry | `dashboard.summary`, `dashboard.paymentPendingStudents` |
| **Students list** | VM init (paging) | Search/filter change, retry | `students.list` (paginated) |
| **Student detail** | Navigation | After mutation, retry | `students.get`, `payments.list` |
| **Student form** | Navigation (edit) | — | `students.get`, `students.getAadhaarNumber` |
| **Enrollments list** | VM init | Server filter change | `enrollments.list`, `students.list`, `courses.list` |
| **Enrollment detail** | Navigation | After mutation | `enrollments.get` |
| **Enrollment wizard** | Navigation | Preview tap | `courses.list`, `students.get`, `enrollments.preview`, `enrollments.create` |
| **Payments list** | VM init | Server filter change | `payments.list`, `receipts.list` |
| **Payment form** | Navigation | — | `enrollments.get`, `payments.create` |
| **Courses list** | VM init | After create | `courses.list` |
| **Course detail** | Navigation | After mutation | `courses.get`, `courses.topics.list` |
| **Course form** | Navigation | — | `courses.get`, `settings.getAll`, `courses.create/update`, `courses.topics.bulkSave` |
| **Receipts list** | VM init | Filter change | `receipts.list`, `receipts.get` (on open) |
| **Certificates list** | VM init | — | `certificates.list`, `certificates.get` (on open) |
| **Subscriptions list** | VM init | Pending toggle | `subscriptions.list` |
| **Audit log** | VM init | Filter change | `audit.list` |
| **Settings** | VM init | After update | `settings.getAll`, `settings.update`, `settings.uploadLogo/QR` |
| **User management** | VM init | After CRUD | `users.list`, `users.create/update/resetPassword` |
| **Exports** | — | Button tap | `exports.students/payments/enrollments` |
| **Change password** | — | Form submit | `auth.changePassword`, `auth.me` |

---

## 9. What Never Triggers an API Call

| Scenario | Behavior |
|----------|----------|
| App backgrounded / resumed | No re-fetch (state preserved in ViewModel) |
| Bottom tab switch | ViewModel retained if same back-stack entry; no re-fetch |
| Config change (rotation) | `SavedStateHandle` restores filters; ViewModel may survive |
| Pull-to-refresh gesture | Not implemented |
| Periodic background sync | Not implemented |
| Network reconnect | No automatic retry |

---

## Related Docs

- [Storage & Repositories](./03-storage-and-repositories.md) — repository method details
- [Features & Navigation](./05-features-and-navigation.md) — screen-to-route mapping
- [Auth, Roles & Startup](./06-auth-roles-startup.md) — bootstrap and session lifecycle
