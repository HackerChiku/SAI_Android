package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import com.saicomputer.sms.data.dto.FileUploadBase64Input
import com.saicomputer.sms.data.dto.SettingsResponse
import com.saicomputer.sms.data.dto.SettingsUpdateInput
import com.saicomputer.sms.data.dto.SettingsUpdateResponse
import com.saicomputer.sms.data.dto.UploadFileResponse
import com.saicomputer.sms.data.model.SettingEntry
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val settingsCache = SessionCache<List<SettingEntry>>(registry)

    val settingsFlow: StateFlow<Cached<List<SettingEntry>>?> = settingsCache.flow

    val settings: List<SettingEntry>
        get() = settingsCache.value.orEmpty()

    fun getCachedSettings(): List<SettingEntry>? = settingsCache.value

    fun isSettingsFresh(): Boolean = settingsCache.isFresh()

    suspend fun refresh(): List<SettingEntry> {
        val res: SettingsResponse = api.call("settings.getAll")
        settingsCache.put(res.settings)
        return res.settings
    }

    suspend fun update(input: SettingsUpdateInput): SettingsUpdateResponse {
        val res: SettingsUpdateResponse = api.call("settings.update", input)
        runCatching { refresh() }
        return res
    }

    suspend fun uploadLogo(input: FileUploadBase64Input): UploadFileResponse =
        api.call("settings.uploadLogo", input)

    suspend fun uploadQr(input: FileUploadBase64Input): UploadFileResponse =
        api.call("settings.uploadQR", input)
}
