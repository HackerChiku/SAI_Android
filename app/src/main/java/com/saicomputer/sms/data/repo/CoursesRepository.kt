package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import kotlinx.coroutines.flow.StateFlow
import com.saicomputer.sms.data.dto.BulkSaveCourseTopicsInput
import com.saicomputer.sms.data.dto.CourseCreateInput
import com.saicomputer.sms.data.dto.CourseGetResponse
import com.saicomputer.sms.data.dto.CourseListResponse
import com.saicomputer.sms.data.dto.CourseTopicsListResponse
import com.saicomputer.sms.data.dto.CourseUpdateInput
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.model.Course
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject
import javax.inject.Singleton

/** {"CourseID": "..."} — get/delete use PascalCase. */
@Serializable
private data class CourseIdPayload(@SerialName("CourseID") val courseId: String)

/** {"courseId": "...", "includeInactive": false} — topics.list uses camelCase. */
@Serializable
private data class CourseTopicsListPayload(
    val courseId: String,
    val includeInactive: Boolean = false
)

@Singleton
class CoursesRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val listCache = SessionCache<List<Course>>(registry)

    val listFlow: StateFlow<Cached<List<Course>>?> = listCache.flow

    fun getCachedList(): List<Course>? = listCache.value

    fun isListFresh(): Boolean = listCache.isFresh()

    suspend fun list(): CourseListResponse = api.call("courses.list")

    fun cacheList(rows: List<Course>) {
        listCache.put(rows)
    }

    suspend fun refreshList(): List<Course> {
        val rows = list().rows
        listCache.put(rows)
        return rows
    }

    /** courses.get returns the course DTO flat; decode and wrap. */
    suspend fun get(courseId: String): CourseGetResponse {
        val data = api.callRaw("courses.get", api.json.encodeToJsonElement(CourseIdPayload(courseId)))
        return CourseGetResponse(api.json.decodeFromJsonElement<Course>(data))
    }

    suspend fun create(input: CourseCreateInput): Course = api.call("courses.create", input)

    suspend fun update(input: CourseUpdateInput): Course = api.call("courses.update", input)

    suspend fun delete(courseId: String): OkResponse =
        api.call("courses.delete", CourseIdPayload(courseId))

    suspend fun listTopics(courseId: String): CourseTopicsListResponse =
        api.call("courses.topics.list", CourseTopicsListPayload(courseId))

    suspend fun bulkSaveTopics(input: BulkSaveCourseTopicsInput): OkResponse =
        api.call("courses.topics.bulkSave", input)
}
