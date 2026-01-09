package com.example.car_shop.core.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val settingsCollection = firestore.collection("settings")
    private val shopLocationDoc = "shopLocation"

    suspend fun getShopLocation(): Result<String> {
        return try {
            val doc = settingsCollection.document(shopLocationDoc).get().await()
            val location = doc.getString("location") ?: ""
            Result.success(location)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setShopLocation(location: String): Result<Unit> {
        return try {
            settingsCollection.document(shopLocationDoc).set(
                mapOf("location" to location)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
