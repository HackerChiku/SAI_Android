package com.saicomputer.sms.feature.students

import com.saicomputer.sms.data.model.Student

internal fun applyStudentFilters(students: List<Student>, filters: StudentFilters): List<Student> {
    val query = filters.search.trim().lowercase()
    return students.filter { student ->
        val matchesSearch = query.isBlank() ||
            student.fullName.lowercase().contains(query) ||
            student.phoneNumber.contains(query) ||
            student.studentId.lowercase().contains(query)
        val matchesStatus = filters.status == "All" || student.status.name == filters.status
        val matchesSession = filters.registrationSession == "All" ||
            student.registrationSession.name == filters.registrationSession
        matchesSearch && matchesStatus && matchesSession
    }
}
