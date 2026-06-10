package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import com.saicomputer.sms.data.model.ReceiptListItem
import kotlinx.coroutines.flow.StateFlow
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
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val baseListCache = SessionCache<List<ReceiptListItem>>(registry)

    val baseListFlow: StateFlow<Cached<List<ReceiptListItem>>?> = baseListCache.flow

    fun getCachedBaseList(): List<ReceiptListItem>? = baseListCache.value

    fun isBaseListFresh(): Boolean = baseListCache.isFresh()

    suspend fun list(filters: ReceiptListFilters): ReceiptListResponse =
        api.call("receipts.list", filters)

    fun cacheBaseList(rows: List<ReceiptListItem>) {
        baseListCache.put(rows)
    }

    suspend fun refreshBaseList(): List<ReceiptListItem> {
        val rows = list(ReceiptListFilters(pageSize = 500)).receipts
        baseListCache.put(rows)
        return rows
    }

    suspend fun get(receiptId: String): ReceiptGetResponse =
        api.call("receipts.get", ReceiptIdPayload(receiptId))

    suspend fun resendEmail(receiptId: String, recipientEmail: String?): OkResponse =
        api.call("receipts.resendEmail", ResendEmailInput(receiptId = receiptId, recipientEmail = recipientEmail))
}
