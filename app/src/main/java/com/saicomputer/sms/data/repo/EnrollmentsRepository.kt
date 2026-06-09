package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.KeyedSessionCache
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import kotlinx.coroutines.flow.StateFlow
import com.saicomputer.sms.data.dto.CancelEnrollmentInput
import com.saicomputer.sms.data.dto.EditInstallmentsInput
import com.saicomputer.sms.data.dto.EditInstallmentsResponse
import com.saicomputer.sms.data.dto.EnrollmentCreateInput
import com.saicomputer.sms.data.dto.EnrollmentGetResponse
import com.saicomputer.sms.data.dto.EnrollmentListFilters
import com.saicomputer.sms.data.dto.EnrollmentListResponse
import com.saicomputer.sms.data.dto.EnrollmentPreviewInput
import com.saicomputer.sms.data.dto.EnrollmentPreviewResult
import com.saicomputer.sms.data.dto.InstallmentEnrollmentCreateInput
import com.saicomputer.sms.data.dto.MarkCompleteInput
import com.saicomputer.sms.data.dto.MarkCompleteResponse
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.SetExcludedFromBillingInput
import com.saicomputer.sms.data.dto.SetExcludedFromBillingResult
import com.saicomputer.sms.data.dto.SubscriptionEnrollmentCreateInput
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Payment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/** {"EnrollmentID": "..."} — enrollments.get uses PascalCase. */
@Serializable
private data class EnrollmentIdPascalPayload(@SerialName("EnrollmentID") val enrollmentId: String)

@Singleton
class EnrollmentsRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val baseListCache = SessionCache<List<Enrollment>>(registry)
    private val detailCache = KeyedSessionCache<String, EnrollmentGetResponse>(registry)

    val baseListFlow: StateFlow<Cached<List<Enrollment>>?> = baseListCache.flow

    fun getCachedBaseList(): List<Enrollment>? = baseListCache.value

    fun isBaseListFresh(): Boolean = baseListCache.isFresh()

    fun getCachedEnrollment(enrollmentId: String): EnrollmentGetResponse? =
        detailCache.get(enrollmentId)

    fun isEnrollmentFresh(enrollmentId: String): Boolean = detailCache.isFresh(enrollmentId)

    suspend fun list(filters: EnrollmentListFilters = EnrollmentListFilters()): EnrollmentListResponse =
        api.call("enrollments.list", filters)

    fun cacheBaseList(rows: List<Enrollment>) {
        baseListCache.put(rows)
    }

    suspend fun refreshBaseList(): List<Enrollment> {
        val rows = list(EnrollmentListFilters(limit = 500)).rows
        baseListCache.put(rows)
        return rows
    }

    suspend fun preview(input: EnrollmentPreviewInput): EnrollmentPreviewResult =
        api.call("enrollments.preview", input)

    suspend fun create(input: EnrollmentCreateInput): Enrollment = when (input) {
        is InstallmentEnrollmentCreateInput -> api.call("enrollments.create", input)
        is SubscriptionEnrollmentCreateInput -> api.call("enrollments.create", input)
    }

    /**
     * enrollments.get returns the enrollment flat (with nested student/course/installments/
     * topics) and a separate `payments` array. Decode both and wrap.
     */
    suspend fun get(enrollmentId: String): EnrollmentGetResponse {
        val data = api.callRaw(
            "enrollments.get",
            api.json.encodeToJsonElement(EnrollmentIdPascalPayload(enrollmentId))
        ).jsonObject
        val enrollment: Enrollment = api.json.decodeFromJsonElement(data)
        val payments: List<Payment> =
            data["payments"]?.let { api.json.decodeFromJsonElement(it) } ?: emptyList()
        return EnrollmentGetResponse(enrollment, payments)
    }

    suspend fun refreshEnrollment(enrollmentId: String): EnrollmentGetResponse {
        val result = get(enrollmentId)
        detailCache.put(enrollmentId, result)
        return result
    }

    suspend fun markComplete(input: MarkCompleteInput): MarkCompleteResponse =
        api.call("enrollments.markComplete", input)

    suspend fun cancel(input: CancelEnrollmentInput): OkResponse =
        api.call("enrollments.cancel", input)

    suspend fun editInstallments(input: EditInstallmentsInput): EditInstallmentsResponse =
        api.call("enrollments.editInstallments", input)

    suspend fun setExcludedFromBilling(
        input: SetExcludedFromBillingInput
    ): SetExcludedFromBillingResult =
        api.call("enrollments.setExcludedFromBilling", input)
}

@kotlinx.serialization.Serializable
internal data class EnrollmentIdPayload(val enrollmentId: String)
