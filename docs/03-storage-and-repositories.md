# Storage & Repositories

[← Back to Index](./README.md)

---

## Local Storage Summary

The app has **no Room database, no DataStore, and no disk-backed entity cache**. All business data lives on the Google Apps Script backend.

### What IS Persisted

| Store | File / Key | Contents | File |
|-------|------------|----------|------|
| **Encrypted prefs** | `sms_secure_prefs` → `session_token` | Auth session token | `di/StorageModule.kt` |
| **Plain prefs** | `sms_app_prefs` → `theme_mode` | Light/dark/system theme | `core/theme/ThemePreferences.kt` |

### What Is In-Memory Only

| Component | Contents | Lifetime |
|-----------|----------|----------|
| `SessionManager` | Token (volatile mirror) + `currentUser` StateFlow | App process; token restored from encrypted prefs on start |
| `SettingsRepository` | Settings list `StateFlow` | Until `refresh()` or app kill |
| ViewModels | UI state, filters, list buffers | ViewModel scope |
| Paging 3 | Student list pages | `cachedIn(viewModelScope)` — not persistent |
| `SavedStateHandle` | Search/filter values (students, dashboard period) | Survives config changes, not app kill |

---

## Domain Model (Server-Side Relationships)

Since there is no local DB, these reflect the **backend data model** as Kotlin types in `data/model/`:

```
User (staff accounts)
  └── performs actions → AuditLogEntry

Student (1) ──< Enrollment (N) >── (1) Course
                    │
                    ├──< Installment (N)       [installment billing]
                    ├──< EnrollmentTopic (N)   [course progress]
                    ├──< Payment (N)
                    │       └── (0..1) Receipt
                    └── (0..1) Certificate

Course (1) ──< CourseTopic (N)

Payment ──> Enrollment (required)
        ──> Installment (optional)
        ──> Receipt (optional, via receiptId)

SettingEntry[] (key-value institute config)
```

### Entity Files

| Entity | File | Key ID field |
|--------|------|--------------|
| `Student` | `data/model/Student.kt` | `StudentID` |
| `Course` | `data/model/Course.kt` | `CourseID` |
| `Enrollment` | `data/model/Enrollment.kt` | `EnrollmentID` |
| `Installment` | nested in `Enrollment.kt` | `InstallmentID` |
| `Payment` | `data/model/Payment.kt` | `PaymentID` |
| `ReceiptListItem` / `ReceiptDetail` | `data/model/Files.kt` | `receiptId` |
| `CertificateListItem` / `CertificateDetail` | `data/model/Files.kt` | `certificateId` |
| `CourseTopic` / `EnrollmentTopic` | `data/model/Topic.kt` | topic IDs |
| `SubscriptionListItem` | `data/model/Subscription.kt` | subscription view |
| `User` | `data/model/User.kt` | `UserID` |
| `SettingEntry` | `data/model/Settings.kt` | `Key` |
| `AuditLogEntry` | `data/model/Audit.kt` | `LogID` |
| Dashboard aggregates | `data/model/Dashboard.kt` | read-only summaries |

---

## All Repositories (14)

All repositories are `@Singleton`, inject `ApiClient`, and follow a **network-only** pattern unless noted.

---

### AuthRepository

**File:** `data/repo/AuthRepository.kt`

| Method | API | Local storage effect |
|--------|-----|---------------------|
| `login(email, password)` | `auth.login` | Writes token + user to `SessionManager` |
| `me()` | `auth.me` | Updates in-memory user |
| `changePassword(current, new)` | `auth.changePassword` → `auth.me` | Refreshes user (clears `mustChangePassword`) |
| `logout()` | `auth.logout` (best-effort) | Clears session |

**Cache invalidation:** Logout and `UNAUTHENTICATED` clear session entirely.

---

### StudentsRepository

**File:** `data/repo/StudentsRepository.kt`

| Method | API(s) |
|--------|--------|
| `list(filters)` | `students.list` |
| `get(studentId)` | `students.get` + `payments.list` (**2 calls**) |
| `create(input)` | `students.create` |
| `update(input)` | `students.update` |
| `changeStatus(input)` | `students.changeStatus` |
| `getAadhaarNumber(studentId)` | `students.getAadhaarNumber` |
| `getPhotoBase64(studentId)` | `students.getPhotoBase64` |
| `getAadhaarBase64(studentId)` | `students.getAadhaarBase64` |
| `replacePhoto(input)` | `students.replacePhoto` |
| `replaceAadhaar(input)` | `students.replaceAadhaar` |

**Composite fetch — `get()`:**
1. `students.get` via `callRaw` → parses flat student + `enrollments[]`
2. `payments.list` with `StudentID` filter → attaches payment rows

**No local caching.** Aadhaar/photo fetches are on-demand only.

---

### EnrollmentsRepository

**File:** `data/repo/EnrollmentsRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `enrollments.list` |
| `get(enrollmentId)` | `enrollments.get` (via `callRaw` — flat + `payments[]`) |
| `preview(input)` | `enrollments.preview` |
| `create(input)` | `enrollments.create` |
| `markComplete(input)` | `enrollments.markComplete` |
| `cancel(input)` | `enrollments.cancel` |
| `editInstallments(input)` | `enrollments.editInstallments` |
| `setExcludedFromBilling(input)` | `enrollments.setExcludedFromBilling` |

---

### TopicsRepository

**File:** `data/repo/TopicsRepository.kt`

| Method | API |
|--------|-----|
| `listForEnrollment(enrollmentId)` | `enrollments.topics.list` |
| `markComplete(input)` | `enrollments.topics.markComplete` |
| `unmark(input)` | `enrollments.topics.unmark` |

---

### PaymentsRepository

**File:** `data/repo/PaymentsRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `payments.list` + **`receipts.list`** (**2 calls**) |
| `create(input)` | `payments.create` |
| `void(input)` | `payments.void` |
| `editBillingMonth(input)` | `payments.editBillingMonth` |

**Composite fetch — `list()`:**
1. `payments.list` via `callRaw` → parses payment rows
2. `receipts.list` (pageSize=500) → joins student names from receipts when missing in payment row

---

### DashboardRepository

**File:** `data/repo/DashboardRepository.kt`

| Method | API |
|--------|-----|
| `summary(period)` | `dashboard.summary` |
| `paymentPendingStudents()` | `dashboard.paymentPendingStudents` |

**No local cache.** Each dashboard visit fetches fresh data.

---

### CoursesRepository

**File:** `data/repo/CoursesRepository.kt`

| Method | API |
|--------|-----|
| `list()` | `courses.list` |
| `get(courseId)` | `courses.get` (via `callRaw`) |
| `create(input)` | `courses.create` |
| `update(input)` | `courses.update` |
| `delete(courseId)` | `courses.delete` |
| `listTopics(courseId)` | `courses.topics.list` |
| `bulkSaveTopics(input)` | `courses.topics.bulkSave` |

---

### SubscriptionsRepository

**File:** `data/repo/SubscriptionsRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `subscriptions.list` |
| `extend(input)` | `subscriptions.extend` |
| `editEndDate(input)` | `subscriptions.editEndDate` |

---

### ReceiptsRepository

**File:** `data/repo/ReceiptsRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `receipts.list` |
| `get(receiptId)` | `receipts.get` |
| `resendEmail(receiptId, email)` | `receipts.resendEmail` |

---

### CertificatesRepository

**File:** `data/repo/CertificatesRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `certificates.list` |
| `get(certificateId)` | `certificates.get` |
| `resendEmail(certificateId, email)` | `certificates.resendEmail` |

---

### SettingsRepository

**File:** `data/repo/SettingsRepository.kt`

| Method | API | Local storage |
|--------|-----|---------------|
| `refresh()` | `settings.getAll` | Updates `_settings` StateFlow |
| `update(input)` | `settings.update` → `refresh()` | In-memory cache |
| `uploadLogo(input)` | `settings.uploadLogo` | — |
| `uploadQr(input)` | `settings.uploadQR` | — |

**Only repository with in-memory cache.** `settings` StateFlow holds the list until `refresh()` is called.

---

### UsersRepository

**File:** `data/repo/UsersRepository.kt`

| Method | API |
|--------|-----|
| `list()` | `users.list` |
| `create(input)` | `users.create` |
| `update(input)` | `users.update` |
| `resetPassword(userId, password)` | `users.resetPassword` |

---

### AuditRepository

**File:** `data/repo/AuditRepository.kt`

| Method | API |
|--------|-----|
| `list(filters)` | `audit.list` |

---

### ExportsRepository

**File:** `data/repo/ExportsRepository.kt`

| Method | API |
|--------|-----|
| `students()` | `exports.students` |
| `payments()` | `exports.payments` |
| `enrollments()` | `exports.enrollments` |

All exports are on-demand, user-triggered (button tap).

---

### Paging Helper

**File:** `data/repo/paging/StudentsPagingSource.kt`

Not a repository, but the students list data path:
- Wraps `StudentsRepository.list` with offset-based Paging 3
- `pageSize = 50`
- Used by `StudentsListViewModel` with `cachedIn(viewModelScope)`

---

## Data Flow Patterns

| Pattern | Where used |
|---------|------------|
| **Network-only** | All repositories except settings in-memory flow |
| **Network-first, reload on mutation** | Detail screens call `reload()` after void/cancel/status change |
| **Hybrid server + client filtering** | Enrollments, Payments, Receipts lists — server fetch once, client search/sort |
| **Composite multi-call** | `StudentsRepository.get`, `PaymentsRepository.list`, `EnrollmentsListViewModel` (parallel enrollments + students + courses), `CourseDetailViewModel` (course + topics) |
| **Paging 3 network paging** | Students list only |
| **On-demand fetch** | Photo/Aadhaar base64, receipt/certificate PDF, Aadhaar number in edit form |
| **In-memory session cache** | `SessionManager` user/token; `SettingsRepository.settings` |

### No Pull-to-Refresh

There is no `SwipeRefresh` in the codebase. Refresh happens via:
- ViewModel `init { load() }`
- `LaunchedEffect(id) { viewModel.load(id) }` on detail screens
- Filter/sort changes
- Error-state retry buttons
- Post-mutation `reload()`

---

## Related Docs

- [API & Networking](./02-api-and-networking.md) — all API action definitions
- [Data Fetch Triggers](./04-data-fetch-triggers.md) — when each repository method is called
