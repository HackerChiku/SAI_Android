package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.DashboardSummaryInput
import com.saicomputer.sms.data.model.DashboardPendingStudentsResponse
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun summary(period: DashboardPeriod): DashboardSummaryResponse =
        api.call("dashboard.summary", DashboardSummaryInput(period))

    suspend fun paymentPendingStudents(): DashboardPendingStudentsResponse =
        api.call("dashboard.paymentPendingStudents")
}
