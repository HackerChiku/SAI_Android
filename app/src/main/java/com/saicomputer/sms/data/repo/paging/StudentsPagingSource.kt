package com.saicomputer.sms.data.repo.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.repo.StudentsRepository

/**
 * Offset-based PagingSource for the Students list. Paging 3 provides the
 * keepPreviousData-style smooth refresh on filter/page changes.
 */
class StudentsPagingSource(
    private val repo: StudentsRepository,
    private val search: String?,
    private val status: String?,
    private val registrationSession: String?,
    private val pageSize: Int = 50
) : PagingSource<Int, Student>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Student> {
        val offset = params.key ?: 0
        return try {
            val response = repo.list(
                StudentListFilters(
                    search = search?.takeIf { it.isNotBlank() },
                    status = status?.takeIf { it.isNotBlank() && it != "All" },
                    registrationSession = registrationSession?.takeIf { it.isNotBlank() && it != "All" },
                    limit = pageSize,
                    offset = offset
                )
            )
            val rows = response.rows
            val nextOffset = offset + rows.size
            LoadResult.Page(
                data = rows,
                prevKey = if (offset == 0) null else (offset - pageSize).coerceAtLeast(0),
                nextKey = if (rows.isEmpty() || nextOffset >= response.total) null else nextOffset
            )
        } catch (e: ApiException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Student>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(pageSize)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(pageSize)
        }
    }
}
