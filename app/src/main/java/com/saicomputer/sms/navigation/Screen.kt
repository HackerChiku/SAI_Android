package com.saicomputer.sms.navigation

/** Sealed route definitions for Navigation-Compose. */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object ChangePassword : Screen("change_password")
    data object Dashboard : Screen("dashboard")

    data object Students : Screen("students")
    data object StudentNew : Screen("student_new")
    data object StudentDetail : Screen("student/{id}") {
        fun create(id: String) = "student/$id"
    }
    data object StudentEdit : Screen("student/{id}/edit") {
        fun create(id: String) = "student/$id/edit"
    }
    data object Search : Screen("search")
    data object More : Screen("more")

    data object Courses : Screen("courses")
    data object CourseNew : Screen("course_new")
    data object CourseDetail : Screen("course/{id}") {
        fun create(id: String) = "course/$id"
    }
    data object CourseEdit : Screen("course/{id}/edit") {
        fun create(id: String) = "course/$id/edit"
    }

    data object EnrollmentNew : Screen("enrollment_new?studentId={studentId}") {
        fun create(studentId: String? = null) =
            if (studentId != null) "enrollment_new?studentId=$studentId" else "enrollment_new?studentId="
    }
    data object EnrollmentDetail : Screen("enrollment/{id}") {
        fun create(id: String) = "enrollment/$id"
    }

    data object PaymentNew : Screen("payment_new?enrollmentId={enrollmentId}") {
        fun create(enrollmentId: String) = "payment_new?enrollmentId=$enrollmentId"
    }
    data object Payments : Screen("payments")

    data object Subscriptions : Screen("subscriptions")
    data object Receipts : Screen("receipts")
    data object Certificates : Screen("certificates")
    data object Audit : Screen("audit")
    data object Settings : Screen("settings")
    data object Users : Screen("settings/users")
    data object Exports : Screen("exports")

    companion object {
        const val ARG_ID = "id"
        const val ARG_STUDENT_ID = "studentId"
        const val ARG_ENROLLMENT_ID = "enrollmentId"
    }
}
