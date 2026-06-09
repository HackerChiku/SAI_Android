package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AuditLogEntry(
    @SerialName("LogID") val logId: String,
    @SerialName("Timestamp") val timestamp: String = "",
    @SerialName("UserID") val userId: String = "",
    @SerialName("Action") val action: String,
    @SerialName("Entity") val entity: String? = null,
    @SerialName("EntityID") val entityId: String? = null,
    @SerialName("OldValues") val oldValues: JsonElement? = null,
    @SerialName("NewValues") val newValues: JsonElement? = null,
    @SerialName("IPAddress") val ipAddress: String? = null,
    @SerialName("UserAgent") val userAgent: String? = null
) {
    /** Short human-readable target descriptor for list rows. */
    val target: String?
        get() = when {
            entity == null -> null
            entityId == null -> entity
            else -> "$entity • $entityId"
        }

    val actionLabel: String get() = humanizeAuditAction(action)

    val subjectLine: String
        get() = entityId?.takeIf { it.isNotBlank() } ?: target ?: action

    val performerLine: String get() = "By $userId"

    val detailLine: String?
        get() = extractAuditDetail(this)
}

private fun humanizeAuditAction(action: String): String {
    val known = mapOf(
        "students.getAadhaarNumber" to "View Aadhaar",
        "students.getAadhaarBase64" to "View Aadhaar",
        "payments.void" to "Void Payment",
        "enrollments.create" to "Create Enrollment",
        "students.changeStatus" to "Change Status",
        "enrollments.editInstallments" to "Edit Installments",
        "users.resetPassword" to "Reset Password"
    )
    known[action]?.let { return it }

    val segments = action.split(".")
    if (segments.size >= 2) {
        val resource = segments[segments.size - 2]
            .removeSuffix("s")
            .replaceFirstChar { it.uppercase() }
        val verb = segments.last()
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
        return "$verb $resource"
    }
    return action.replace(".", " ")
        .split(" ")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private fun extractAuditDetail(entry: AuditLogEntry): String? {
    entry.newValues?.jsonObject?.let { obj ->
        for (key in listOf("reason", "Reason", "notes", "Notes", "message", "description")) {
            val value = obj[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            if (value != null) {
                return if (key.equals("reason", ignoreCase = true)) "Reason: $value" else value
            }
        }
        val courseName = obj["courseName"]?.jsonPrimitive?.contentOrNull
            ?: obj["CourseName"]?.jsonPrimitive?.contentOrNull
        val courseId = obj["courseId"]?.jsonPrimitive?.contentOrNull
            ?: obj["CourseID"]?.jsonPrimitive?.contentOrNull
            ?: entry.entityId
        if (courseName != null) {
            return if (courseId != null) "Course: $courseName ($courseId)" else "Course: $courseName"
        }
    }

    return when {
        entry.action.contains("aadhaar", ignoreCase = true) -> "Aadhaar document viewed"
        entry.action.contains("password", ignoreCase = true) -> "Password reset initiated"
        entry.action.contains("installment", ignoreCase = true) -> "Installment schedule updated"
        else -> null
    }
}
