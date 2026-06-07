package com.saicomputer.sms.data.model

import kotlinx.serialization.Serializable

enum class TopicDurationUnit { Weeks, Months }

@Serializable
data class CourseTopic(
    val courseTopicId: String,
    val courseId: String,
    val topicName: String,
    val description: String? = null,
    val estimatedDurationValue: Int,
    val estimatedDurationUnit: TopicDurationUnit,
    val isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class EnrollmentTopic(
    val enrollmentTopicId: String,
    val topicName: String,
    val description: String? = null,
    val estimatedDurationValue: Int,
    val estimatedDurationUnit: TopicDurationUnit,
    val isCompleted: Boolean = false,
    val completedDate: String? = null,
    val completedBy: String? = null,
    val completionNotes: String? = null
)

@Serializable
data class TopicsSummary(
    val hasTopics: Boolean,
    val total: Int,
    val completed: Int,
    val remaining: Int,
    val progressPercent: Int
)
