# API & Networking

[← Back to Index](./README.md)

---

## Overview

The app talks to a **single Google Apps Script `/exec` endpoint** using an RPC-style envelope. There are no per-resource Retrofit interfaces (`@GET`, `@POST` paths). Every call is:

```
POST {WEB_APP_URL}
Body: { "action": "domain.verb", "sessionToken": "...", "payload": { ... } }
```

---

## Configuration

| Setting | Source |
|---------|--------|
| Runtime URL | `BuildConfig.WEB_APP_URL` |
| Config file | `local.properties` → `WEB_APP_URL=https://script.google.com/macros/s/.../exec` |
| Retrofit base | `https://script.google.com/` (placeholder; every call passes absolute `@Url`) |
| Injection | `@WebAppUrl` qualifier in `di/Qualifiers.kt` |

---

## Core Network Files

| File | Role |
|------|------|
| `core/network/ApiService.kt` | Retrofit interface — single `call(@Url, @Body)` method |
| `core/network/ApiClient.kt` | Builds envelope, POSTs, parses response, handles auth errors |
| `core/network/ApiEnvelope.kt` | `ApiRequest` data class |
| `core/network/ApiException.kt` | Error codes + user-friendly messages |
| `di/NetworkModule.kt` | OkHttp, Retrofit, Json, logging interceptor |

---

## Request Envelope

```json
{
  "action": "students.list",
  "sessionToken": "<token-or-null>",
  "payload": {
    "search": "",
    "status": "Active",
    "limit": 50,
    "offset": 0
  }
}
```

Built by `ApiClient`:

```kotlin
val request = ApiRequest(action, session.token, payload)
api.call(webAppUrl, request)
```

- `sessionToken` is read from `SessionManager.token` on every call
- Token is **not** sent as an HTTP header

---

## Response Envelope

**Success:**
```json
{ "ok": true, "data": { ... } }
```

**Failure:**
```json
{
  "ok": false,
  "error": {
    "code": "UNAUTHENTICATED",
    "message": "Session expired",
    "details": null
  }
}
```

### Error Handling

| Error code | App behavior |
|------------|--------------|
| `UNAUTHENTICATED` | `SessionManager.clear()` → user routed to Login |
| `NETWORK` | IOException wrapped — shown as network error |
| Other codes | `ApiException` thrown with friendly message |

---

## OkHttp Configuration

| Setting | Value |
|---------|-------|
| Connect/read/write timeout | 30 seconds |
| Redirects | Enabled (Apps Script 302 → `script.googleusercontent.com`) |
| Logging | BODY in DEBUG, NONE in release |

---

## All API Actions (56 total)

Every action is `POST` to the same `/exec` URL.

### Auth (4)

| Action | Payload | Repository method |
|--------|---------|-------------------|
| `auth.login` | `LoginInput(email, password)` | `AuthRepository.login` |
| `auth.me` | empty | `AuthRepository.me` |
| `auth.changePassword` | `ChangePasswordInput` | `AuthRepository.changePassword` |
| `auth.logout` | empty | `AuthRepository.logout` |

### Students (10)

| Action | Payload | Notes |
|--------|---------|-------|
| `students.list` | `StudentListFilters` | Paginated: search, status, session, limit, offset |
| `students.get` | `{ StudentID }` | Returns flat student + `enrollments[]` array |
| `students.create` | `StudentCreateInput` | |
| `students.update` | `StudentUpdateInput` | |
| `students.changeStatus` | `ChangeStudentStatusInput` | |
| `students.getAadhaarNumber` | `{ StudentID }` | Audit-logged; edit form only |
| `students.getPhotoBase64` | `{ studentId }` | camelCase ID |
| `students.getAadhaarBase64` | `{ studentId }` | camelCase ID |
| `students.replacePhoto` | `FileUploadInput` | Base64 upload |
| `students.replaceAadhaar` | `FileUploadInput` | Base64 upload |

### Enrollments (8)

| Action | Payload | Notes |
|--------|---------|-------|
| `enrollments.list` | `EnrollmentListFilters` | |
| `enrollments.get` | `{ EnrollmentID }` | Flat enrollment + `payments[]` |
| `enrollments.preview` | Preview input | Wizard fee/installment preview |
| `enrollments.create` | Installment or subscription variant | |
| `enrollments.markComplete` | Input | |
| `enrollments.cancel` | Input | Owner only |
| `enrollments.editInstallments` | Input | |
| `enrollments.setExcludedFromBilling` | Input | |

### Enrollment Topics (3)

| Action | Repository |
|--------|------------|
| `enrollments.topics.list` | `TopicsRepository.listForEnrollment` |
| `enrollments.topics.markComplete` | `TopicsRepository.markComplete` |
| `enrollments.topics.unmark` | `TopicsRepository.unmark` |

### Courses (7)

| Action | Notes |
|--------|-------|
| `courses.list` | |
| `courses.get` | Uses `callRaw` for flexible parsing |
| `courses.create` | |
| `courses.update` | |
| `courses.delete` | Owner only |
| `courses.topics.list` | camelCase `courseId` |
| `courses.topics.bulkSave` | |

### Payments (4)

| Action | Notes |
|--------|-------|
| `payments.list` | Also called from `StudentsRepository.get` |
| `payments.create` | Installment or subscription variant |
| `payments.void` | Immutable — void, don't delete |
| `payments.editBillingMonth` | |

### Dashboard (2)

| Action | Payload |
|--------|---------|
| `dashboard.summary` | `{ period: "thisMonth" }` |
| `dashboard.paymentPendingStudents` | empty |

### Subscriptions (3)

| Action | Notes |
|--------|-------|
| `subscriptions.list` | `pendingOnly` filter |
| `subscriptions.extend` | |
| `subscriptions.editEndDate` | |

### Receipts (3)

| Action | Notes |
|--------|-------|
| `receipts.list` | Also called internally by `PaymentsRepository.list` |
| `receipts.get` | PDF detail |
| `receipts.resendEmail` | |

### Certificates (3)

| Action | Notes |
|--------|-------|
| `certificates.list` | |
| `certificates.get` | PDF detail |
| `certificates.resendEmail` | |

### Settings (4)

| Action | Notes |
|--------|-------|
| `settings.getAll` | |
| `settings.update` | Owner only |
| `settings.uploadLogo` | Base64 |
| `settings.uploadQR` | Base64 |

### Users (4)

| Action | Notes |
|--------|-------|
| `users.list` | |
| `users.create` | Owner only |
| `users.update` | Owner only |
| `users.resetPassword` | Owner only |

### Audit (1)

| Action | Notes |
|--------|-------|
| `audit.list` | Filterable by action, date range, user |

### Exports (3)

| Action | Returns |
|--------|---------|
| `exports.students` | CSV metadata |
| `exports.payments` | CSV metadata |
| `exports.enrollments` | CSV metadata |

---

## ApiClient Methods

```kotlin
// No payload
suspend inline fun <reified T> call(action: String): T

// Typed payload
suspend inline fun <reified T, reified P> call(action: String, payload: P): T

// Raw JsonElement (for flexible parsing)
suspend fun callRaw(action: String, payload: JsonElement): JsonElement
```

`callRaw` is used when the response shape doesn't map cleanly to a single DTO:
- `students.get` — flat student + nested `enrollments[]`
- `enrollments.get` — flat enrollment + nested `payments[]`
- `courses.get` — flexible course detail
- `payments.list` — rows with optional inline name fields

---

## ID Naming Conventions

The backend uses mixed casing depending on the endpoint:

| Convention | Used by |
|------------|---------|
| PascalCase `StudentID` | `students.get`, `students.getAadhaarNumber`, payment filters |
| camelCase `studentId` | File endpoints (`getPhotoBase64`, `getAadhaarBase64`) |
| PascalCase `EnrollmentID` | Enrollment endpoints |
| camelCase `courseId` | Course topic endpoints |

---

## Related Docs

- [Storage & Repositories](./03-storage-and-repositories.md) — how repositories wrap these actions
- [Data Fetch Triggers](./04-data-fetch-triggers.md) — when each action is called
