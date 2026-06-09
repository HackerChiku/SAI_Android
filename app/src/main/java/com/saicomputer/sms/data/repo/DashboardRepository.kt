package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import com.saicomputer.sms.data.dto.DashboardSummaryInput
import com.saicomputer.sms.data.model.DashboardPendingStudent
import com.saicomputer.sms.data.model.DashboardPendingStudentsResponse
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val summaryCache = SessionCache<Map<DashboardPeriod, DashboardSummaryResponse>>(registry)
    private val pendingCache = SessionCache<List<DashboardPendingStudent>>(registry)

    val summaryFlow: StateFlow<com.saicomputer.sms.core.session.Cached<Map<DashboardPeriod, DashboardSummaryResponse>>?> =
        summaryCache.flow
    val pendingFlow: StateFlow<com.saicomputer.sms.core.session.Cached<List<DashboardPendingStudent>>?> =
        pendingCache.flow

    fun getCachedSummary(period: DashboardPeriod): DashboardSummaryResponse? =
        summaryCache.value?.get(period)

    fun getCachedPendingStudents(): List<DashboardPendingStudent>? =
        pendingCache.value

    fun isCacheFresh(): Boolean = summaryCache.isFresh() && pendingCache.isFresh()

    suspend fun summary(period: DashboardPeriod): DashboardSummaryResponse =
        api.call("dashboard.summary", DashboardSummaryInput(period))

    suspend fun paymentPendingStudents(): List<DashboardPendingStudent> =
        api.call<DashboardPendingStudentsResponse>("dashboard.paymentPendingStudents").students

    suspend fun refreshSummary(period: DashboardPeriod): DashboardSummaryResponse {
        val fetched = summary(period)
        val current = summaryCache.value?.toMutableMap() ?: mutableMapOf()
        current[period] = fetched
        summaryCache.put(current)
        return fetched
    }

    suspend fun refreshPendingStudents(): List<DashboardPendingStudent> {
        val fetched = paymentPendingStudents()
        pendingCache.put(fetched)
        return fetched
    }
}
