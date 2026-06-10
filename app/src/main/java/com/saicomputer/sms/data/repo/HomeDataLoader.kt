package com.saicomputer.sms.data.repo

import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.UserRole
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parallel preload/refresh for splash bootstrap and dashboard pull-to-refresh.
 */
@Singleton
class HomeDataLoader @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val studentsRepository: StudentsRepository
) {
    suspend fun load(
        role: UserRole,
        period: DashboardPeriod = DashboardPeriod.thisMonth
    ): Result<Unit> = runCatching {
        coroutineScope {
            val studentsJob = async { studentsRepository.refreshBaseList() }
            if (role == UserRole.Owner || role == UserRole.Admin) {
                val summaryJob = async { dashboardRepository.refreshSummary(period) }
                val pendingJob = async { dashboardRepository.refreshPendingStudents() }
                summaryJob.await()
                pendingJob.await()
            }
            studentsJob.await()
        }
    }
}
