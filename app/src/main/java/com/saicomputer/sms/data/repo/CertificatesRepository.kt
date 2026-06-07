package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.CertificateGetResponse
import com.saicomputer.sms.data.dto.CertificateListFilters
import com.saicomputer.sms.data.dto.CertificateListResponse
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.ResendEmailInput
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class CertificateIdPayload(val certificateId: String)

@Singleton
class CertificatesRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun list(filters: CertificateListFilters): CertificateListResponse =
        api.call("certificates.list", filters)

    suspend fun get(certificateId: String): CertificateGetResponse =
        api.call("certificates.get", CertificateIdPayload(certificateId))

    suspend fun resendEmail(certificateId: String, recipientEmail: String?): OkResponse =
        api.call(
            "certificates.resendEmail",
            ResendEmailInput(certificateId = certificateId, recipientEmail = recipientEmail)
        )
}
