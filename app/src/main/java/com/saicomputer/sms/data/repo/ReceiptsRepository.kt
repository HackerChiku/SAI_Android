package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.ReceiptGetResponse
import com.saicomputer.sms.data.dto.ReceiptListFilters
import com.saicomputer.sms.data.dto.ReceiptListResponse
import com.saicomputer.sms.data.dto.ResendEmailInput
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ReceiptIdPayload(val receiptId: String)

@Singleton
class ReceiptsRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun list(filters: ReceiptListFilters): ReceiptListResponse =
        api.call("receipts.list", filters)

    suspend fun get(receiptId: String): ReceiptGetResponse =
        api.call("receipts.get", ReceiptIdPayload(receiptId))

    suspend fun resendEmail(receiptId: String, recipientEmail: String?): OkResponse =
        api.call("receipts.resendEmail", ResendEmailInput(receiptId = receiptId, recipientEmail = recipientEmail))
}
