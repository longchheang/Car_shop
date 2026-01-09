package com.example.car_shop.core.repository

import com.example.car_shop.core.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    // Register new user
    suspend fun register(email: String, password: String, name: String, phone: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID not found")

            val user = User(
                id = userId,
                email = email,
                name = name,
                phone = phone,
                isAdmin = false
            )

            firestore.collection("users")
                .document(userId)
                .set(user.toMap())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login user
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID not found")

            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = User.fromMap(userId, userDoc.data ?: emptyMap())

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get current user data
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val userId = currentUserId ?: return Result.success(null)
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            if (userDoc.exists()) {
                val user = User.fromMap(userId, userDoc.data ?: emptyMap())
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get total number of users (for admin dashboard stats)
    suspend fun getTotalUsers(): Result<Int> {
        return try {
            // Count only non-admin users
            val snapshot = firestore.collection("users")
                .whereEqualTo("isAdmin", false)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload profile image
    suspend fun uploadProfileImage(uri: android.net.Uri): Result<String> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            val ref = storage.reference.child("profile_images/$userId/${System.currentTimeMillis()}.jpg")
            
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user profile
    suspend fun updateProfile(name: String, phone: String, imageUrl: String? = null): Result<Unit> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "phone" to phone
            )
            
            if (imageUrl != null) {
                updates["profileImageUrl"] = imageUrl
            }
            
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout
    fun logout() {
        firebaseAuth.signOut()
    }

}

