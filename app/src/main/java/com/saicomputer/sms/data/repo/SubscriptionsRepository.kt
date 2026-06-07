package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.EditEndDateInput
import com.saicomputer.sms.data.dto.ExtendSubscriptionInput
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.SubscriptionListFilters
import com.saicomputer.sms.data.dto.SubscriptionListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionsRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun list(filters: SubscriptionListFilters): SubscriptionListResponse =
        api.call("subscriptions.list", filters)

    suspend fun extend(input: ExtendSubscriptionInput): OkResponse =
        api.call("subscriptions.extend", input)

    suspend fun editEndDate(input: EditEndDateInput): OkResponse =
        api.call("subscriptions.editEndDate", input)
}
