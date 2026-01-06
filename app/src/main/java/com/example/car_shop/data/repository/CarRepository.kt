package com.example.car_shop.data.repository

import android.net.Uri
import com.example.car_shop.data.model.Car
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor(
    firestore: FirebaseFirestore,
    storage: FirebaseStorage
) {
    private val carsCollection = firestore.collection("cars")
    private val storageRef = storage.reference.child("car_images")

    // Get all cars
    fun getAllCars(): Flow<List<Car>> = callbackFlow {
        val listener = carsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val cars = snapshot?.documents?.mapNotNull { doc ->
                    Car.fromMap(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()

                trySend(cars)
            }

        awaitClose { listener.remove() }
    }

    // Get car by ID
    suspend fun getCarById(carId: String): Result<Car> {
        return try {
            val doc = carsCollection.document(carId).get().await()
            if (doc.exists()) {
                val car = Car.fromMap(doc.id, doc.data ?: emptyMap())
                Result.success(car)
            } else {
                Result.failure(Exception("Car not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Search cars
    fun searchCars(query: String): Flow<List<Car>> = callbackFlow {
        val listener = carsCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val cars = snapshot?.documents?.mapNotNull { doc ->
                    Car.fromMap(doc.id, doc.data ?: emptyMap())
                }?.filter { car ->
                    car.name.contains(query, ignoreCase = true) ||
                    car.brand.contains(query, ignoreCase = true) ||
                    car.model.contains(query, ignoreCase = true)
                } ?: emptyList()

                trySend(cars)
            }

        awaitClose { listener.remove() }
    }

    // Add new car (Admin)
    suspend fun addCar(car: Car, imageUri: Uri?): Result<String> {
        return try {
            val carId = UUID.randomUUID().toString()
            val imageUrl = imageUri?.let { uploadImage(it, carId) } ?: ""

            val carWithImage = car.copy(imageUrl = imageUrl)
            carsCollection.document(carId).set(carWithImage.toMap()).await()

            Result.success(carId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update car (Admin)
    suspend fun updateCar(carId: String, car: Car, imageUri: Uri?): Result<Unit> {
        return try {
            val imageUrl = imageUri?.let { uploadImage(it, carId) } ?: car.imageUrl
            val updatedCar = car.copy(imageUrl = imageUrl)

            carsCollection.document(carId).update(updatedCar.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete car (Admin)
    suspend fun deleteCar(carId: String): Result<Unit> {
        return try {
            // Delete image from storage
            try {
                storageRef.child(carId).delete().await()
            } catch (e: Exception) {
                // Image might not exist, continue with deletion
            }

            // Delete car document
            carsCollection.document(carId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload image to Firebase Storage
    private suspend fun uploadImage(uri: Uri, carId: String): String {
        return try {
            val imageRef = storageRef.child(carId)
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            ""
        }
    }

    // Get cars by IDs (for favorites)
    suspend fun getCarsByIds(carIds: Set<String>): Result<List<Car>> {
        return try {
            if (carIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val cars = mutableListOf<Car>()
            carIds.chunked(10).forEach { chunk ->
                val snapshot = carsCollection
                    .whereIn("__name__", chunk.toList())
                    .get()
                    .await()

                cars.addAll(
                    snapshot.documents.mapNotNull { doc ->
                        Car.fromMap(doc.id, doc.data ?: emptyMap())
                    }
                )
            }

            Result.success(cars)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
