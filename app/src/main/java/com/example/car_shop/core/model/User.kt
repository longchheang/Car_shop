package com.example.car_shop.core.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val isAdmin: Boolean = false,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "email" to email,
            "name" to name,
            "phone" to phone,
            "isAdmin" to isAdmin,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): User {
            return User(
                id = id,
                email = map["email"] as? String ?: "",
                name = map["name"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                isAdmin = map["isAdmin"] as? Boolean ?: false,
                profileImageUrl = map["profileImageUrl"] as? String ?: "",
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }
}

