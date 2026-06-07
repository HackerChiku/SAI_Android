package com.saicomputer.sms.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The single request envelope every backend call uses:
 * { "action": string, "sessionToken": string|null, "payload": object }
 */
@Serializable
data class ApiRequest(
    val action: String,
    val sessionToken: String?,
    val payload: JsonElement
)
