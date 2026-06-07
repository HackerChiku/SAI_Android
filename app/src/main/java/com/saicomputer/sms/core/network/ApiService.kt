package com.saicomputer.sms.core.network

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Single-endpoint Retrofit service. The exact deployed Apps Script /exec URL is
 * passed as an absolute [Url] (see [com.saicomputer.sms.di.NetworkModule]). We
 * read the raw [ResponseBody] so [ApiClient] can parse the `{ ok, data|error }`
 * envelope leniently with kotlinx.serialization.
 */
interface ApiService {
    @POST
    suspend fun call(@Url url: String, @Body body: ApiRequest): ResponseBody
}
