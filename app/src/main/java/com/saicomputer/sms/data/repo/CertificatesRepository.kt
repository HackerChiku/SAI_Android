package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import com.saicomputer.sms.data.dto.CertificateGetResponse
import com.saicomputer.sms.data.dto.CertificateListFilters
import com.saicomputer.sms.data.dto.CertificateListResponse
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.ResendEmailInput
import com.saicomputer.sms.data.model.CertificateListItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class CertificateIdPayload(val certificateId: String)

@Singleton
class CertificatesRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val baseListCache = SessionCache<List<CertificateListItem>>(registry)

    val baseListFlow: StateFlow<Cached<List<CertificateListItem>>?> = baseListCache.flow

    fun getCachedBaseList(): List<CertificateListItem>? = baseListCache.value

    fun isBaseListFresh(): Boolean = baseListCache.isFresh()

    suspend fun list(filters: CertificateListFilters): CertificateListResponse =
        api.call("certificates.list", filters)

    fun cacheBaseList(rows: List<CertificateListItem>) {
        baseListCache.put(rows)
    }

    suspend fun refreshBaseList(): List<CertificateListItem> {
        val rows = list(CertificateListFilters()).certificates
        baseListCache.put(rows)
        return rows
    }

    suspend fun get(certificateId: String): CertificateGetResponse =
        api.call("certificates.get", CertificateIdPayload(certificateId))

    suspend fun resendEmail(certificateId: String, recipientEmail: String?): OkResponse =
        api.call(
            "certificates.resendEmail",
            ResendEmailInput(certificateId = certificateId, recipientEmail = recipientEmail)
        )
}
