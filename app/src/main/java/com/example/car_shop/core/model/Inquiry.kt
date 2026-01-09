package com.example.car_shop.core.model

data class Inquiry(
    val id: String = "",
    val carId: String = "",
    val carName: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userProfileImageUrl: String = "",
    val message: String = "",
    val reply: String = "",
    val status: InquiryStatus = InquiryStatus.PENDING,
    val userHasRead: Boolean = true, // Default true because user knows they sent it. False when admin replies.
    val adminHasRead: Boolean = false, // False when created, true when admin sees it.
    val hiddenByUser: Boolean = false, // True when user hides the inquiry from their view
    val createdAt: Long = System.currentTimeMillis(),
    val repliedAt: Long? = null
) {
    fun toMap(): Map<String, Any> {
        return buildMap {
            put("carId", carId)
            put("carName", carName)
            put("userId", userId)
            put("userName", userName)
            put("userEmail", userEmail)
            put("userPhone", userPhone)
            put("userProfileImageUrl", userProfileImageUrl)
            put("message", message)
            put("reply", reply)
            put("status", status.name)
            put("userHasRead", userHasRead)
            put("adminHasRead", adminHasRead)
            put("hiddenByUser", hiddenByUser)
            put("createdAt", createdAt)
            repliedAt?.let { put("repliedAt", it) }
        }
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Inquiry {
            return Inquiry(
                id = id,
                carId = map["carId"] as? String ?: "",
                carName = map["carName"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userEmail = map["userEmail"] as? String ?: "",
                userPhone = map["userPhone"] as? String ?: "",
                userProfileImageUrl = map["userProfileImageUrl"] as? String ?: "",
                message = map["message"] as? String ?: "",
                reply = map["reply"] as? String ?: "",
                status = InquiryStatus.valueOf(map["status"] as? String ?: "PENDING"),
                userHasRead = map["userHasRead"] as? Boolean ?: true,
                adminHasRead = map["adminHasRead"] as? Boolean ?: false,
                hiddenByUser = map["hiddenByUser"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                repliedAt = map["repliedAt"] as? Long
            )
        }
    }
}

enum class InquiryStatus {
    PENDING,
    REPLIED,
    CLOSED
}

