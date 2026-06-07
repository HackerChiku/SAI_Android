package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.FileUploadBase64Input
import com.saicomputer.sms.data.dto.SettingsResponse
import com.saicomputer.sms.data.dto.SettingsUpdateInput
import com.saicomputer.sms.data.dto.SettingsUpdateResponse
import com.saicomputer.sms.data.dto.UploadFileResponse
import com.saicomputer.sms.data.model.SettingEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: ApiClient
) {
    private val _settings = MutableStateFlow<List<SettingEntry>>(emptyList())
    val settings: StateFlow<List<SettingEntry>> = _settings.asStateFlow()

    suspend fun refresh(): List<SettingEntry> {
        val res: SettingsResponse = api.call("settings.getAll")
        _settings.value = res.settings
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
