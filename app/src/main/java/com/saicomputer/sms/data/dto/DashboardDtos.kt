package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.DashboardPeriod
import kotlinx.serialization.Serializable

/** camelCase, case-sensitive filterType per spec. */
@Serializable
data class DashboardSummaryInput(
    val filterType: DashboardPeriod,
    val customStart: String = "",
    val customEnd: String = ""
)
