package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.EditBillingMonthInput
import com.saicomputer.sms.data.dto.InstallmentPaymentCreateInput
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.dto.PaymentCreateInput
import com.saicomputer.sms.data.dto.PaymentCreateResponse
import com.saicomputer.sms.data.dto.SubscriptionPaymentCreateInput
import com.saicomputer.sms.data.dto.VoidPaymentInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentsRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun create(input: PaymentCreateInput): PaymentCreateResponse = when (input) {
        is InstallmentPaymentCreateInput -> api.call("payments.create", input)
        is SubscriptionPaymentCreateInput -> api.call("payments.create", input)
    }

    suspend fun void(input: VoidPaymentInput): OkResponse = api.call("payments.void", input)

    suspend fun editBillingMonth(input: EditBillingMonthInput): OkResponse =
        api.call("payments.editBillingMonth", input)
}
