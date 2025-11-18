package com.tonio.albarapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class WorkSlipStatus {
    DRAFT,
    PENDING_CONTRACTOR,
    PENDING_MANAGER,
    APPROVED,
    REJECTED
}

data class Signature(
    val userId: String,
    val userName: String,
    val timestamp: String, // Changed to String for JSON serialization
    val signatureData: String? = null
) {
    // Helper to get LocalDateTime
    fun getTimestamp(): LocalDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    companion object {
        fun create(userId: String, userName: String, timestamp: LocalDateTime): Signature {
            return Signature(userId, userName, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }
    }
}

data class Comment(
    val id: String,
    val workSlipId: String,
    val userId: String,
    val userName: String,
    val userRole: com.tonio.albarapp.UserRole,
    val message: String,
    val timestamp: String, // ISO format for JSON serialization
    val isRejectionReason: Boolean = false
) {
    fun getTimestamp(): LocalDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    companion object {
        fun create(
            id: String,
            workSlipId: String,
            userId: String,
            userName: String,
            userRole: com.tonio.albarapp.UserRole,
            message: String,
            timestamp: LocalDateTime,
            isRejectionReason: Boolean = false
        ): Comment {
            return Comment(
                id = id,
                workSlipId = workSlipId,
                userId = userId,
                userName = userName,
                userRole = userRole,
                message = message,
                timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                isRejectionReason = isRejectionReason
            )
        }
    }
}

data class WorkSlip(
    val id: String,
    val title: String,
    val date: String, // Changed to String for JSON serialization
    val worksiteId: String,
    val worksiteName: String,
    val location: String,

    val subcontractorId: String,
    val subcontractorName: String,
    val contractorId: String,
    val contractorName: String,
    val managerId: String?,

    val lineItems: List<LineItem>,
    val vatRate: Int,
    val totalCents: Long,

    val status: WorkSlipStatus,
    val subcontractorSignature: Signature? = null,
    val contractorSignature: Signature? = null,
    val managerApproval: Signature? = null,

    val createdAt: String, // Changed to String for JSON serialization
    val updatedAt: String,  // Changed to String for JSON serialization

    val comments: List<Comment> = emptyList()
) {
    // Helper functions to get LocalDate/LocalDateTime
    fun getDate(): LocalDate = LocalDate.parse(date)
    fun getCreatedAt(): LocalDateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    fun getUpdatedAt(): LocalDateTime = LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    companion object {
        fun create(
            id: String,
            title: String,
            date: LocalDate,
            worksiteId: String,
            worksiteName: String,
            location: String,
            subcontractorId: String,
            subcontractorName: String,
            contractorId: String,
            contractorName: String,
            managerId: String?,
            lineItems: List<LineItem>,
            vatRate: Int,
            totalCents: Long,
            status: WorkSlipStatus,
            subcontractorSignature: Signature? = null,
            contractorSignature: Signature? = null,
            managerApproval: Signature? = null,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): WorkSlip {
            return WorkSlip(
                id = id,
                title = title,
                date = date.toString(),
                worksiteId = worksiteId,
                worksiteName = worksiteName,
                location = location,
                subcontractorId = subcontractorId,
                subcontractorName = subcontractorName,
                contractorId = contractorId,
                contractorName = contractorName,
                managerId = managerId,
                lineItems = lineItems,
                vatRate = vatRate,
                totalCents = totalCents,
                status = status,
                subcontractorSignature = subcontractorSignature,
                contractorSignature = contractorSignature,
                managerApproval = managerApproval,
                createdAt = createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                updatedAt = updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
    }
}

// Repository to manage work slips with persistent storage
object WorkSlipRepository {
    private var workSlips = mutableListOf<WorkSlip>()
    private var preferences: SharedPreferences? = null
    private val gson = Gson()
    private const val PREFS_NAME = "workslip_prefs"
    private const val KEY_WORKSLIPS = "workslips"

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPreferences()
    }

    private fun loadFromPreferences() {
        val json = preferences?.getString(KEY_WORKSLIPS, null)
        if (json != null) {
            val type = object : TypeToken<List<WorkSlip>>() {}.type
            workSlips = gson.fromJson<List<WorkSlip>>(json, type).toMutableList()
        }
    }

    private fun saveToPreferences() {
        val json = gson.toJson(workSlips)
        preferences?.edit()?.putString(KEY_WORKSLIPS, json)?.apply()
    }

    fun getAll(): List<WorkSlip> = workSlips.toList()

    fun getForUser(userId: String, userRole: com.tonio.albarapp.UserRole): List<WorkSlip> {
        return when (userRole) {
            com.tonio.albarapp.UserRole.SUBCONTRACTOR -> {
                workSlips.filter { it.subcontractorId == userId }
            }
            com.tonio.albarapp.UserRole.CONTRACTOR -> {
                workSlips.filter { it.contractorId == userId }
            }
            com.tonio.albarapp.UserRole.MANAGER -> {
                // See work slips pending approval or already approved/rejected by them
                workSlips.filter {
                    it.managerId == userId &&
                            (it.status == WorkSlipStatus.PENDING_MANAGER ||
                                    it.status == WorkSlipStatus.APPROVED ||
                                    it.status == WorkSlipStatus.REJECTED)
                }
            }
        }
    }

    fun add(workSlip: WorkSlip) {
        workSlips.add(workSlip)
        saveToPreferences()
    }

    fun update(workSlip: WorkSlip) {
        val index = workSlips.indexOfFirst { it.id == workSlip.id }
        if (index != -1) {
            workSlips[index] = workSlip
            saveToPreferences()
        }
    }

    fun getById(id: String): WorkSlip? {
        return workSlips.find { it.id == id }
    }

    fun addComment(workSlipId: String, comment: Comment) {
        val workSlip = getById(workSlipId)
        if (workSlip != null) {
            val updatedComments = workSlip.comments + comment
            val updatedWorkSlip = workSlip.copy(comments = updatedComments)
            update(updatedWorkSlip)
        }
    }
}