package com.example.car_shop.core.model

data class Car(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val mileage: Int = 0,
    val fuelType: String = "",
    val transmission: String = "",
    val bodyType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "brand" to brand,
            "model" to model,
            "year" to year,
            "price" to price,
            "description" to description,
            "imageUrl" to imageUrl,
            "mileage" to mileage,
            "fuelType" to fuelType,
            "transmission" to transmission,
            "bodyType" to bodyType,
            "createdAt" to createdAt,
            "isAvailable" to isAvailable
        )
    }

    companion object {
        // Convert from Firestore document
        fun fromMap(id: String, map: Map<String, Any>): Car {
            return Car(
                id = id,
                name = map["name"] as? String ?: "",
                brand = map["brand"] as? String ?: "",
                model = map["model"] as? String ?: "",
                year = (map["year"] as? Long)?.toInt() ?: 0,
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                description = map["description"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                mileage = (map["mileage"] as? Long)?.toInt() ?: 0,
                fuelType = map["fuelType"] as? String ?: "",
                transmission = map["transmission"] as? String ?: "",
                bodyType = map["bodyType"] as? String ?: "",
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                isAvailable = map["isAvailable"] as? Boolean ?: true
            )
        }
    }
}

