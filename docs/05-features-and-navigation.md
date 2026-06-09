# Features & Navigation

[← Back to Index](./README.md)

---

## Navigation Architecture

| Component | File | Role |
|-----------|------|------|
| Routes | `navigation/Screen.kt` | Sealed class with all route strings |
| NavHost | `navigation/SmsNavHost.kt` | Auth gate, route registration, start destination |
| Bottom shell | `navigation/MainShell.kt` | Bottom navigation bar wrapper |
| More menu | `navigation/MoreScreen.kt` | Secondary feature links |

---

## All Routes

| Route | Screen | Bottom bar tab |
|-------|--------|----------------|
| `login` | `LoginScreen` | — |
| `change_password` | `ChangePasswordScreen` | — |
| `profile` | `UserProfileScreen` | More (highlight) |
| `dashboard` | `DashboardScreen` | Dashboard (Owner/Admin) |
| `students` | `StudentsListScreen` | Students |
| `student_new` | `StudentFormScreen` (create) | Students |
| `student/{id}` | `StudentDetailScreen` | Students |
| `student/{id}/edit` | `StudentFormScreen` (edit) | Students |
| `search` | `StudentsListScreen` (duplicate) | — **orphan, no nav entry** |
| `more` | `MoreScreen` | More |
| `courses` | `CoursesListScreen` | More |
| `course_new` | `CourseFormScreen` | More |
| `course/{id}` | `CourseDetailScreen` | More |
| `course/{id}/edit` | `CourseFormScreen` | More |
| `enrollments` | `EnrollmentsListScreen` | Enroll |
| `enrollment_new?studentId={studentId}` | `EnrollmentWizardScreen` | Enroll / Students |
| `enrollment/{id}` | `EnrollmentDetailScreen` | Enroll |
| `payment_new?enrollmentId={enrollmentId}` | `PaymentFormScreen` | Payments |
| `payments` | `PaymentsListScreen` | Payments (Owner/Admin) |
| `subscriptions` | `SubscriptionsListScreen` | — **orphan, no menu link** |
| `receipts` | `ReceiptsListScreen` | More |
| `certificates` | `CertificatesListScreen` | More |
| `audit` | `AuditScreen` | More |
| `settings` | `InstituteSettingsScreen` | More |
| `settings/users` | `UserManagementScreen` | More |
| `exports` | `ExportsScreen` | More |

---

## Navigation Tree

```
[SmsNavHost]
│
├── Auth (no bottom bar)
│   ├── login
│   └── change_password
│
└── MainShell (bottom bar when logged in)
    │
    ├── Dashboard tab ── dashboard
    │   └── → student/{id}
    │
    ├── Students tab ── students
    │   ├── → student_new → student/{id}
    │   ├── → student/{id}
    │   │   ├── → student/{id}/edit
    │   │   ├── → enrollment_new?studentId={id} → enrollment/{id}
    │   │   └── → payment_new?enrollmentId={id}
    │   └── (search — registered, unused)
    │
    ├── Enroll tab ── enrollments
    │   ├── → enrollment_new?studentId= → enrollment/{id}
    │   │   └── → student_new (create student mid-wizard)
    │   └── → enrollment/{id}
    │       └── → payment_new?enrollmentId={id}
    │
    ├── Payments tab ── payments
    │   └── (void/edit billing in-place)
    │
    └── More tab ── more
        ├── → courses → course_new | course/{id} → course/{id}/edit
        ├── → receipts
        ├── → certificates
        ├── → audit
        ├── → settings → settings/users
        ├── → exports
        ├── → profile → change_password
        └── (subscriptions — registered, no menu link)
```

---

## Start Destinations (Role-Based)

| Condition | Start route |
|-----------|-------------|
| No user | `login` |
| `mustChangePassword` | `change_password` |
| `Receptionist` | `students` |
| Owner / Admin | `dashboard` |

---

## Bottom Navigation

Built in `MainShell.buildBottomNavItems()`:

| Tab | Route | Visible when |
|-----|-------|--------------|
| Dashboard | `dashboard` | Not Receptionist + `can("dashboard.summary")` |
| Students | `students` | Always |
| Enroll | `enrollments` | Always |
| Payments | `payments` | `can("receipts.list")` (Owner/Admin) |
| More | `more` | Always |

---

## Feature Areas

### Authentication & Profile

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `LoginScreen` | `LoginViewModel` | Email/password login → role-based redirect |
| `ChangePasswordScreen` | `ChangePasswordViewModel` | Forced or voluntary password change |
| `UserProfileScreen` | `ThemeViewModel` | View account, change password, theme selector, logout |

**Files:** `feature/auth/`

---

### Dashboard (Owner/Admin)

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `DashboardScreen` | `DashboardViewModel` | Period selector, summary cards (students, revenue, enrollments), charts, payment-pending list, tap student → detail |

**Repo:** `DashboardRepository`  
**Files:** `feature/dashboard/`

---

### Students

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `StudentsListScreen` | `StudentsListViewModel` | Search, filter (status, session), sort, paginated list, add student |
| `StudentFormScreen` | `StudentFormViewModel` | Create/edit student (demographics, contact, Aadhaar, photo); Owner backdate toggle |
| `StudentDetailScreen` | `StudentDetailViewModel`, `StudentDocumentsViewModel` | View profile, enrollments, payments; edit; change status; view/replace photo & Aadhaar; new enrollment; record payment; call/WhatsApp; void payments |

**Repo:** `StudentsRepository`  
**Paging:** `data/repo/paging/StudentsPagingSource.kt`  
**Files:** `feature/students/`

---

### Courses

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `CoursesListScreen` | `CoursesListViewModel` | List courses; create (Owner/Admin) |
| `CourseDetailScreen` | `CourseDetailViewModel` | View course, topics, edit (Owner/Admin) |
| `CourseFormScreen` | `CourseFormViewModel` | Create/edit course + bulk topic save |

**Repos:** `CoursesRepository`, `TopicsRepository`  
**Files:** `feature/courses/`

---

### Enrollments

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `EnrollmentsListScreen` | `EnrollmentsListViewModel` | Search/filter/sort, list all enrollments, new enrollment |
| `EnrollmentWizardScreen` | `EnrollmentWizardViewModel` | Multi-step: pick student, course, billing type, preview, create; backdate (Owner) |
| `EnrollmentDetailScreen` | `EnrollmentDetailViewModel` | View installments/subscription, payments, topic progress; mark complete, cancel, edit installments, exclude from billing, extend subscription, mark/unmark topics, record payment |

**Repos:** `EnrollmentsRepository`, `SubscriptionsRepository`, `TopicsRepository`  
**Files:** `feature/enrollments/`

---

### Payments

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `PaymentsListScreen` | `PaymentsListViewModel` | Search/filter/sort payments; void; edit billing month (Owner/Admin); open receipt |
| `PaymentFormScreen` | `PaymentFormViewModel` | Record installment or subscription payment; backdate (Owner) |

**Repo:** `PaymentsRepository`  
**Files:** `feature/payments/`

---

### Subscriptions

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `SubscriptionsListScreen` | `SubscriptionsListViewModel` | List subscription enrollments → open enrollment detail |

**Note:** Route exists but no UI navigates to it. Subscription management happens on `EnrollmentDetailScreen`.

**Files:** `feature/subscriptions/`

---

### Receipts & Certificates

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `ReceiptsListScreen` | `ReceiptsViewModel` | Search/filter/sort; view PDF; resend email (Owner/Admin) |
| `CertificatesListScreen` | `CertificatesViewModel` | List; view PDF; resend email (Owner/Admin) |

**Repos:** `ReceiptsRepository`, `CertificatesRepository`  
**Files:** `feature/receipts/`, `feature/certificates/`

---

### Audit Log

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `AuditScreen` | `AuditViewModel` | Filter by date range, action type, user; browse entries (Owner/Admin) |

**Repo:** `AuditRepository`  
**Files:** `feature/audit/`

---

### Settings & User Management

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `InstituteSettingsScreen` | `SettingsViewModel` | Edit institute settings rows, upload logo/QR (Owner) |
| `UserManagementScreen` | `UserManagementViewModel` | List users, create, edit role, reset password (Owner) |

**Repos:** `SettingsRepository`, `UsersRepository`  
**Files:** `feature/settings/`

---

### Exports

| Screen | ViewModel | Key actions |
|--------|-----------|-------------|
| `ExportsScreen` | `ExportsViewModel` | Export students, payments, enrollments to Download Manager (Owner) |

**Repo:** `ExportsRepository`  
**Files:** `feature/exports/`

---

## More Menu Items

From `MoreScreen.kt` — items filtered by permission:

| Label | Route | Permission gate |
|-------|-------|-----------------|
| Courses | `courses` | `courses.list` (defaults allow) |
| Receipts | `receipts` | `receipts.list` |
| Certificates | `certificates` | `certificates.list` |
| Audit Log | `audit` | `audit.list` |
| Settings | `settings` | `settings.update` |
| Exports | `exports` | `exports.students` |
| User Management | `settings/users` | `users.create` |

---

## ViewModel Index (24 total)

| ViewModel | Feature area |
|-----------|-------------|
| `AppViewModel` | Session bootstrap, logout |
| `LoginViewModel` | Auth |
| `ChangePasswordViewModel` | Auth |
| `ThemeViewModel` | Profile theme |
| `DashboardViewModel` | Dashboard |
| `StudentsListViewModel` | Students list |
| `StudentFormViewModel` | Student create/edit |
| `StudentDetailViewModel` | Student detail |
| `StudentDocumentsViewModel` | Photo/Aadhaar viewers |
| `CoursesListViewModel` | Courses list |
| `CourseFormViewModel` | Course create/edit |
| `CourseDetailViewModel` | Course detail |
| `EnrollmentsListViewModel` | Enrollments list |
| `EnrollmentWizardViewModel` | Enrollment wizard |
| `EnrollmentDetailViewModel` | Enrollment detail |
| `PaymentsListViewModel` | Payments list |
| `PaymentFormViewModel` | Payment form |
| `SubscriptionsListViewModel` | Subscriptions list |
| `ReceiptsViewModel` | Receipts list |
| `CertificatesViewModel` | Certificates list |
| `AuditViewModel` | Audit log |
| `SettingsViewModel` | Institute settings |
| `UserManagementViewModel` | User management |
| `ExportsViewModel` | Data exports |

---

## Related Docs

- [Data Fetch Triggers](./04-data-fetch-triggers.md) — when each screen loads data
- [Auth, Roles & Startup](./06-auth-roles-startup.md) — permissions and role gating
