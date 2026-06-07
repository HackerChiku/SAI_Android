package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.EnrollmentTopicsListResponse
import com.saicomputer.sms.data.dto.MarkTopicCompleteInput
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.UnmarkTopicInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicsRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun listForEnrollment(enrollmentId: String): EnrollmentTopicsListResponse =
        api.call("enrollments.topics.list", EnrollmentIdPayload(enrollmentId))

    suspend fun markComplete(input: MarkTopicCompleteInput): OkResponse =
        api.call("enrollments.topics.markComplete", input)

    suspend fun unmark(input: UnmarkTopicInput): OkResponse =
        api.call("enrollments.topics.unmark", input)
}
