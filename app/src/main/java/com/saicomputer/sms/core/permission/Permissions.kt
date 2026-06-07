package com.saicomputer.sms.core.permission

import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole

/**
 * Consolidated permission map (v1–v12). Mirrors the backend. Unlisted actions
 * default to ALLOW for any authenticated role. Gates are UX-only; the server is
 * the source of truth.
 */
val PERMISSIONS: Map<String, Set<UserRole>> = mapOf(
    "students.delete" to setOf(UserRole.Owner),
    "students.changeStatus" to setOf(UserRole.Owner, UserRole.Admin),
    "students.getAadhaarNumber" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "students.getAadhaarBase64" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "students.getPhotoBase64" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "students.replacePhoto" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "students.replaceAadhaar" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.create" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.update" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.delete" to setOf(UserRole.Owner),
    "courses.topics.list" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "courses.topics.create" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.topics.update" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.topics.delete" to setOf(UserRole.Owner, UserRole.Admin),
    "courses.topics.bulkSave" to setOf(UserRole.Owner, UserRole.Admin),
    "enrollments.markComplete" to setOf(UserRole.Owner, UserRole.Admin),
    "enrollments.cancel" to setOf(UserRole.Owner),
    "enrollments.editInstallments" to setOf(UserRole.Owner, UserRole.Admin),
    "enrollments.setExcludedFromBilling" to setOf(UserRole.Owner, UserRole.Admin),
    "enrollments.topics.list" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "enrollments.topics.markComplete" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "enrollments.topics.unmark" to setOf(UserRole.Owner, UserRole.Admin),
    "payments.void" to setOf(UserRole.Owner, UserRole.Admin),
    "payments.editBillingMonth" to setOf(UserRole.Owner, UserRole.Admin),
    "subscriptions.list" to setOf(UserRole.Owner, UserRole.Admin, UserRole.Receptionist),
    "subscriptions.extend" to setOf(UserRole.Owner, UserRole.Admin),
    "subscriptions.editEndDate" to setOf(UserRole.Owner, UserRole.Admin),
    "dashboard.summary" to setOf(UserRole.Owner, UserRole.Admin),
    "dashboard.paymentPendingStudents" to setOf(UserRole.Owner, UserRole.Admin),
    "dashboard.subscriptionPendingStudents" to setOf(UserRole.Owner, UserRole.Admin),
    "receipts.list" to setOf(UserRole.Owner, UserRole.Admin),
    "receipts.get" to setOf(UserRole.Owner, UserRole.Admin),
    "receipts.resendEmail" to setOf(UserRole.Owner, UserRole.Admin),
    "certificates.list" to setOf(UserRole.Owner, UserRole.Admin),
    "certificates.get" to setOf(UserRole.Owner, UserRole.Admin),
    "certificates.resendEmail" to setOf(UserRole.Owner, UserRole.Admin),
    "settings.update" to setOf(UserRole.Owner),
    "settings.uploadLogo" to setOf(UserRole.Owner),
    "settings.uploadQR" to setOf(UserRole.Owner),
    "users.create" to setOf(UserRole.Owner),
    "users.update" to setOf(UserRole.Owner),
    "users.resetPassword" to setOf(UserRole.Owner),
    "audit.list" to setOf(UserRole.Owner, UserRole.Admin),
    "exports.students" to setOf(UserRole.Owner),
    "exports.payments" to setOf(UserRole.Owner),
    "exports.enrollments" to setOf(UserRole.Owner),
    // Frontend-only gate for showing the BackdateToggle (no backend route).
    "system.backdate" to setOf(UserRole.Owner)
)

fun can(user: User?, action: String): Boolean {
    if (user == null) return false
    val allowed = PERMISSIONS[action] ?: return true
    return user.role in allowed
}
