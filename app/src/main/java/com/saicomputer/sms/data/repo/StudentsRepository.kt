package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.Cached
import com.saicomputer.sms.core.session.KeyedSessionCache
import com.saicomputer.sms.core.session.SessionCache
import com.saicomputer.sms.core.session.SessionCacheRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import com.saicomputer.sms.data.dto.AadhaarNumberResponse
import com.saicomputer.sms.data.dto.ChangeStudentStatusInput
import com.saicomputer.sms.data.dto.ChangeStudentStatusResponse
import com.saicomputer.sms.data.dto.FileUploadInput
import com.saicomputer.sms.data.dto.PaymentListResponse
import com.saicomputer.sms.data.dto.ReplaceAadhaarResponse
import com.saicomputer.sms.data.dto.ReplacePhotoResponse
import com.saicomputer.sms.data.dto.StudentCreateInput
import com.saicomputer.sms.data.dto.StudentGetResponse
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.dto.StudentListResponse
import com.saicomputer.sms.data.dto.StudentUpdateInput
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.FileBase64Response
import com.saicomputer.sms.data.model.Student
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/** {"studentId": "..."} — file endpoints use camelCase. */
@Serializable
private data class StudentIdCamelPayload(val studentId: String)

/** {"StudentID": "..."} — get/getAadhaarNumber use PascalCase. */
@Serializable
private data class StudentIdPayload(@SerialName("StudentID") val studentId: String)

@Serializable
private data class StudentPaymentsFilter(
    @SerialName("StudentID") val studentId: String,
    val status: String = "Active",
    val limit: Int = 200
)

@Singleton
class StudentsRepository @Inject constructor(
    private val api: ApiClient,
    registry: SessionCacheRegistry
) {
    private val baseListCache = SessionCache<List<Student>>(registry)
    private val detailCache = KeyedSessionCache<String, StudentGetResponse>(registry)

    val baseListFlow: StateFlow<Cached<List<Student>>?> = baseListCache.flow

    fun getCachedBaseList(): List<Student>? = baseListCache.value

    fun isBaseListFresh(): Boolean = baseListCache.isFresh()

    fun getCachedStudent(studentId: String): StudentGetResponse? = detailCache.get(studentId)

    fun isStudentFresh(studentId: String): Boolean = detailCache.isFresh(studentId)

    suspend fun list(filters: StudentListFilters): StudentListResponse =
        api.call("students.list", filters)

    fun cacheBaseList(rows: List<Student>) {
        baseListCache.put(rows)
    }

    suspend fun refreshBaseList(): List<Student> {
        val rows = list(StudentListFilters(limit = 1000)).rows
        baseListCache.put(rows)
        return rows
    }

    /**
     * students.get returns the student DTO flat plus an `enrollments` array (no wrapper).
     * Payments aren't part of that response, so they are loaded via payments.list in
     * parallel to reduce total latency.
     */
    suspend fun get(studentId: String): StudentGetResponse = coroutineScope {
        val studentDeferred = async {
            api.callRaw(
                "students.get",
                api.json.encodeToJsonElement(StudentIdPayload(studentId))
            ).jsonObject
        }
        val paymentsDeferred = async {
            runCatching {
                val res: PaymentListResponse =
                    api.call("payments.list", StudentPaymentsFilter(studentId))
                res.rows
            }.getOrDefault(emptyList())
        }
        val data = studentDeferred.await()
        val student: Student = api.json.decodeFromJsonElement(data)
        val enrollments: List<Enrollment> =
            data["enrollments"]?.let { api.json.decodeFromJsonElement(it) } ?: emptyList()
        StudentGetResponse(student, enrollments, paymentsDeferred.await())
    }

    suspend fun refreshStudent(studentId: String): StudentGetResponse {
        val result = get(studentId)
        detailCache.put(studentId, result)
        return result
    }

    suspend fun create(input: StudentCreateInput): Student =
        api.call("students.create", input)

    suspend fun update(input: StudentUpdateInput): Student =
        api.call("students.update", input)

    suspend fun changeStatus(input: ChangeStudentStatusInput): ChangeStudentStatusResponse =
        api.call("students.changeStatus", input)

    suspend fun getAadhaarNumber(studentId: String): AadhaarNumberResponse =
        api.call("students.getAadhaarNumber", StudentIdPayload(studentId))

    suspend fun getPhotoBase64(studentId: String): FileBase64Response =
        api.call("students.getPhotoBase64", StudentIdCamelPayload(studentId))

    suspend fun getAadhaarBase64(studentId: String): FileBase64Response =
        api.call("students.getAadhaarBase64", StudentIdCamelPayload(studentId))

    suspend fun replacePhoto(input: FileUploadInput): ReplacePhotoResponse =
        api.call("students.replacePhoto", input)

    suspend fun replaceAadhaar(input: FileUploadInput): ReplaceAadhaarResponse =
        api.call("students.replaceAadhaar", input)
}
