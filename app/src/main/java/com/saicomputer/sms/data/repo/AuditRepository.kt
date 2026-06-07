package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.AuditListFilters
import com.saicomputer.sms.data.dto.AuditListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun list(filters: AuditListFilters): AuditListResponse =
        api.call("audit.list", filters)
}
