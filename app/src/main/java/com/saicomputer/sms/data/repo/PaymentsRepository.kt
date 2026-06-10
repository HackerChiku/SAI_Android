package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import kotlinx.coroutines.flow.StateFlow
import com.saicomputer.sms.data.dto.EditBillingMonthInput
import com.saicomputer.sms.data.dto.InstallmentPaymentCreateInput
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.PaymentCreateInput
import com.saicomputer.sms.data.dto.PaymentCreateResponse
import com.saicomputer.sms.data.dto.PaymentListFilters
import com.saicomputer.sms.data.dto.ReceiptListFilters
import com.saicomputer.sms.data.dto.ReceiptListResponse
import com.saicomputer.sms.data.dto.SubscriptionPaymentCreateInput
import com.saicomputer.sms.data.dto.VoidPaymentInput
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.RegistrationSession
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val baseListCache = SessionCache<List<PaymentListItem>>(registry)

    val baseListFlow: StateFlow<Cached<List<PaymentListItem>>?> = baseListCache.flow

    fun getCachedBaseList(): List<PaymentListItem>? = baseListCache.value

    fun isBaseListFresh(): Boolean = baseListCache.isFresh()

    fun cacheBaseList(rows: List<PaymentListItem>) {
        baseListCache.put(rows)
    }

    suspend fun refreshBaseList(): List<PaymentListItem> {
        val rows = list(PaymentListFilters(limit = 500))
        baseListCache.put(rows)
        return rows
    }

    suspend fun create(input: PaymentCreateInput): PaymentCreateResponse = when (input) {
        is InstallmentPaymentCreateInput -> api.call("payments.create", input)
        is SubscriptionPaymentCreateInput -> api.call("payments.create", input)
    }

    suspend fun void(input: VoidPaymentInput): OkResponse = api.call("payments.void", input)

    suspend fun editBillingMonth(input: EditBillingMonthInput): OkResponse =
        api.call("payments.editBillingMonth", input)

    suspend fun list(filters: PaymentListFilters = PaymentListFilters()): List<PaymentListItem> {
        val data = api.callRaw(
            "payments.list",
            api.json.encodeToJsonElement(filters)
        ).jsonObject
        val rows = data["rows"]?.jsonArray ?: return emptyList()
        val receiptByPayment = runCatching {
            api.call<ReceiptListResponse, ReceiptListFilters>(
                "receipts.list",
                ReceiptListFilters(pageSize = 500)
            ).receipts.associateBy { it.paymentId }
        }.getOrDefault(emptyMap())

        return rows.map { elem ->
            val obj = elem.jsonObject
            val payment: Payment = api.json.decodeFromJsonElement(elem)
            val studentName = obj["StudentName"]?.let { api.json.decodeFromJsonElement<String>(it) }
                ?: receiptByPayment[payment.paymentId]?.studentName
                ?: ""
            val courseName = obj["CourseName"]?.let { api.json.decodeFromJsonElement<String>(it) }.orEmpty()
            val session = obj["RegistrationSession"]?.let {
                runCatching { api.json.decodeFromJsonElement<RegistrationSession>(it) }.getOrNull()
            }
            PaymentListItem(
                payment = payment,
                studentName = studentName.ifBlank { "Unknown student" },
                courseName = courseName,
                registrationSession = session
            )
        }
    }
}
