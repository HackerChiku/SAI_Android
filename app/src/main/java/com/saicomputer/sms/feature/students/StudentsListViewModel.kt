package com.saicomputer.sms.feature.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.repo.StudentsRepository
import com.saicomputer.sms.data.repo.paging.StudentsPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class StudentFilters(
    val search: String = "",
    val status: String = "All",
    val registrationSession: String = "All"
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class StudentsListViewModel @Inject constructor(
    private val repository: StudentsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _filters = MutableStateFlow(
        StudentFilters(
            search = savedStateHandle.get<String>(KEY_SEARCH).orEmpty(),
            status = savedStateHandle.get<String>(KEY_STATUS) ?: "All",
            registrationSession = savedStateHandle.get<String>(KEY_SESSION) ?: "All"
        )
    )
    val filters: StateFlow<StudentFilters> = _filters.asStateFlow()

    // Debounce search only; status/session changes apply immediately.
    val students: Flow<PagingData<Student>> = _filters
        .debounce { if (it.search.isBlank()) 0L else 300L }
        .distinctUntilChanged()
        .flatMapLatest { f ->
            Pager(
                config = PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = false)
            ) {
                StudentsPagingSource(
                    repo = repository,
                    search = f.search,
                    status = f.status,
                    registrationSession = f.registrationSession
                )
            }.flow
        }
        .cachedIn(viewModelScope)

    fun onSearchChange(value: String) {
        savedStateHandle[KEY_SEARCH] = value
        _filters.update { it.copy(search = value) }
    }

    fun onStatusChange(value: String) {
        savedStateHandle[KEY_STATUS] = value
        _filters.update { it.copy(status = value) }
    }

    fun onSessionChange(value: String) {
        savedStateHandle[KEY_SESSION] = value
        _filters.update { it.copy(registrationSession = value) }
    }

    fun clearFilters() {
        savedStateHandle[KEY_SEARCH] = ""
        savedStateHandle[KEY_STATUS] = "All"
        savedStateHandle[KEY_SESSION] = "All"
        _filters.value = StudentFilters()
    }

    companion object {
        private const val KEY_SEARCH = "students_search"
        private const val KEY_STATUS = "students_status"
        private const val KEY_SESSION = "students_session"
    }
}
