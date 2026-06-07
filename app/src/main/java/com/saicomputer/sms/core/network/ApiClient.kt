package com.saicomputer.sms.core.network

import com.saicomputer.sms.core.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single-call analog of the web client's `apiCall<T>`.
 *
 * Builds the request envelope, performs the POST, parses the `{ ok, data|error }`
 * response leniently, and either returns the decoded `data` or throws an
 * [ApiException]. On [ErrorCode.UNAUTHENTICATED] it clears the session so the
 * navigation layer can route back to Login.
 */
@Singleton
class ApiClient @Inject constructor(
    private val api: ApiService,
    @PublishedApi internal val json: Json,
    private val session: SessionManager,
    @com.saicomputer.sms.di.WebAppUrl private val webAppUrl: String
) {

    /** Call with no payload (sends an empty object). */
    suspend inline fun <reified T> call(action: String): T =
        json.decodeFromJsonElement(callRaw(action, JsonObject(emptyMap())))

    /** Call with a typed, @Serializable payload (or a simple typed Map). */
    suspend inline fun <reified T, reified P> call(action: String, payload: P): T =
        json.decodeFromJsonElement(callRaw(action, json.encodeToJsonElement(payload)))

    /** Returns the raw `data` JsonElement, throwing [ApiException] on failure. */
    suspend fun callRaw(action: String, payload: JsonElement): JsonElement =
        withContext(Dispatchers.IO) {
            val request = ApiRequest(action, session.token, payload)
            val raw = try {
                api.call(webAppUrl, request).string()
            } catch (e: IOException) {
                throw ApiException(ErrorCode.NETWORK, e.message ?: "Network error")
            }

            val env = try {
                json.parseToJsonElement(raw).jsonObject
            } catch (e: Exception) {
                throw ApiException(ErrorCode.INTERNAL_ERROR, "Unexpected server response")
            }

            val ok = env["ok"]?.jsonPrimitive?.boolean ?: false
            if (ok) {
                env["data"] ?: JsonObject(emptyMap())
            } else {
                val err = env["error"]?.jsonObject
                val code = err?.get("code")?.jsonPrimitive?.contentOrNull ?: ErrorCode.INTERNAL_ERROR
                val message = err?.get("message")?.jsonPrimitive?.contentOrNull ?: "Error"
                if (code == ErrorCode.UNAUTHENTICATED) session.clear()
                throw ApiException(code, message, err?.get("details"))
            }
        }
}
