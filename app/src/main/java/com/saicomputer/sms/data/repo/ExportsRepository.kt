package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.ExportResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportsRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun students(): ExportResponse = api.call("exports.students")
    suspend fun payments(): ExportResponse = api.call("exports.payments")
    suspend fun enrollments(): ExportResponse = api.call("exports.enrollments")
}
