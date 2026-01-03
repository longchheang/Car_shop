package com.example.car_shop.data.repository

import com.example.car_shop.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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

    // Update user profile
    suspend fun updateProfile(name: String, phone: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: throw Exception("User not logged in")
            
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "name" to name,
                        "phone" to phone
                    )
                )
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

    // Observe auth state
    fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
}
